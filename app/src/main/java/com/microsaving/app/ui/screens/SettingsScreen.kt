package com.microsaving.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.microsaving.app.data.AppState
import com.microsaving.app.logic.BudgetCalculator
import com.microsaving.app.ui.formatMoney
import java.time.LocalDate

@Composable
fun SettingsScreen(
    state: AppState,
    onEditPlan: () -> Unit,
    onLoadDemo: () -> Unit,
    onReset: () -> Unit,
) {
    var confirmReset by rememberSaveable { mutableStateOf(false) }
    var confirmDemo by rememberSaveable { mutableStateOf(false) }
    val money = { v: Double -> formatMoney(state.currency, v) }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Your plan", style = MaterialTheme.typography.titleMedium)
                state.goal?.let { Text("Goal: ${it.emoji} ${it.name} — ${money(it.targetAmount)}") }
                Text("Monthly income: ${money(state.monthlyIncome)}")
                state.fixedExpenses.forEach { Text("• ${it.name}: ${money(it.amount)}") }
                Text("Daily limit today: ${money(BudgetCalculator.dailyLimit(state, LocalDate.now()))}", fontWeight = FontWeight.SemiBold)
                Button(onClick = onEditPlan, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) { Text("Edit goal & budget") }
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Try it out", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Fill the last 3 weeks with example spending so you can see the climber and trees move without waiting.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                OutlinedButton(onClick = { confirmDemo = true }, modifier = Modifier.fillMaxWidth()) { Text("Load demo data") }
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Start over", style = MaterialTheme.typography.titleMedium)
                Text("Deletes your goal, budget and all logged spending.", style = MaterialTheme.typography.bodyMedium)
                Button(
                    onClick = { confirmReset = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Reset everything") }
            }
        }
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("Reset everything?") },
            text = { Text("This cannot be undone.") },
            confirmButton = { TextButton(onClick = { confirmReset = false; onReset() }) { Text("Reset") } },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("Cancel") } },
        )
    }
    if (confirmDemo) {
        AlertDialog(
            onDismissRequest = { confirmDemo = false },
            title = { Text("Load demo data?") },
            text = { Text("This replaces any spending you logged before today with example data.") },
            confirmButton = { TextButton(onClick = { confirmDemo = false; onLoadDemo() }) { Text("Load") } },
            dismissButton = { TextButton(onClick = { confirmDemo = false }) { Text("Cancel") } },
        )
    }
}
