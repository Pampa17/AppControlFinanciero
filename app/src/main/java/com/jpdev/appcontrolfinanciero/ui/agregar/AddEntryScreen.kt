package com.jpdev.appcontrolfinanciero.ui.agregar

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jpdev.appcontrolfinanciero.AppControlFinancieroApplication
import com.jpdev.appcontrolfinanciero.domain.EntryType
import com.jpdev.appcontrolfinanciero.ui.components.ThousandsVisualTransformation
import com.jpdev.appcontrolfinanciero.ui.components.formatMoney
import com.jpdev.appcontrolfinanciero.ui.theme.Spacing
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEntryScreen(
    type: EntryType,
    editId: Long?,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier
) {
    val application = LocalContext.current.applicationContext as AppControlFinancieroApplication
    val viewModel: AddEntryViewModel = viewModel(
        factory = AddEntryViewModel.factory(application, type, editId)
    )
    val state by viewModel.uiState.collectAsState()
    val voiceState by viewModel.voiceState.collectAsState()

    LaunchedEffect(state.saved) {
        if (state.saved) onSaved()
    }

    LaunchedEffect(voiceState) {
        val current = voiceState
        if (current is VoiceState.Result) viewModel.consumeVoiceResult(current.text)
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) viewModel.startVoice()
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    var showNewCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Text(
            text = if (type == EntryType.INGRESO) "Registrar ingreso" else if (state.isEditing) "Editar gasto" else "Registrar gasto",
            style = MaterialTheme.typography.headlineLarge
        )

        VoiceMicButton(
            voiceState = voiceState,
            onClick = {
                val context = application
                val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                    PackageManager.PERMISSION_GRANTED
                if (granted) viewModel.startVoice() else micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            },
            onRetry = { viewModel.startVoice() }
        )

        OutlinedTextField(
            value = state.description,
            onValueChange = viewModel::updateDescription,
            label = { Text("Descripción *") },
            isError = state.descriptionError != null,
            supportingText = { state.descriptionError?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )

        Column {
            Box {
                OutlinedButton(
                    onClick = { categoryMenuExpanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Categoría *: ${state.selectedCategory?.name ?: "Selecciona una"}",
                        modifier = Modifier.weight(1f)
                    )
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                }
                DropdownMenu(
                    expanded = categoryMenuExpanded,
                    onDismissRequest = { categoryMenuExpanded = false }
                ) {
                    state.categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.name) },
                            onClick = {
                                viewModel.updateCategory(category.id)
                                categoryMenuExpanded = false
                            }
                        )
                    }
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("+ Nueva categoría") },
                        onClick = {
                            categoryMenuExpanded = false
                            newCategoryName = ""
                            showNewCategoryDialog = true
                        }
                    )
                }
            }
            state.categoryError?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        }

        OutlinedTextField(
            value = state.amountText,
            onValueChange = viewModel::updateAmountText,
            label = { Text("Monto *") },
            isError = state.amountError != null,
            visualTransformation = ThousandsVisualTransformation,
            supportingText = {
                state.amountError?.let { Text(it) }
                    ?: state.maxAllowed?.takeIf { type == EntryType.EGRESO }?.let { Text("Monto máximo permitido: $${it.formatMoney()}") }
            },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp))
            Text(
                "Fecha: ${state.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",
                modifier = Modifier.padding(start = Spacing.xs)
            )
        }

        state.saveError?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }

        if (state.saveError == null) {
            state.saveBlockedReason?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        }

        Button(
            onClick = { viewModel.save() },
            enabled = state.canSave,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar")
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        viewModel.updateDate(date)
                    }
                    showDatePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
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
                        viewModel.addAndSelectCategory(newCategoryName)
                        showNewCategoryDialog = false
                    }
                ) { Text("Crear") }
            },
            dismissButton = {
                TextButton(onClick = { showNewCategoryDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun VoiceMicButton(
    voiceState: VoiceState,
    onClick: () -> Unit,
    onRetry: () -> Unit
) {
    Column {
        val label = when (voiceState) {
            VoiceState.Listening -> "Escuchando..."
            else -> "Dictar movimiento"
        }
        Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.Mic, contentDescription = null, modifier = Modifier.size(18.dp))
            Text(label, modifier = Modifier.padding(start = Spacing.xs))
        }
        if (voiceState is VoiceState.Error) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    voiceState.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
                TextButton(onClick = onRetry) { Text("Reintentar") }
            }
        }
    }
}
