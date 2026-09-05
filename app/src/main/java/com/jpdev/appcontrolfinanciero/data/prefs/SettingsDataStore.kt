package com.jpdev.appcontrolfinanciero.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

data class UserSettings(
    val isDarkTheme: Boolean? = null, // null = follow system
    val accentPalette: String = "AZUL",
    val userAlias: String = "",
    val activeMonthLabel: String = "",
    val activeMonthEmoji: String = ""
)

class SettingsDataStore(private val context: Context) {

    private object Keys {
        val DARK_THEME = booleanPreferencesKey("is_dark_theme")
        val ACCENT_PALETTE = stringPreferencesKey("accent_palette")
        val USER_ALIAS = stringPreferencesKey("user_alias")
        val ACTIVE_MONTH_LABEL = stringPreferencesKey("active_month_label")
        val ACTIVE_MONTH_EMOJI = stringPreferencesKey("active_month_emoji")
    }

    val settings: Flow<UserSettings> = context.dataStore.data.map { prefs ->
        UserSettings(
            isDarkTheme = prefs[Keys.DARK_THEME],
            accentPalette = prefs[Keys.ACCENT_PALETTE] ?: "AZUL",
            userAlias = prefs[Keys.USER_ALIAS] ?: "",
            activeMonthLabel = prefs[Keys.ACTIVE_MONTH_LABEL] ?: "",
            activeMonthEmoji = prefs[Keys.ACTIVE_MONTH_EMOJI] ?: ""
        )
    }

    suspend fun setDarkTheme(value: Boolean?) {
        context.dataStore.edit { prefs ->
            if (value == null) prefs.remove(Keys.DARK_THEME) else prefs[Keys.DARK_THEME] = value
        }
    }

    suspend fun setAccentPalette(value: String) {
        context.dataStore.edit { prefs -> prefs[Keys.ACCENT_PALETTE] = value }
    }

    suspend fun setUserAlias(value: String) {
        context.dataStore.edit { prefs -> prefs[Keys.USER_ALIAS] = value }
    }

    suspend fun setActiveMonth(label: String, emoji: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ACTIVE_MONTH_LABEL] = label
            prefs[Keys.ACTIVE_MONTH_EMOJI] = emoji
        }
    }
}
