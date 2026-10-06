package com.microsaving.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.microsaving.app.data.AppState
import com.microsaving.app.logic.BudgetCalculator
import com.microsaving.app.ui.components.MountainScene
import com.microsaving.app.ui.formatMoney
import com.microsaving.app.ui.parseAmount
import com.microsaving.app.ui.theme.DeepLeaf
import com.microsaving.app.ui.theme.Overspent
import java.time.LocalDate

@Composable
fun HomeScreen(
    state: AppState,
    today: LocalDate,
    onAddExpense: (Double, String) -> Unit,
    onDeleteExpense: (Long) -> Unit,
) {
    val goal = state.goal ?: return
    val summary = remember(state, today) { BudgetCalculator.summarize(state, today) }
    val todaysExpenses = remember(state, today) { state.expenses.filter { it.date == today }.sortedByDescending { it.id } }
    var showAdd by rememberSaveable { mutableStateOf(false) }
    val money = { v: Double -> formatMoney(state.currency, v) }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text("Climbing to", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Text("${goal.emoji} ${goal.name}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
            item {
                MountainScene(
                    progress = summary.progress,
                    projectedProgress = summary.projectedProgress,
                    trees = summary.successfulDays,
                    goalEmoji = goal.emoji,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .clip(RoundedCornerShape(28.dp)),
                )
            }
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(money(summary.totalSaved), fontSize = 26.sp, fontWeight = FontWeight.Bold, color = DeepLeaf)
                            Text("  of ${money(goal.targetAmount)}", style = MaterialTheme.typography.bodyLarge)
                        }
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { summary.progress },
                            modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            when {
                                summary.goalReached -> "🎉 You reached the summit! Time to enjoy your ${goal.name}."
                                summary.daysToGoal != null -> "${"%.1f".format(summary.progress * 100)}% climbed · about ${summary.daysToGoal} days to the top at this pace"
                                else -> "${"%.1f".format(summary.progress * 100)}% climbed"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard("🔥", "${summary.streak}", "day streak", Modifier.weight(1f))
                    StatCard("🌲", "${summary.successfulDays}", "trees planted", Modifier.weight(1f))
                }
            }
            item { TodayCard(summary.dailyLimitToday, summary.spentToday, summary.leftToday, money) }
            if (todaysExpenses.isNotEmpty()) {
                item { Text("Today's spending", style = MaterialTheme.typography.titleMedium) }
                items(todaysExpenses, key = { it.id }) { e ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Row(Modifier.padding(start = 16.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(e.note.ifBlank { "Spending" }, Modifier.weight(1f))
                            Text(money(e.amount), fontWeight = FontWeight.SemiBold)
                            IconButton(onClick = { onDeleteExpense(e.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                }
            } else {
                item {
                    Text(
                        "No spending logged today. Tap the button whenever you buy something.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    )
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = { showAdd = true },
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            text = { Text("Add spending") },
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        )
    }

    if (showAdd) {
        AddExpenseDialog(
            currency = state.currency,
            onDismiss = { showAdd = false },
            onAdd = { amount, note ->
                onAddExpense(amount, note)
                showAdd = false
            },
        )
    }
}

@Composable
private fun StatCard(emoji: String, value: String, label: String, modifier: Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 28.sp)
            Column(Modifier.padding(start = 12.dp)) {
                Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(label, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun TodayCard(limit: Double, spent: Double, left: Double, money: (Double) -> String) {
    val over = left < 0
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (over) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Today", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Row {
                Figure("Daily limit", money(limit), Modifier.weight(1f))
                Figure("Spent", money(spent), Modifier.weight(1f))
                Figure(
                    if (over) "Over by" else "Left",
                    money(kotlin.math.abs(left)),
                    Modifier.weight(1f),
                    color = if (over) Overspent else DeepLeaf,
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                if (over) "You went over today. The climber will slip back a little tonight — tomorrow is a new chance!"
                else "Keep it up! Whatever is left at midnight is saved and you climb higher.",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun Figure(label: String, value: String, modifier: Modifier, color: Color = MaterialTheme.colorScheme.onSurface) {
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Text(value, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun AddExpenseDialog(currency: String, onDismiss: () -> Unit, onAdd: (Double, String) -> Unit) {
    var amount by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    val parsed = parseAmount(amount)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("What did you spend?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MoneyField(value = amount, onValueChange = { amount = it }, label = "Amount", currency = currency)
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("What for? (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { parsed?.let { onAdd(it, note) } }, enabled = parsed != null) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
