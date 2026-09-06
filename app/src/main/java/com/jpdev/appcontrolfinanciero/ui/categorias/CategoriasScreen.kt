package com.jpdev.appcontrolfinanciero.ui.categorias

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jpdev.appcontrolfinanciero.AppControlFinancieroApplication
import com.jpdev.appcontrolfinanciero.data.CategoryTotal
import com.jpdev.appcontrolfinanciero.domain.EntryType
import com.jpdev.appcontrolfinanciero.ui.components.formatMoney
import com.jpdev.appcontrolfinanciero.ui.theme.Spacing
import kotlinx.coroutines.launch

/** One color per slice, spread evenly around the hue wheel — works for any category count. */
private fun sliceColor(index: Int, count: Int): Color =
    Color.hsv((index * 360f / count.coerceAtLeast(1)) % 360f, 0.55f, 0.85f)

@Composable
fun CategoriasScreen(modifier: Modifier = Modifier) {
    val application = LocalContext.current.applicationContext as AppControlFinancieroApplication
    val viewModel: CategoriasViewModel = viewModel(factory = CategoriasViewModel.factory(application))
    val state by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(0) }
    val currentType = if (selectedTab == 0) EntryType.INGRESO else EntryType.EGRESO
    // Categories with $0 this month add nothing to a pie slice — shown once they have movements.
    val currentTotals = (if (selectedTab == 0) state.incomeTotals else state.expenseTotals).filter { it.total > 0 }
    val grandTotal = currentTotals.sumOf { it.total }

    var showNewCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var actionTarget by remember { mutableStateOf<CategoryTotal?>(null) }
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

        if (currentTotals.isEmpty()) {
            Text(
                "Sin movimientos este mes",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(Spacing.md)
            )
        } else {
            LazyColumn(contentPadding = PaddingValues(horizontal = Spacing.md)) {
                item {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CategoryPieChart(
                            totals = currentTotals,
                            modifier = Modifier.padding(vertical = Spacing.md)
                        )
                    }
                }
                itemsIndexed(currentTotals, key = { _, item -> item.id }) { index, category ->
                    val percent = (category.total * 100 / grandTotal).toInt()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { actionTarget = category }
                            .padding(vertical = Spacing.xs),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(sliceColor(index, currentTotals.size))
                            )
                            Text(category.name, modifier = Modifier.padding(start = Spacing.xs))
                        }
                        Text("$${category.total.formatMoney()} · $percent%", style = MaterialTheme.typography.bodyMedium)
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

    // Tapping a slice's legend row opens this chooser instead of always-visible edit/delete icons.
    actionTarget?.let { category ->
        AlertDialog(
            onDismissRequest = { actionTarget = null },
            title = { Text(category.name) },
            text = { Text("$${category.total.formatMoney()} este mes") },
            confirmButton = {
                TextButton(onClick = {
                    renaming = category
                    renameText = category.name
                    actionTarget = null
                }) { Text("Renombrar") }
            },
            dismissButton = {
                Row {
                    TextButton(
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        onClick = {
                            actionTarget = null
                            scope.launch {
                                pendingDeleteUsage = viewModel.countUsage(category.id, currentType)
                                pendingDelete = category
                            }
                        }
                    ) { Text("Eliminar") }
                    TextButton(onClick = { actionTarget = null }) { Text("Cerrar") }
                }
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

@Composable
private fun CategoryPieChart(totals: List<CategoryTotal>, modifier: Modifier = Modifier) {
    val total = totals.sumOf { it.total }.toFloat()
    Canvas(modifier = modifier.size(200.dp)) {
        var startAngle = -90f
        totals.forEachIndexed { index, category ->
            val sweep = 360f * category.total / total
            drawArc(
                color = sliceColor(index, totals.size),
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = true
            )
            startAngle += sweep
        }
    }
}
