package com.jpdev.appcontrolfinanciero.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.jpdev.appcontrolfinanciero.AppControlFinancieroApplication
import com.jpdev.appcontrolfinanciero.data.FinanceRepository
import com.jpdev.appcontrolfinanciero.data.local.ExpenseEntity
import com.jpdev.appcontrolfinanciero.data.local.IncomeEntity
import com.jpdev.appcontrolfinanciero.data.prefs.SettingsDataStore
import com.jpdev.appcontrolfinanciero.domain.BalanceStatus
import java.time.YearMonth
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class DashboardUiState(
    val alias: String = "",
    val monthLabel: String = "",
    val monthEmoji: String = "",
    val totalIncome: Long = 0,
    val totalExpenses: Long = 0,
    val balance: Long = 0,
    val status: BalanceStatus = BalanceStatus.NORMAL,
    val recentIncomes: List<IncomeEntity> = emptyList(),
    val recentExpenses: List<ExpenseEntity> = emptyList()
)

class DashboardViewModel(
    repository: FinanceRepository,
    settingsDataStore: SettingsDataStore
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.observeMonthSummary(YearMonth.now()),
        settingsDataStore.settings
    ) { summary, settings ->
        DashboardUiState(
            alias = settings.userAlias,
            monthLabel = settings.activeMonthLabel,
            monthEmoji = settings.activeMonthEmoji,
            totalIncome = summary.balance.totalIncome,
            totalExpenses = summary.balance.totalExpenses,
            balance = summary.balance.balance,
            status = summary.balance.status,
            recentIncomes = summary.incomes.take(5),
            recentExpenses = summary.expenses.take(5)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())

    companion object {
        fun factory(application: AppControlFinancieroApplication) = viewModelFactory {
            initializer { DashboardViewModel(application.repository, application.settingsDataStore) }
        }
    }
}
