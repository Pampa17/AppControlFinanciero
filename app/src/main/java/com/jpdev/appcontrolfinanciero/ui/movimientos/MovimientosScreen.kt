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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
            LazyColumn(contentPadding = PaddingValues(Spacing.md)) {
                items(state.incomes) { income ->
                    Column {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(income.description, style = MaterialTheme.typography.bodyLarge)
                                Text(income.category, style = MaterialTheme.typography.bodyMedium)
                            }
                            Text("+$${income.amount}", style = MaterialTheme.typography.bodyLarge)
                        }
                        HorizontalDivider(Modifier.padding(vertical = Spacing.xs))
                    }
                }
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(Spacing.md)) {
                items(state.expenses) { expense ->
                    Column {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(expense.description, style = MaterialTheme.typography.bodyLarge)
                                val label = ExpenseCategory.entries
                                    .firstOrNull { it.name == expense.category }?.label ?: expense.category
                                Text(label, style = MaterialTheme.typography.bodyMedium)
                            }
                            Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                                Text("-$${expense.amount}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
                                Row {
                                    TextButton(onClick = { onEditExpense(expense.id) }) { Text("Editar") }
                                    TextButton(onClick = { pendingDelete = expense }) { Text("Eliminar") }
                                }
                            }
                        }
                        HorizontalDivider(Modifier.padding(vertical = Spacing.xs))
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
