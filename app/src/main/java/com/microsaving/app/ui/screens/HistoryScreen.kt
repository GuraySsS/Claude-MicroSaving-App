package com.microsaving.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.microsaving.app.data.AppState
import com.microsaving.app.logic.BudgetCalculator
import com.microsaving.app.ui.formatDay
import com.microsaving.app.ui.formatMoney
import com.microsaving.app.ui.theme.DeepLeaf
import com.microsaving.app.ui.theme.Overspent
import java.time.LocalDate

@Composable
fun HistoryScreen(state: AppState, today: LocalDate) {
    val summary = remember(state, today) { BudgetCalculator.summarize(state, today) }
    val days = remember(summary) { summary.completedDays.asReversed() }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Text("Your climb so far", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                "🌲 ${summary.successfulDays} days under budget · 🪨 ${days.size - summary.successfulDays} days over",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }
        if (days.isEmpty()) {
            item {
                Text(
                    "Your first day is still in progress. Come back tomorrow to see how it went!",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
        items(days, key = { it.date.toEpochDay() }) { day ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(if (day.underBudget) "🌲" else "🪨", fontSize = 26.sp)
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text(formatDay(day.date), fontWeight = FontWeight.SemiBold)
                        Text(
                            "Spent ${formatMoney(state.currency, day.spent)} of ${formatMoney(state.currency, day.limit)}",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Text(
                        (if (day.saved >= 0) "+" else "") + formatMoney(state.currency, day.saved),
                        fontWeight = FontWeight.Bold,
                        color = if (day.underBudget) DeepLeaf else Overspent,
                    )
                }
            }
        }
    }
}
