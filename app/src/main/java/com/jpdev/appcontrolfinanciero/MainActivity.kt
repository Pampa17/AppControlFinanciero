package com.jpdev.appcontrolfinanciero

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.jpdev.appcontrolfinanciero.data.prefs.UserSettings
import com.jpdev.appcontrolfinanciero.ui.navigation.AppNavHost
import com.jpdev.appcontrolfinanciero.ui.theme.AccentPalette
import com.jpdev.appcontrolfinanciero.ui.theme.AppControlFinancieroTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val application = application as AppControlFinancieroApplication
        setContent {
            val settings by application.settingsDataStore.settings.collectAsState(initial = UserSettings())
            val palette = AccentPalette.entries.firstOrNull { it.name == settings.accentPalette } ?: AccentPalette.AZUL
            val darkTheme = settings.isDarkTheme ?: isSystemInDarkTheme()
            AppControlFinancieroTheme(accentPalette = palette, darkTheme = darkTheme) {
                AppNavHost()
            }
        }
    }
}
