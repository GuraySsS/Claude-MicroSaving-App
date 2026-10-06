package com.microsaving.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.microsaving.app.ui.AppViewModel
import com.microsaving.app.ui.screens.HistoryScreen
import com.microsaving.app.ui.screens.HomeScreen
import com.microsaving.app.ui.screens.SettingsScreen
import com.microsaving.app.ui.screens.SetupFlow
import com.microsaving.app.ui.theme.SummitSaverTheme

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SummitSaverTheme {
                App(viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshToday()
    }
}

private enum class Tab(val label: String) { Home("Climb"), History("History"), Settings("Settings") }

@Composable
private fun App(viewModel: AppViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val today by viewModel.today.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableStateOf(Tab.Home) }
    var editing by rememberSaveable { mutableStateOf(false) }

    if (!state.isSetupComplete || editing) {
        if (editing) BackHandler { editing = false }
        SetupFlow(
            initial = state,
            isEditing = editing,
            onCancel = { editing = false },
            onDone = { goal, currency, income, fixed ->
                viewModel.saveSetup(goal, currency, income, fixed)
                editing = false
                tab = Tab.Home
            },
        )
        return
    }

    if (tab != Tab.Home) BackHandler { tab = Tab.Home }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                Tab.entries.forEach { t ->
                    NavigationBarItem(
                        selected = tab == t,
                        onClick = { tab = t },
                        icon = {
                            Icon(
                                when (t) {
                                    Tab.Home -> Icons.Default.Home
                                    Tab.History -> Icons.Default.DateRange
                                    Tab.Settings -> Icons.Default.Settings
                                },
                                contentDescription = t.label,
                            )
                        },
                        label = { Text(t.label) },
                    )
                }
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (tab) {
                Tab.Home -> HomeScreen(
                    state = state,
                    today = today,
                    onAddExpense = viewModel::addExpense,
                    onDeleteExpense = viewModel::deleteExpense,
                )
                Tab.History -> HistoryScreen(state = state, today = today)
                Tab.Settings -> SettingsScreen(
                    state = state,
                    onEditPlan = { editing = true },
                    onLoadDemo = {
                        viewModel.loadDemoData()
                        tab = Tab.Home
                    },
                    onReset = {
                        viewModel.resetAll()
                        tab = Tab.Home
                    },
                )
            }
        }
    }
}
