package com.jpdev.appcontrolfinanciero.ui.ajustes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jpdev.appcontrolfinanciero.AppControlFinancieroApplication
import com.jpdev.appcontrolfinanciero.ui.theme.AccentPalette
import com.jpdev.appcontrolfinanciero.ui.theme.Spacing
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val application = LocalContext.current.applicationContext as AppControlFinancieroApplication
    val viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.factory(application))
    val settings by viewModel.settings.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        Text("Ajustes", style = MaterialTheme.typography.headlineLarge)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Tema oscuro", style = MaterialTheme.typography.titleMedium)
            Switch(
                checked = settings.isDarkTheme ?: false,
                onCheckedChange = { viewModel.setDarkTheme(it) }
            )
        }

        Column {
            Text("Paleta de acento", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                AccentPalette.entries.forEach { palette ->
                    val selected = settings.accentPalette == palette.name
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { viewModel.setAccentPalette(palette.name) }
                    ) {
                        var swatchModifier = Modifier
                            .size(40.dp)
                            .shadow(elevation = 3.dp, shape = CircleShape)
                            .clip(CircleShape)
                            .background(palette.swatch)
                        if (selected) {
                            swatchModifier = swatchModifier.border(2.dp, Color.Black, CircleShape)
                        }
                        Box(modifier = swatchModifier)
                        Text(palette.label, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        // Theme switch/palette above apply instantly (single taps, no "draft" to confirm).
        // Text fields below are explicit-save: typing only edits a local draft, nothing persists
        // until Guardar — avoids both the old per-keystroke DataStore race and the "does this
        // even save?" feeling of fields with no confirmation.
        var aliasDraft by remember { mutableStateOf<String?>(null) }
        LaunchedEffect(settings.userAlias) { if (aliasDraft == null) aliasDraft = settings.userAlias }
        var aliasSaved by remember { mutableStateOf(false) }
        LaunchedEffect(aliasSaved) { if (aliasSaved) { delay(1500.milliseconds); aliasSaved = false } }

        OutlinedTextField(
            value = aliasDraft ?: "",
            onValueChange = { aliasDraft = it; aliasSaved = false },
            label = { Text("Tu nombre o alias") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Button(
                onClick = { viewModel.setUserAlias(aliasDraft.orEmpty()); aliasSaved = true },
                enabled = aliasDraft != null && aliasDraft != settings.userAlias
            ) { Text("Guardar") }
            if (aliasSaved) Text("Guardado ✓", color = MaterialTheme.colorScheme.primary)
        }

        Text("Mes activo", style = MaterialTheme.typography.titleMedium)
        var monthLabelDraft by remember { mutableStateOf<String?>(null) }
        LaunchedEffect(settings.activeMonthLabel) { if (monthLabelDraft == null) monthLabelDraft = settings.activeMonthLabel }
        var monthEmojiDraft by remember { mutableStateOf<String?>(null) }
        LaunchedEffect(settings.activeMonthEmoji) { if (monthEmojiDraft == null) monthEmojiDraft = settings.activeMonthEmoji }
        var monthSaved by remember { mutableStateOf(false) }
        LaunchedEffect(monthSaved) { if (monthSaved) { delay(1500.milliseconds); monthSaved = false } }

        OutlinedTextField(
            value = monthLabelDraft ?: "",
            onValueChange = { monthLabelDraft = it; monthSaved = false },
            label = { Text("Nombre del mes (ej. Mes del viaje)") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = monthEmojiDraft ?: "",
            onValueChange = { monthEmojiDraft = it; monthSaved = false },
            label = { Text("Emoji del mes") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Button(
                onClick = {
                    viewModel.setActiveMonth(monthLabelDraft.orEmpty(), monthEmojiDraft.orEmpty())
                    monthSaved = true
                },
                enabled = (monthLabelDraft != null && monthLabelDraft != settings.activeMonthLabel) ||
                    (monthEmojiDraft != null && monthEmojiDraft != settings.activeMonthEmoji)
            ) { Text("Guardar") }
            if (monthSaved) Text("Guardado ✓", color = MaterialTheme.colorScheme.primary)
        }
    }
}
