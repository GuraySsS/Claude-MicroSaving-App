package com.microsaving.app.data

import java.time.LocalDate

/** The thing the user is saving for, e.g. "Trip to Japan". */
data class Goal(
    val name: String,
    val emoji: String,
    val targetAmount: Double,
)

/** A recurring monthly cost such as rent or a phone bill. */
data class FixedExpense(
    val name: String,
    val amount: Double,
)

/** A single day-to-day purchase the user logs. */
data class Expense(
    val id: Long,
    val date: LocalDate,
    val amount: Double,
    val note: String,
)

data class AppState(
    val goal: Goal? = null,
    val currency: String = "$",
    val monthlyIncome: Double = 0.0,
    val fixedExpenses: List<FixedExpense> = emptyList(),
    /** The first day the user started climbing. Days before it are ignored. */
    val startDate: LocalDate? = null,
    val expenses: List<Expense> = emptyList(),
) {
    val isSetupComplete: Boolean get() = goal != null && startDate != null
    val fixedTotal: Double get() = fixedExpenses.sumOf { it.amount }
}
