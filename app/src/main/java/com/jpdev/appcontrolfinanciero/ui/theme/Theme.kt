package com.jpdev.appcontrolfinanciero.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * Po's tokens are authoritative here — unlike the original template, this does NOT branch on
 * Android 12+ dynamic/wallpaper color. [accentPalette] and [darkTheme] come from SettingsDataStore.
 */
@Composable
fun AppControlFinancieroTheme(
    accentPalette: AccentPalette = AccentPalette.AZUL,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = accentPalette.toColorScheme(darkTheme)
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
