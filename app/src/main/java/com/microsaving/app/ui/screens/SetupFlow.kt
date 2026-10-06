package com.microsaving.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.microsaving.app.data.AppState
import com.microsaving.app.data.FixedExpense
import com.microsaving.app.data.Goal
import com.microsaving.app.logic.BudgetCalculator
import com.microsaving.app.ui.formatMoney
import com.microsaving.app.ui.parseAmount
import java.time.LocalDate

private data class GoalPreset(val emoji: String, val name: String)

private val presets = listOf(
    GoalPreset("✈️", "Trip to Japan"),
    GoalPreset("📷", "New camera"),
    GoalPreset("💻", "Laptop"),
    GoalPreset("🚲", "Bicycle"),
    GoalPreset("🎸", "Guitar"),
    GoalPreset("🏖️", "Summer holiday"),
    GoalPreset("🎓", "Course"),
    GoalPreset("🎯", "Something else"),
)

private val currencies = listOf("$", "€", "£", "₺", "¥")

/** Two-step onboarding: 1) choose a goal, 2) enter income and fixed expenses. */
@Composable
fun SetupFlow(
    initial: AppState,
    isEditing: Boolean,
    onCancel: () -> Unit,
    onDone: (Goal, String, Double, List<FixedExpense>) -> Unit,
) {
    var step by rememberSaveable { mutableStateOf(1) }

    var emoji by rememberSaveable { mutableStateOf(initial.goal?.emoji ?: "✈️") }
    var goalName by rememberSaveable { mutableStateOf(initial.goal?.name ?: "") }
    var target by rememberSaveable { mutableStateOf(initial.goal?.targetAmount?.let(::plain) ?: "") }
    var currency by rememberSaveable { mutableStateOf(initial.currency) }
    var income by rememberSaveable { mutableStateOf(initial.monthlyIncome.takeIf { it > 0 }?.let(::plain) ?: "") }
    val fixed = remember {
        mutableStateListOf<Pair<String, String>>().apply {
            if (initial.fixedExpenses.isEmpty()) add("Rent" to "")
            else initial.fixedExpenses.forEach { add(it.name to plain(it.amount)) }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            if (step == 1) "⛰️ Pick your summit" else "🎒 Pack your budget",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            if (step == 1) "What are you saving for? Reaching it means reaching the top of the mountain."
            else "Tell us what comes in and what must go out each month. The rest becomes your daily limit.",
            style = MaterialTheme.typography.bodyLarge,
        )
        Text("Step $step of 2", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)

        if (step == 1) {
            GoalStep(
                emoji = emoji,
                goalName = goalName,
                target = target,
                currency = currency,
                onPreset = { p ->
                    emoji = p.emoji
                    if (goalName.isBlank() || presets.any { it.name == goalName }) {
                        goalName = if (p.name == "Something else") "" else p.name
                    }
                },
                onGoalName = { goalName = it },
                onTarget = { target = it },
                onCurrency = { currency = it },
            )
            val canContinue = goalName.isNotBlank() && parseAmount(target) != null
            Button(onClick = { step = 2 }, enabled = canContinue, modifier = Modifier.fillMaxWidth()) {
                Text("Next")
            }
            if (isEditing) {
                TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) { Text("Cancel") }
            }
        } else {
            BudgetStep(currency = currency, income = income, onIncome = { income = it }, fixed = fixed)

            val incomeValue = parseAmount(income)
            val fixedList = fixed.mapNotNull { (n, a) ->
                parseAmount(a)?.let { FixedExpense(n.ifBlank { "Expense" }, it) }
            }
            val preview = AppState(monthlyIncome = incomeValue ?: 0.0, fixedExpenses = fixedList)
            val free = BudgetCalculator.monthlyFreeMoney(preview)
            val daily = BudgetCalculator.dailyLimit(preview, LocalDate.now())

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Your daily limit", style = MaterialTheme.typography.titleMedium)
                    Text(
                        formatMoney(currency, daily),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        "${formatMoney(currency, free)} free this month. Every bit you don't spend moves you up the mountain.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { step = 1 }, modifier = Modifier.weight(1f)) { Text("Back") }
                Button(
                    onClick = {
                        onDone(Goal(goalName.trim(), emoji, parseAmount(target)!!), currency, incomeValue!!, fixedList)
                    },
                    enabled = incomeValue != null && free > 0,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(if (isEditing) "Save" else "Start climbing")
                }
            }
            if (incomeValue != null && free <= 0) {
                Text(
                    "Your fixed expenses use up all of your income, so there is nothing left to spend or save.",
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun GoalStep(
    emoji: String,
    goalName: String,
    target: String,
    currency: String,
    onPreset: (GoalPreset) -> Unit,
    onGoalName: (String) -> Unit,
    onTarget: (String) -> Unit,
    onCurrency: (String) -> Unit,
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        presets.forEach { p ->
            FilterChip(
                selected = emoji == p.emoji,
                onClick = { onPreset(p) },
                label = { Text("${p.emoji} ${p.name}") },
            )
        }
    }
    OutlinedTextField(
        value = goalName,
        onValueChange = onGoalName,
        label = { Text("Goal name") },
        leadingIcon = { Text(emoji, fontSize = 22.sp) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    Text("Currency", style = MaterialTheme.typography.titleSmall)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        currencies.forEach { c ->
            FilterChip(selected = currency == c, onClick = { onCurrency(c) }, label = { Text(c, fontSize = 18.sp) })
        }
    }
    MoneyField(value = target, onValueChange = onTarget, label = "How much does it cost?", currency = currency)
}

@Composable
private fun BudgetStep(
    currency: String,
    income: String,
    onIncome: (String) -> Unit,
    fixed: MutableList<Pair<String, String>>,
) {
    MoneyField(value = income, onValueChange = onIncome, label = "Monthly income", currency = currency)
    Text("Fixed monthly expenses", style = MaterialTheme.typography.titleMedium)
    Text("Rent, bills, subscriptions, transport pass…", style = MaterialTheme.typography.bodyMedium)
    fixed.forEachIndexed { index, (name, amount) ->
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = name,
                onValueChange = { fixed[index] = it to amount },
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.weight(1.2f),
            )
            Spacer(Modifier.width(8.dp))
            MoneyField(
                value = amount,
                onValueChange = { fixed[index] = name to it },
                label = "Amount",
                currency = currency,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = { fixed.removeAt(index) }) {
                Icon(Icons.Default.Delete, contentDescription = "Remove")
            }
        }
    }
    OutlinedButton(onClick = { fixed.add("" to "") }) {
        Icon(Icons.Default.Add, contentDescription = null)
        Spacer(Modifier.width(4.dp))
        Text("Add fixed expense")
    }
    Spacer(Modifier.height(4.dp))
}

@Composable
fun MoneyField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    currency: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
) {
    OutlinedTextField(
        value = value,
        onValueChange = { new -> onValueChange(new.filter { it.isDigit() || it == '.' || it == ',' }) },
        label = { Text(label) },
        prefix = { Text(currency) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier,
    )
}

/** Formats a stored number for an input field without trailing ".0". */
private fun plain(value: Double): String =
    if (value % 1.0 == 0.0) value.toLong().toString() else value.toString()
