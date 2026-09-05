package com.jpdev.appcontrolfinanciero.ui.movimientos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.jpdev.appcontrolfinanciero.AppControlFinancieroApplication
import com.jpdev.appcontrolfinanciero.data.FinanceRepository
import com.jpdev.appcontrolfinanciero.data.local.ExpenseEntity
import com.jpdev.appcontrolfinanciero.data.local.IncomeEntity
import java.time.YearMonth
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MovimientosUiState(
    val incomes: List<IncomeEntity> = emptyList(),
    val expenses: List<ExpenseEntity> = emptyList()
)

class MovimientosViewModel(private val repository: FinanceRepository) : ViewModel() {

    val uiState: StateFlow<MovimientosUiState> = repository.observeMonthSummary(YearMonth.now())
        .map { summary -> MovimientosUiState(summary.incomes, summary.expenses) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MovimientosUiState())

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch { repository.deleteExpense(expense) }
    }

    companion object {
        fun factory(application: AppControlFinancieroApplication) = viewModelFactory {
            initializer { MovimientosViewModel(application.repository) }
        }
    }
}
