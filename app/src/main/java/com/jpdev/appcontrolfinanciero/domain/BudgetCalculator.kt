package com.jpdev.appcontrolfinanciero.domain

enum class BalanceStatus { NORMAL, PRECAUCION, CRITICO }

data class BalanceState(
    val totalIncome: Long,
    val totalExpenses: Long,
    val balance: Long,
    val status: BalanceStatus
)

/**
 * Single source of truth for the exam's budget rules. Dashboard, the Agregar form and
 * FinanceRepository's insert guard all route through this — the threshold math never gets
 * duplicated or drifts between screens.
 */
object BudgetCalculator {

    fun evaluate(totalIncome: Long, totalExpenses: Long): BalanceState {
        val balance = totalIncome - totalExpenses
        val status = when {
            totalIncome <= 0L -> BalanceStatus.CRITICO
            balance * 100 <= totalIncome * 10 -> BalanceStatus.CRITICO
            balance * 100 <= totalIncome * 30 -> BalanceStatus.PRECAUCION
            else -> BalanceStatus.NORMAL
        }
        return BalanceState(totalIncome, totalExpenses, balance, status)
    }

    /** Hard ceiling for a new expense this month. Floored at 0 — never negative. */
    fun maxAllowedExpense(totalIncome: Long, totalExpensesSoFar: Long): Long {
        val remaining = totalIncome - totalExpensesSoFar
        return if (remaining > 0L) remaining else 0L
    }
}
