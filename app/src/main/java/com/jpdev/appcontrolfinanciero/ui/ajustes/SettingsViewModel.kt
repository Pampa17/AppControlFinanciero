package com.jpdev.appcontrolfinanciero.ui.ajustes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.jpdev.appcontrolfinanciero.AppControlFinancieroApplication
import com.jpdev.appcontrolfinanciero.data.prefs.SettingsDataStore
import com.jpdev.appcontrolfinanciero.data.prefs.UserSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val settingsDataStore: SettingsDataStore) : ViewModel() {

    val settings: StateFlow<UserSettings> = settingsDataStore.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettings())

    fun setDarkTheme(value: Boolean?) {
        viewModelScope.launch { settingsDataStore.setDarkTheme(value) }
    }

    fun setAccentPalette(value: String) {
        viewModelScope.launch { settingsDataStore.setAccentPalette(value) }
    }

    fun setUserAlias(value: String) {
        viewModelScope.launch { settingsDataStore.setUserAlias(value) }
    }

    fun setActiveMonth(label: String, emoji: String) {
        viewModelScope.launch { settingsDataStore.setActiveMonth(label, emoji) }
    }

    companion object {
        fun factory(application: AppControlFinancieroApplication) = viewModelFactory {
            initializer { SettingsViewModel(application.settingsDataStore) }
        }
    }
}
