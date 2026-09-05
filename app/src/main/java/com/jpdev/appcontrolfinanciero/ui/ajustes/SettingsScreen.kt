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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

        OutlinedTextField(
            value = settings.userAlias,
            onValueChange = { viewModel.setUserAlias(it) },
            label = { Text("Tu nombre o alias") },
            modifier = Modifier.fillMaxWidth()
        )

        Text("Mes activo", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = settings.activeMonthLabel,
            onValueChange = { viewModel.setActiveMonth(it, settings.activeMonthEmoji) },
            label = { Text("Nombre del mes (ej. Mes del viaje)") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = settings.activeMonthEmoji,
            onValueChange = { viewModel.setActiveMonth(settings.activeMonthLabel, it) },
            label = { Text("Emoji del mes") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
