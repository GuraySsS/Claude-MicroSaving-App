package com.microsaving.app.logic

import com.microsaving.app.data.AppState
import com.microsaving.app.data.Expense
import com.microsaving.app.data.FixedExpense
import com.microsaving.app.data.Goal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class BudgetCalculatorTest {
    // June has 30 days: (3500 - 500) / 30 = 100 per day.
    private val start = LocalDate.of(2026, 6, 1)
    private val base = AppState(
        goal = Goal("Camera", "📷", 1000.0),
        monthlyIncome = 3500.0,
        fixedExpenses = listOf(FixedExpense("Rent", 500.0)),
        startDate = start,
    )

    private fun spend(day: Int, amount: Double, id: Long = day.toLong()) =
        Expense(id, start.plusDays(day.toLong()), amount, "")

    @Test
    fun dailyLimitSpreadsFreeMoneyOverTheMonth() {
        assertEquals(100.0, BudgetCalculator.dailyLimit(base, start), 1e-9)
        // July has 31 days.
        assertEquals(3000.0 / 31, BudgetCalculator.dailyLimit(base, LocalDate.of(2026, 7, 1)), 1e-9)
    }

    @Test
    fun onlyCompletedDaysCountTowardsSavings() {
        val state = base.copy(expenses = listOf(spend(0, 40.0), spend(1, 70.0), spend(2, 10.0)))
        val summary = BudgetCalculator.summarize(state, start.plusDays(2))
        assertEquals(2, summary.completedDays.size)
        assertEquals(60.0 + 30.0, summary.totalSaved, 1e-9)
        // Today is still running: 90 left today would bring the projection to 180.
        assertEquals(180.0, summary.projectedSaved, 1e-9)
        assertEquals(0.09f, summary.progress, 1e-6f)
    }

    @Test
    fun overspendingSlidesBackAndBreaksStreak() {
        val state = base.copy(expenses = listOf(spend(0, 20.0), spend(1, 150.0), spend(2, 50.0), spend(3, 90.0)))
        val summary = BudgetCalculator.summarize(state, start.plusDays(4))
        assertEquals(80.0 - 50.0 + 50.0 + 10.0, summary.totalSaved, 1e-9)
        assertEquals(2, summary.streak)
        assertEquals(3, summary.successfulDays)
    }

    @Test
    fun savingsNeverGoNegative() {
        val state = base.copy(expenses = listOf(spend(0, 500.0)))
        val summary = BudgetCalculator.summarize(state, start.plusDays(1))
        assertEquals(0.0, summary.totalSaved, 1e-9)
        assertEquals(0, summary.streak)
        assertNull(summary.daysToGoal)
    }

    @Test
    fun daysWithoutSpendingSaveTheWholeLimit() {
        val summary = BudgetCalculator.summarize(base, start.plusDays(3))
        assertEquals(300.0, summary.totalSaved, 1e-9)
        assertEquals(3, summary.streak)
        // 700 left at 100 per day.
        assertEquals(7, summary.daysToGoal)
    }

    @Test
    fun goalReachedCapsProgress() {
        val summary = BudgetCalculator.summarize(base, start.plusDays(15))
        assertTrue(summary.goalReached)
        assertEquals(1f, summary.progress, 0f)
        assertEquals(0, summary.daysToGoal)
    }
}
