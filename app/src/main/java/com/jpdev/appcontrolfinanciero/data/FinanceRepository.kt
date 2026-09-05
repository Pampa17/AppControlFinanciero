package com.jpdev.appcontrolfinanciero.data

import com.jpdev.appcontrolfinanciero.data.local.CategoryDao
import com.jpdev.appcontrolfinanciero.data.local.CategoryEntity
import com.jpdev.appcontrolfinanciero.data.local.ExpenseDao
import com.jpdev.appcontrolfinanciero.data.local.ExpenseEntity
import com.jpdev.appcontrolfinanciero.data.local.IncomeDao
import com.jpdev.appcontrolfinanciero.data.local.IncomeEntity
import com.jpdev.appcontrolfinanciero.domain.BalanceState
import com.jpdev.appcontrolfinanciero.domain.BudgetCalculator
import com.jpdev.appcontrolfinanciero.domain.EntryType
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

data class MonthSummary(
    val incomes: List<IncomeEntity>,
    val expenses: List<ExpenseEntity>,
    val balance: BalanceState
)

data class CategoryTotal(
    val id: Long,
    val name: String,
    val total: Long,
    val isReserved: Boolean
)

sealed interface AddExpenseResult {
    data class Ok(val id: Long) : AddExpenseResult
    data class Rejected(val reason: String) : AddExpenseResult
}

/** Single repository for all three tables — one repo per entity would just be ceremony here. */
class FinanceRepository(
    private val incomeDao: IncomeDao,
    private val expenseDao: ExpenseDao,
    private val categoryDao: CategoryDao
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

    suspend fun addIncome(description: String, categoryId: Long, amount: Long, date: LocalDate): Long =
        incomeDao.insert(
            IncomeEntity(description = description, categoryId = categoryId, amount = amount, date = date.toEpochDay())
        )

    /** Re-validates against BudgetCalculator here too — the UI disables Save, but this is the real gate. */
    suspend fun addExpense(description: String, categoryId: Long, amount: Long, date: LocalDate): AddExpenseResult {
        val month = YearMonth.from(date)
        val (start, end) = rangeOf(month)
        val totalIncome = incomeDao.getByDateRange(start, end).sumOf { it.amount }
        val totalExpenses = expenseDao.getByDateRange(start, end).sumOf { it.amount }
        val maxAllowed = BudgetCalculator.maxAllowedExpense(totalIncome, totalExpenses)
        if (amount > maxAllowed) {
            return AddExpenseResult.Rejected("Monto máximo permitido: $$maxAllowed")
        }
        val id = expenseDao.insert(
            ExpenseEntity(description = description, categoryId = categoryId, amount = amount, date = date.toEpochDay())
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
    suspend fun updateExpense(id: Long, description: String, categoryId: Long, amount: Long, date: LocalDate): AddExpenseResult {
        val (start, end) = rangeOf(YearMonth.from(date))
        val totalIncome = incomeDao.getByDateRange(start, end).sumOf { it.amount }
        val otherExpenses = expenseDao.getByDateRange(start, end).filter { it.id != id }.sumOf { it.amount }
        val maxAllowed = BudgetCalculator.maxAllowedExpense(totalIncome, otherExpenses)
        if (amount > maxAllowed) {
            return AddExpenseResult.Rejected("Monto máximo permitido: $$maxAllowed")
        }
        expenseDao.update(
            ExpenseEntity(id = id, description = description, categoryId = categoryId, amount = amount, date = date.toEpochDay())
        )
        return AddExpenseResult.Ok(id)
    }

    suspend fun deleteExpense(expense: ExpenseEntity) = expenseDao.delete(expense)

    // --- Categories ---

    fun observeCategories(type: EntryType): Flow<List<CategoryEntity>> = categoryDao.observeByType(type.name)

    suspend fun getCategories(type: EntryType): List<CategoryEntity> = categoryDao.getByType(type.name)

    /** Reuses an existing category with the same name (case-insensitive) instead of duplicating it. */
    suspend fun addCategory(name: String, type: EntryType): Long {
        val trimmed = name.trim()
        val existing = categoryDao.getByType(type.name).firstOrNull { it.name.equals(trimmed, ignoreCase = true) }
        return existing?.id ?: categoryDao.insert(CategoryEntity(name = trimmed, type = type.name))
    }

    suspend fun renameCategory(category: CategoryEntity, newName: String) =
        categoryDao.update(category.copy(name = newName.trim()))

    suspend fun countMovementsUsing(category: CategoryEntity): Int =
        if (category.type == EntryType.INGRESO.name) incomeDao.countByCategory(category.id)
        else expenseDao.countByCategory(category.id)

    /** Reassigns any movements pointing at [category] to that type's reserved "Sin categoría" before deleting it. */
    suspend fun deleteCategoryReassigning(category: CategoryEntity) {
        val reserved = categoryDao.getByType(category.type).first { it.isReserved }
        if (category.type == EntryType.INGRESO.name) incomeDao.reassignCategory(category.id, reserved.id)
        else expenseDao.reassignCategory(category.id, reserved.id)
        categoryDao.delete(category)
    }

    fun observeCategoryTotals(type: EntryType, month: YearMonth): Flow<List<CategoryTotal>> {
        val (start, end) = rangeOf(month)
        val amountsByCategoryFlow = if (type == EntryType.INGRESO) {
            incomeDao.observeByDateRange(start, end).map { list -> list.map { it.categoryId to it.amount } }
        } else {
            expenseDao.observeByDateRange(start, end).map { list -> list.map { it.categoryId to it.amount } }
        }
        return combine(categoryDao.observeByType(type.name), amountsByCategoryFlow) { categories, amounts ->
            val totalsByCategoryId = amounts.groupBy({ it.first }, { it.second }).mapValues { it.value.sum() }
            categories
                .map { cat -> CategoryTotal(cat.id, cat.name, totalsByCategoryId[cat.id] ?: 0L, cat.isReserved) }
                .sortedByDescending { it.total }
        }
    }
}
