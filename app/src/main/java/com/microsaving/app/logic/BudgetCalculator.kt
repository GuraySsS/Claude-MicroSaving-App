package com.microsaving.app.logic

import com.microsaving.app.data.AppState
import java.time.LocalDate
import kotlin.math.ceil
import kotlin.math.max

data class DayResult(
    val date: LocalDate,
    val limit: Double,
    val spent: Double,
) {
    /** Positive when the user stayed under budget, negative when they overspent. */
    val saved: Double get() = limit - spent
    val underBudget: Boolean get() = spent <= limit
}

data class Summary(
    val dailyLimitToday: Double,
    val spentToday: Double,
    /** Days from the start date up to (not including) today. */
    val completedDays: List<DayResult>,
    /** Money saved on completed days. Never below zero. */
    val totalSaved: Double,
    /** Where the user would be if today ended right now. */
    val projectedSaved: Double,
    val target: Double,
    val streak: Int,
    val successfulDays: Int,
    /** Estimated days left to reach the goal, or null if no estimate is possible yet. */
    val daysToGoal: Int?,
) {
    val leftToday: Double get() = dailyLimitToday - spentToday
    val progress: Float get() = fraction(totalSaved)
    val projectedProgress: Float get() = fraction(projectedSaved)
    val goalReached: Boolean get() = target > 0 && totalSaved >= target

    private fun fraction(amount: Double): Float =
        if (target <= 0) 0f else (amount / target).coerceIn(0.0, 1.0).toFloat()
}

object BudgetCalculator {

    /** Money left each month after fixed expenses. */
    fun monthlyFreeMoney(state: AppState): Double = max(0.0, state.monthlyIncome - state.fixedTotal)

    /** Free money spread evenly over the days of the month that [date] is in. */
    fun dailyLimit(state: AppState, date: LocalDate): Double =
        monthlyFreeMoney(state) / date.lengthOfMonth()

    fun spentOn(state: AppState, date: LocalDate): Double =
        state.expenses.filter { it.date == date }.sumOf { it.amount }

    fun summarize(state: AppState, today: LocalDate): Summary {
        val target = state.goal?.targetAmount ?: 0.0
        val start = state.startDate ?: today
        val spentByDate = state.expenses.groupBy { it.date }.mapValues { (_, list) -> list.sumOf { it.amount } }

        val completed = mutableListOf<DayResult>()
        var day = start
        while (day.isBefore(today)) {
            completed += DayResult(day, dailyLimit(state, day), spentByDate[day] ?: 0.0)
            day = day.plusDays(1)
        }

        val totalSaved = max(0.0, completed.sumOf { it.saved })
        val limitToday = dailyLimit(state, today)
        val spentToday = spentByDate[today] ?: 0.0
        val projected = max(0.0, totalSaved + (limitToday - spentToday))

        var streak = 0
        for (result in completed.asReversed()) {
            if (result.underBudget) streak++ else break
        }

        val daysToGoal = estimateDaysToGoal(completed, totalSaved, target, limitToday)

        return Summary(
            dailyLimitToday = limitToday,
            spentToday = spentToday,
            completedDays = completed,
            totalSaved = totalSaved,
            projectedSaved = projected,
            target = target,
            streak = streak,
            successfulDays = completed.count { it.underBudget },
            daysToGoal = daysToGoal,
        )
    }

    /**
     * Uses the average daily saving so far. Before any day is completed we assume the user
     * saves half of their daily limit, which gives a friendly first estimate.
     */
    private fun estimateDaysToGoal(
        completed: List<DayResult>,
        totalSaved: Double,
        target: Double,
        limitToday: Double,
    ): Int? {
        if (target <= 0) return null
        val remaining = target - totalSaved
        if (remaining <= 0) return 0
        val perDay = if (completed.isEmpty()) limitToday / 2 else completed.sumOf { it.saved } / completed.size
        if (perDay <= 0) return null
        return ceil(remaining / perDay).toInt()
    }
}
