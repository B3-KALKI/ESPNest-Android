package com.bharatupadhyay.espnest.data.repository

import android.content.Context
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.webkit.WebView
import com.bharatupadhyay.espnest.data.local.SettingsDataStore
import com.bharatupadhyay.espnest.domain.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class SettingsRepository(private val dataStore: SettingsDataStore) {
    val themeMode: Flow<ThemeMode> = dataStore.themeMode
    val javaScriptEnabled: Flow<Boolean> = dataStore.javaScriptEnabled
    val connectionTimeoutMs: Flow<Int> = dataStore.connectionTimeoutMs

    suspend fun setThemeMode(mode: ThemeMode) = dataStore.setThemeMode(mode)

    suspend fun setJavaScriptEnabled(enabled: Boolean) = dataStore.setJavaScriptEnabled(enabled)

    suspend fun setConnectionTimeoutMs(timeoutMs: Int) = dataStore.setConnectionTimeoutMs(timeoutMs)

    suspend fun clearBrowsingData(context: Context) {
        withContext(Dispatchers.Main) {
            WebStorage.getInstance().deleteAllData()
            val cookies = CookieManager.getInstance()
            cookies.removeAllCookies(null)
            cookies.flush()
            WebView(context.applicationContext).apply {
                clearCache(true)
                clearHistory()
                clearFormData()
                destroy()
            }
        }
    }
}
