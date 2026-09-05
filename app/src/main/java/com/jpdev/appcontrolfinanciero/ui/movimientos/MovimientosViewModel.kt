package com.jpdev.appcontrolfinanciero.ui.movimientos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.jpdev.appcontrolfinanciero.AppControlFinancieroApplication
import com.jpdev.appcontrolfinanciero.data.FinanceRepository
import com.jpdev.appcontrolfinanciero.data.local.ExpenseEntity
import com.jpdev.appcontrolfinanciero.data.local.IncomeEntity
import com.jpdev.appcontrolfinanciero.domain.EntryType
import java.time.YearMonth
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MovimientosUiState(
    val incomes: List<IncomeEntity> = emptyList(),
    val expenses: List<ExpenseEntity> = emptyList(),
    val incomeCategoryNames: Map<Long, String> = emptyMap(),
    val expenseCategoryNames: Map<Long, String> = emptyMap()
)

class MovimientosViewModel(private val repository: FinanceRepository) : ViewModel() {

    val uiState: StateFlow<MovimientosUiState> = combine(
        repository.observeMonthSummary(YearMonth.now()),
        repository.observeCategories(EntryType.INGRESO),
        repository.observeCategories(EntryType.EGRESO)
    ) { summary, incomeCategories, expenseCategories ->
        MovimientosUiState(
            incomes = summary.incomes,
            expenses = summary.expenses,
            incomeCategoryNames = incomeCategories.associate { it.id to it.name },
            expenseCategoryNames = expenseCategories.associate { it.id to it.name }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MovimientosUiState())

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch { repository.deleteExpense(expense) }
    }

    companion object {
        fun factory(application: AppControlFinancieroApplication) = viewModelFactory {
            initializer { MovimientosViewModel(application.repository) }
        }
    }
}
