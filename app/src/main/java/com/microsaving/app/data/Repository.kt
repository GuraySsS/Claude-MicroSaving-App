package com.microsaving.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

/** Stores the whole app state as one JSON document in SharedPreferences. */
class Repository(context: Context) {
    private val prefs = context.getSharedPreferences("summit_saver", Context.MODE_PRIVATE)

    fun load(): AppState {
        val raw = prefs.getString(KEY_STATE, null) ?: return AppState()
        return try {
            fromJson(JSONObject(raw))
        } catch (e: Exception) {
            AppState()
        }
    }

    fun save(state: AppState) {
        prefs.edit().putString(KEY_STATE, toJson(state).toString()).apply()
    }

    private fun toJson(state: AppState): JSONObject = JSONObject().apply {
        state.goal?.let {
            put("goal", JSONObject().apply {
                put("name", it.name)
                put("emoji", it.emoji)
                put("target", it.targetAmount)
            })
        }
        put("currency", state.currency)
        put("income", state.monthlyIncome)
        put("fixed", JSONArray().apply {
            state.fixedExpenses.forEach {
                put(JSONObject().put("name", it.name).put("amount", it.amount))
            }
        })
        state.startDate?.let { put("startDate", it.toString()) }
        put("expenses", JSONArray().apply {
            state.expenses.forEach {
                put(
                    JSONObject()
                        .put("id", it.id)
                        .put("date", it.date.toString())
                        .put("amount", it.amount)
                        .put("note", it.note)
                )
            }
        })
    }

    private fun fromJson(json: JSONObject): AppState {
        val goal = json.optJSONObject("goal")?.let {
            Goal(it.getString("name"), it.optString("emoji", "🎯"), it.getDouble("target"))
        }
        val fixed = json.optJSONArray("fixed")?.let { arr ->
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                FixedExpense(o.getString("name"), o.getDouble("amount"))
            }
        } ?: emptyList()
        val expenses = json.optJSONArray("expenses")?.let { arr ->
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                Expense(
                    id = o.getLong("id"),
                    date = LocalDate.parse(o.getString("date")),
                    amount = o.getDouble("amount"),
                    note = o.optString("note", ""),
                )
            }
        } ?: emptyList()
        return AppState(
            goal = goal,
            currency = json.optString("currency", "$"),
            monthlyIncome = json.optDouble("income", 0.0),
            fixedExpenses = fixed,
            startDate = json.optString("startDate", "").takeIf { it.isNotEmpty() }?.let(LocalDate::parse),
            expenses = expenses,
        )
    }

    private companion object {
        const val KEY_STATE = "state"
    }
}
