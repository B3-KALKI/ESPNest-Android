package com.bharatupadhyay.espnest.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.bharatupadhyay.espnest.domain.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "espnest_settings"
)

class SettingsDataStore(context: Context) {
    private val dataStore = context.applicationContext.settingsDataStore

    val themeMode: Flow<ThemeMode> = dataStore.data.map { prefs ->
        ThemeMode.fromStorage(prefs[Keys.THEME])
    }

    val javaScriptEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.JAVASCRIPT] ?: true
    }

    val connectionTimeoutMs: Flow<Int> = dataStore.data.map { prefs ->
        prefs[Keys.TIMEOUT_MS] ?: DEFAULT_TIMEOUT_MS
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[Keys.THEME] = mode.storageKey }
    }

    suspend fun setJavaScriptEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.JAVASCRIPT] = enabled }
    }

    suspend fun setConnectionTimeoutMs(timeoutMs: Int) {
        dataStore.edit { it[Keys.TIMEOUT_MS] = timeoutMs }
    }

    private object Keys {
        val THEME = stringPreferencesKey("theme_mode")
        val JAVASCRIPT = booleanPreferencesKey("javascript_enabled")
        val TIMEOUT_MS = intPreferencesKey("connection_timeout_ms")
    }

    companion object {
        const val DEFAULT_TIMEOUT_MS = 8000
    }
}
