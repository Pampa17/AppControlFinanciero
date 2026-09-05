package com.jpdev.appcontrolfinanciero.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class BudgetCalculatorTest {

    @Test
    fun `no income yet is critico with zero allowance`() {
        val state = BudgetCalculator.evaluate(totalIncome = 0, totalExpenses = 0)
        assertEquals(BalanceStatus.CRITICO, state.status)
        assertEquals(0L, BudgetCalculator.maxAllowedExpense(0, 0))
    }

    @Test
    fun `balance above 30 percent of income is normal`() {
        // income 100_000, balance 40_000 (40%) -> normal
        val state = BudgetCalculator.evaluate(totalIncome = 100_000, totalExpenses = 60_000)
        assertEquals(BalanceStatus.NORMAL, state.status)
    }

    @Test
    fun `balance at exactly 30 percent triggers precaucion`() {
        val state = BudgetCalculator.evaluate(totalIncome = 100_000, totalExpenses = 70_000)
        assertEquals(BalanceStatus.PRECAUCION, state.status)
    }

    @Test
    fun `balance at exactly 10 percent triggers critico`() {
        val state = BudgetCalculator.evaluate(totalIncome = 100_000, totalExpenses = 90_000)
        assertEquals(BalanceStatus.CRITICO, state.status)
    }

    @Test
    fun `max allowed expense never goes negative`() {
        assertEquals(0L, BudgetCalculator.maxAllowedExpense(totalIncome = 50_000, totalExpensesSoFar = 50_000))
        assertEquals(10_000L, BudgetCalculator.maxAllowedExpense(totalIncome = 50_000, totalExpensesSoFar = 40_000))
    }
}
