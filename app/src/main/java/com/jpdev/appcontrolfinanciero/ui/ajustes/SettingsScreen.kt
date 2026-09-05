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

        // ponytail: `settings.*` round-trips through DataStore on every keystroke, so binding a
        // field's value straight to it races the disk write and resets the cursor to the end of
        // whatever stale value lands next (looked like typing backwards). Each field keeps its
        // own local echo, adopted once from the store on first load, then left alone.
        var alias by remember { mutableStateOf<String?>(null) }
        LaunchedEffect(settings.userAlias) { if (alias == null) alias = settings.userAlias }
        OutlinedTextField(
            value = alias ?: "",
            onValueChange = { alias = it; viewModel.setUserAlias(it) },
            label = { Text("Tu nombre o alias") },
            modifier = Modifier.fillMaxWidth()
        )

        Text("Mes activo", style = MaterialTheme.typography.titleMedium)
        var monthLabel by remember { mutableStateOf<String?>(null) }
        LaunchedEffect(settings.activeMonthLabel) { if (monthLabel == null) monthLabel = settings.activeMonthLabel }
        OutlinedTextField(
            value = monthLabel ?: "",
            onValueChange = { monthLabel = it; viewModel.setActiveMonth(it, settings.activeMonthEmoji) },
            label = { Text("Nombre del mes (ej. Mes del viaje)") },
            modifier = Modifier.fillMaxWidth()
        )
        var monthEmoji by remember { mutableStateOf<String?>(null) }
        LaunchedEffect(settings.activeMonthEmoji) { if (monthEmoji == null) monthEmoji = settings.activeMonthEmoji }
        OutlinedTextField(
            value = monthEmoji ?: "",
            onValueChange = { monthEmoji = it; viewModel.setActiveMonth(settings.activeMonthLabel, it) },
            label = { Text("Emoji del mes") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
