package com.jpdev.appcontrolfinanciero.ui.agregar

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.jpdev.appcontrolfinanciero.AppControlFinancieroApplication
import com.jpdev.appcontrolfinanciero.data.AddExpenseResult
import com.jpdev.appcontrolfinanciero.data.FinanceRepository
import com.jpdev.appcontrolfinanciero.data.prefs.SettingsDataStore
import com.jpdev.appcontrolfinanciero.domain.BalanceStatus
import com.jpdev.appcontrolfinanciero.domain.BudgetCalculator
import com.jpdev.appcontrolfinanciero.domain.ExpenseCategory
import com.jpdev.appcontrolfinanciero.domain.VoiceParser
import com.jpdev.appcontrolfinanciero.notifications.NotificationHelper
import com.jpdev.appcontrolfinanciero.ui.navigation.EntryType
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class AddEntryUiState(
    val type: EntryType,
    val isEditing: Boolean = false,
    val description: String = "",
    val incomeCategory: String = "",
    val expenseCategory: ExpenseCategory = ExpenseCategory.OTRO,
    val amountText: String = "",
    val date: LocalDate = LocalDate.now(),
    val descriptionError: String? = null,
    val amountError: String? = null,
    val saveError: String? = null,
    val maxAllowed: Long? = null,
    val saved: Boolean = false
) {
    val isAmountValid: Boolean get() = amountText.toLongOrNull()?.let { it > 0 } == true
    val canSave: Boolean
        get() {
            if (description.isBlank() || !isAmountValid) return false
            if (type == EntryType.EGRESO) {
                val amount = amountText.toLongOrNull() ?: return false
                val max = maxAllowed ?: return true // not loaded yet, allow attempt (repo re-validates anyway)
                if (amount > max) return false
            }
            return true
        }
}

class AddEntryViewModel(
    context: Context,
    private val repository: FinanceRepository,
    private val settingsDataStore: SettingsDataStore,
    type: EntryType,
    private val editId: Long?
) : ViewModel() {

    private val appContext: Context = context.applicationContext

    private val _uiState = MutableStateFlow(AddEntryUiState(type = type, isEditing = editId != null && editId != -1L))
    val uiState: StateFlow<AddEntryUiState> = _uiState

    val voiceController = VoiceRecognizerController(appContext)
    val voiceState: StateFlow<VoiceState> = voiceController.state

    private var originalAmount: Long = 0

    init {
        viewModelScope.launch {
            if (_uiState.value.isEditing) {
                val expense = repository.getExpenseById(editId!!)
                if (expense != null) {
                    originalAmount = expense.amount
                    _uiState.value = _uiState.value.copy(
                        description = expense.description,
                        expenseCategory = ExpenseCategory.entries.firstOrNull { it.name == expense.category } ?: ExpenseCategory.OTRO,
                        amountText = expense.amount.toString(),
                        date = LocalDate.ofEpochDay(expense.date)
                    )
                }
            }
            refreshMaxAllowed()
        }
    }

    // ponytail: computed once when the form opens/edits, not re-derived on every keystroke —
    // FinanceRepository re-validates for real at save time regardless, so this is a UI hint only.
    private suspend fun refreshMaxAllowed() {
        if (_uiState.value.type != EntryType.EGRESO) return
        val (totalIncome, totalExpenses) = repository.getMonthTotals(YearMonth.from(_uiState.value.date))
        val max = BudgetCalculator.maxAllowedExpense(totalIncome, totalExpenses - originalAmount)
        _uiState.value = _uiState.value.copy(maxAllowed = max)
    }

    fun updateDescription(value: String) {
        _uiState.value = _uiState.value.copy(description = value, descriptionError = null)
    }

    fun updateIncomeCategory(value: String) {
        _uiState.value = _uiState.value.copy(incomeCategory = value)
    }

    fun updateExpenseCategory(value: ExpenseCategory) {
        _uiState.value = _uiState.value.copy(expenseCategory = value)
    }

    fun updateAmountText(value: String) {
        val digitsOnly = value.filter { it.isDigit() }
        _uiState.value = _uiState.value.copy(amountText = digitsOnly, amountError = null, saveError = null)
    }

    fun updateDate(value: LocalDate) {
        _uiState.value = _uiState.value.copy(date = value)
        viewModelScope.launch { refreshMaxAllowed() }
    }

    fun startVoice() = voiceController.startListening()

    fun consumeVoiceResult(text: String) {
        val parsed = VoiceParser.parse(text)
        _uiState.value = _uiState.value.copy(
            description = parsed.description.ifBlank { _uiState.value.description },
            amountText = parsed.amount?.toString() ?: _uiState.value.amountText,
            expenseCategory = parsed.category ?: _uiState.value.expenseCategory
        )
        voiceController.reset()
    }

    fun save() {
        val state = _uiState.value
        val amount = state.amountText.toLongOrNull()
        if (state.description.isBlank()) {
            _uiState.value = state.copy(descriptionError = "La descripción no puede estar vacía")
            return
        }
        if (amount == null || amount <= 0) {
            _uiState.value = state.copy(amountError = "Ingresa un monto mayor a cero")
            return
        }

        viewModelScope.launch {
            if (state.type == EntryType.INGRESO) {
                repository.addIncome(state.description, state.incomeCategory.ifBlank { "Otro" }, amount, state.date)
                _uiState.value = _uiState.value.copy(saved = true)
                return@launch
            }

            val result = if (state.isEditing) {
                repository.updateExpense(editId!!, state.description, state.expenseCategory.name, amount, state.date)
            } else {
                repository.addExpense(state.description, state.expenseCategory.name, amount, state.date)
            }

            when (result) {
                is AddExpenseResult.Rejected -> {
                    _uiState.value = _uiState.value.copy(saveError = result.reason)
                }
                is AddExpenseResult.Ok -> {
                    checkAndNotifyCritical(state.date)
                    _uiState.value = _uiState.value.copy(saved = true)
                }
            }
        }
    }

    private suspend fun checkAndNotifyCritical(date: LocalDate) {
        val (totalIncome, totalExpenses) = repository.getMonthTotals(YearMonth.from(date))
        val balance = BudgetCalculator.evaluate(totalIncome, totalExpenses)
        if (balance.status == BalanceStatus.CRITICO) {
            val settings = settingsDataStore.settings.first()
            NotificationHelper.fireCriticalAlert(appContext, settings.activeMonthLabel, balance.balance)
        }
    }

    override fun onCleared() {
        voiceController.destroy()
    }

    companion object {
        fun factory(application: AppControlFinancieroApplication, type: EntryType, editId: Long?) = viewModelFactory {
            initializer {
                AddEntryViewModel(application, application.repository, application.settingsDataStore, type, editId)
            }
        }
    }
}
