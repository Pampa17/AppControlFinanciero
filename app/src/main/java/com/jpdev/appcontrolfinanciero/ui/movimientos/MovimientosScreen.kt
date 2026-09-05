package com.jpdev.appcontrolfinanciero.ui.movimientos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jpdev.appcontrolfinanciero.AppControlFinancieroApplication
import com.jpdev.appcontrolfinanciero.data.local.ExpenseEntity
import com.jpdev.appcontrolfinanciero.domain.ExpenseCategory
import com.jpdev.appcontrolfinanciero.ui.components.ConfirmDeleteDialog
import com.jpdev.appcontrolfinanciero.ui.theme.Spacing

@Composable
fun MovimientosScreen(
    onEditExpense: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val application = LocalContext.current.applicationContext as AppControlFinancieroApplication
    val viewModel: MovimientosViewModel = viewModel(factory = MovimientosViewModel.factory(application))
    val state by viewModel.uiState.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    var pendingDelete by remember { mutableStateOf<ExpenseEntity?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        SecondaryTabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Ingresos") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Gastos") })
        }

        if (selectedTab == 0) {
            LazyColumn(
                contentPadding = PaddingValues(Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                items(state.incomes) { income ->
                    Card(elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                        Row(
                            Modifier.fillMaxWidth().padding(Spacing.sm),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(income.description, style = MaterialTheme.typography.bodyLarge)
                                Text(income.category, style = MaterialTheme.typography.bodyMedium)
                            }
                            Text(
                                "+$${income.amount}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                items(state.expenses) { expense ->
                    Card(elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                        Row(
                            Modifier.fillMaxWidth().padding(Spacing.sm),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(expense.description, style = MaterialTheme.typography.bodyLarge)
                                val label = ExpenseCategory.entries
                                    .firstOrNull { it.name == expense.category }?.label ?: expense.category
                                Text(label, style = MaterialTheme.typography.bodyMedium)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "-$${expense.amount}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Row {
                                    IconButton(onClick = { onEditExpense(expense.id) }) {
                                        Icon(Icons.Filled.Edit, contentDescription = "Editar")
                                    }
                                    IconButton(onClick = { pendingDelete = expense }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Eliminar")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    pendingDelete?.let { expense ->
        ConfirmDeleteDialog(
            itemDescription = expense.description,
            onConfirm = {
                viewModel.deleteExpense(expense)
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null }
        )
    }
}
