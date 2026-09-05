package com.jpdev.appcontrolfinanciero.ui.dashboard

import android.Manifest
import android.os.Build
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jpdev.appcontrolfinanciero.AppControlFinancieroApplication
import com.jpdev.appcontrolfinanciero.ui.components.BalanceCard
import com.jpdev.appcontrolfinanciero.ui.components.formatMoney
import com.jpdev.appcontrolfinanciero.ui.navigation.EntryType
import com.jpdev.appcontrolfinanciero.ui.theme.Spacing

@Composable
fun DashboardScreen(
    onAddEntry: (EntryType) -> Unit,
    modifier: Modifier = Modifier
) {
    val application = LocalContext.current.applicationContext as AppControlFinancieroApplication
    val viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.factory(application))
    val state by viewModel.uiState.collectAsState()

    // Request POST_NOTIFICATIONS once on first composition (API 33+ only; no-op below that).
    val notificationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val context = application
            val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            if (!granted) notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        val greetingName = state.alias.ifBlank { "" }
        Text(
            text = if (greetingName.isBlank()) "¡Hola!" else "Hola, $greetingName!",
            style = MaterialTheme.typography.headlineLarge
        )
        if (state.monthLabel.isNotBlank() || state.monthEmoji.isNotBlank()) {
            Text(
                text = "${state.monthEmoji} ${state.monthLabel}".trim(),
                style = MaterialTheme.typography.titleMedium
            )
        }

        BalanceCard(
            totalIncome = state.totalIncome,
            totalExpenses = state.totalExpenses,
            balance = state.balance,
            status = state.status
        )

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Button(onClick = { onAddEntry(EntryType.INGRESO) }, modifier = Modifier.weight(1f)) {
                Text("+ Ingreso")
            }
            OutlinedButton(onClick = { onAddEntry(EntryType.EGRESO) }, modifier = Modifier.weight(1f)) {
                Text("+ Gasto")
            }
        }

        Text("Últimos movimientos", style = MaterialTheme.typography.titleMedium)
        LazyColumn(
            contentPadding = PaddingValues(bottom = Spacing.xxl),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            items(state.recentExpenses) { expense ->
                Card(elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = Spacing.sm, vertical = Spacing.xs),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(expense.description, style = MaterialTheme.typography.bodyMedium)
                        Text("-$${expense.amount.formatMoney()}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
            items(state.recentIncomes) { income ->
                Card(elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = Spacing.sm, vertical = Spacing.xs),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(income.description, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "+$${income.amount.formatMoney()}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}
