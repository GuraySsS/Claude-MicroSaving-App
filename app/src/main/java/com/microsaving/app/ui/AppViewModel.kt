package com.microsaving.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.microsaving.app.data.AppState
import com.microsaving.app.data.Expense
import com.microsaving.app.data.FixedExpense
import com.microsaving.app.data.Goal
import com.microsaving.app.data.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import kotlin.random.Random

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = Repository(application)

    private val _state = MutableStateFlow(repository.load())
    val state: StateFlow<AppState> = _state.asStateFlow()

    private val _today = MutableStateFlow(LocalDate.now())
    val today: StateFlow<LocalDate> = _today.asStateFlow()

    /** Called when the app comes back to the foreground so a new day is picked up. */
    fun refreshToday() {
        _today.value = LocalDate.now()
    }

    private fun mutate(block: (AppState) -> AppState) {
        _state.update(block)
        repository.save(_state.value)
    }

    fun saveSetup(goal: Goal, currency: String, income: Double, fixed: List<FixedExpense>) = mutate {
        it.copy(
            goal = goal,
            currency = currency,
            monthlyIncome = income,
            fixedExpenses = fixed,
            startDate = it.startDate ?: _today.value,
        )
    }

    fun addExpense(amount: Double, note: String) = mutate {
        val expense = Expense(System.currentTimeMillis(), _today.value, amount, note.trim())
        it.copy(expenses = it.expenses + expense)
    }

    fun deleteExpense(id: Long) = mutate { s -> s.copy(expenses = s.expenses.filterNot { it.id == id }) }

    fun resetAll() = mutate { AppState() }

    /**
     * Moves the start date back three weeks and fills those days with random spending,
     * so the mountain can be tried out without waiting for real days to pass.
     */
    fun loadDemoData() = mutate { s ->
        val today = _today.value
        val start = today.minusDays(21)
        val daily = if (s.monthlyIncome > s.fixedTotal) (s.monthlyIncome - s.fixedTotal) / 30 else 20.0
        val random = Random(42)
        val notes = listOf("Coffee", "Lunch", "Groceries", "Bus ticket", "Snacks", "Dinner out")
        val demo = (0 until 21).flatMap { offset ->
            val date = start.plusDays(offset.toLong())
            // Most days come in under budget, a few go over.
            val factor = if (random.nextInt(10) < 8) random.nextDouble(0.2, 0.9) else random.nextDouble(1.05, 1.5)
            val total = daily * factor
            val parts = random.nextInt(1, 4)
            (0 until parts).map { p ->
                Expense(
                    id = date.toEpochDay() * 10 + p,
                    date = date,
                    amount = Math.round(total / parts * 100) / 100.0,
                    note = notes[random.nextInt(notes.size)],
                )
            }
        }
        s.copy(startDate = start, expenses = s.expenses.filter { it.date >= today } + demo)
    }
}
