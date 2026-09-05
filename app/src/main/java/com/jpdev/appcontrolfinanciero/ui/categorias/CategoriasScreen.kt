package com.jpdev.appcontrolfinanciero.ui.categorias

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jpdev.appcontrolfinanciero.AppControlFinancieroApplication
import com.jpdev.appcontrolfinanciero.data.CategoryTotal
import com.jpdev.appcontrolfinanciero.domain.EntryType
import com.jpdev.appcontrolfinanciero.ui.components.formatMoney
import com.jpdev.appcontrolfinanciero.ui.theme.Spacing
import kotlinx.coroutines.launch

@Composable
fun CategoriasScreen(modifier: Modifier = Modifier) {
    val application = LocalContext.current.applicationContext as AppControlFinancieroApplication
    val viewModel: CategoriasViewModel = viewModel(factory = CategoriasViewModel.factory(application))
    val state by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(0) }
    val currentType = if (selectedTab == 0) EntryType.INGRESO else EntryType.EGRESO
    val currentTotals = if (selectedTab == 0) state.incomeTotals else state.expenseTotals

    var showNewCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var renaming by remember { mutableStateOf<CategoryTotal?>(null) }
    var renameText by remember { mutableStateOf("") }
    var pendingDelete by remember { mutableStateOf<CategoryTotal?>(null) }
    var pendingDeleteUsage by remember { mutableIntStateOf(0) }

    Column(modifier = modifier.fillMaxSize()) {
        SecondaryTabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Ingresos") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Gastos") })
        }

        Button(
            onClick = { newCategoryName = ""; showNewCategoryDialog = true },
            modifier = Modifier.fillMaxWidth().padding(Spacing.md)
        ) {
            Text("+ Nueva categoría")
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            items(currentTotals, key = { it.id }) { category ->
                Card(elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                    Row(
                        Modifier.fillMaxWidth().padding(Spacing.sm),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(category.name, style = MaterialTheme.typography.bodyLarge)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("$${category.total.formatMoney()}", style = MaterialTheme.typography.bodyLarge)
                            if (!category.isReserved) {
                                IconButton(onClick = {
                                    renaming = category
                                    renameText = category.name
                                }) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Renombrar")
                                }
                                IconButton(onClick = {
                                    scope.launch {
                                        pendingDeleteUsage = viewModel.countUsage(category.id, currentType)
                                        pendingDelete = category
                                    }
                                }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNewCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showNewCategoryDialog = false },
            title = { Text("Nueva categoría") },
            text = {
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    enabled = newCategoryName.isNotBlank(),
                    onClick = {
                        viewModel.addCategory(newCategoryName, currentType)
                        showNewCategoryDialog = false
                    }
                ) { Text("Crear") }
            },
            dismissButton = {
                TextButton(onClick = { showNewCategoryDialog = false }) { Text("Cancelar") }
            }
        )
    }

    renaming?.let { category ->
        AlertDialog(
            onDismissRequest = { renaming = null },
            title = { Text("Renombrar categoría") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    enabled = renameText.isNotBlank(),
                    onClick = {
                        viewModel.renameCategory(category.id, currentType, renameText)
                        renaming = null
                    }
                ) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { renaming = null }) { Text("Cancelar") }
            }
        )
    }

    pendingDelete?.let { category ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("¿Eliminar \"${category.name}\"?") },
            text = {
                Text(
                    if (pendingDeleteUsage > 0) {
                        "Esta categoría tiene $pendingDeleteUsage movimiento(s). Si continúas, se " +
                            "reasignarán automáticamente a \"Sin categoría\" — no se borra ningún ingreso/gasto."
                    } else {
                        "No tiene movimientos registrados."
                    }
                )
            },
            confirmButton = {
                TextButton(
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    onClick = {
                        viewModel.deleteCategory(category.id, currentType)
                        pendingDelete = null
                    }
                ) { Text(if (pendingDeleteUsage > 0) "Eliminar de todas formas" else "Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancelar") }
            }
        )
    }
}
