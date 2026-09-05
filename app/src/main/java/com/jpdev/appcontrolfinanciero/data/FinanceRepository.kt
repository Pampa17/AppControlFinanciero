package com.jpdev.appcontrolfinanciero.data

import com.jpdev.appcontrolfinanciero.data.local.ExpenseDao
import com.jpdev.appcontrolfinanciero.data.local.ExpenseEntity
import com.jpdev.appcontrolfinanciero.data.local.IncomeDao
import com.jpdev.appcontrolfinanciero.data.local.IncomeEntity
import com.jpdev.appcontrolfinanciero.domain.BalanceState
import com.jpdev.appcontrolfinanciero.domain.BudgetCalculator
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class MonthSummary(
    val incomes: List<IncomeEntity>,
    val expenses: List<ExpenseEntity>,
    val balance: BalanceState
)

sealed interface AddExpenseResult {
    data class Ok(val id: Long) : AddExpenseResult
    data class Rejected(val reason: String) : AddExpenseResult
}

/** Single repository for both entities — two DAOs don't need one repo each. */
class FinanceRepository(
    private val incomeDao: IncomeDao,
    private val expenseDao: ExpenseDao
) {
    private fun rangeOf(month: YearMonth): Pair<Long, Long> {
        val start = month.atDay(1).toEpochDay()
        val end = month.atEndOfMonth().toEpochDay()
        return start to end
    }

    fun observeMonthSummary(month: YearMonth): Flow<MonthSummary> {
        val (start, end) = rangeOf(month)
        return combine(
            incomeDao.observeByDateRange(start, end),
            expenseDao.observeByDateRange(start, end)
        ) { incomes, expenses ->
            val totalIncome = incomes.sumOf { it.amount }
            val totalExpenses = expenses.sumOf { it.amount }
            MonthSummary(incomes, expenses, BudgetCalculator.evaluate(totalIncome, totalExpenses))
        }
    }

    suspend fun addIncome(description: String, category: String, amount: Long, date: LocalDate): Long =
        incomeDao.insert(
            IncomeEntity(description = description, category = category, amount = amount, date = date.toEpochDay())
        )

    /** Re-validates against BudgetCalculator here too — the UI disables Save, but this is the real gate. */
    suspend fun addExpense(description: String, category: String, amount: Long, date: LocalDate): AddExpenseResult {
        val month = YearMonth.from(date)
        val (start, end) = rangeOf(month)
        val totalIncome = incomeDao.getByDateRange(start, end).sumOf { it.amount }
        val totalExpenses = expenseDao.getByDateRange(start, end).sumOf { it.amount }
        val maxAllowed = BudgetCalculator.maxAllowedExpense(totalIncome, totalExpenses)
        if (amount > maxAllowed) {
            return AddExpenseResult.Rejected("Monto máximo permitido: $$maxAllowed")
        }
        val id = expenseDao.insert(
            ExpenseEntity(description = description, category = category, amount = amount, date = date.toEpochDay())
        )
        return AddExpenseResult.Ok(id)
    }

    suspend fun getExpenseById(id: Long): ExpenseEntity? = expenseDao.getById(id)

    suspend fun getMonthTotals(month: YearMonth): Pair<Long, Long> {
        val (start, end) = rangeOf(month)
        val totalIncome = incomeDao.getByDateRange(start, end).sumOf { it.amount }
        val totalExpenses = expenseDao.getByDateRange(start, end).sumOf { it.amount }
        return totalIncome to totalExpenses
    }

    /** Re-validates excluding the expense's own current amount (we're replacing it, not stacking it). */
    suspend fun updateExpense(id: Long, description: String, category: String, amount: Long, date: LocalDate): AddExpenseResult {
        val (start, end) = rangeOf(YearMonth.from(date))
        val totalIncome = incomeDao.getByDateRange(start, end).sumOf { it.amount }
        val otherExpenses = expenseDao.getByDateRange(start, end).filter { it.id != id }.sumOf { it.amount }
        val maxAllowed = BudgetCalculator.maxAllowedExpense(totalIncome, otherExpenses)
        if (amount > maxAllowed) {
            return AddExpenseResult.Rejected("Monto máximo permitido: $$maxAllowed")
        }
        expenseDao.update(
            ExpenseEntity(id = id, description = description, category = category, amount = amount, date = date.toEpochDay())
        )
        return AddExpenseResult.Ok(id)
    }

    suspend fun deleteExpense(expense: ExpenseEntity) = expenseDao.delete(expense)
}
