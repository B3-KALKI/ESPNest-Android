package com.bharatupadhyay.espnest.di

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import com.bharatupadhyay.espnest.data.local.AppDatabase
import com.bharatupadhyay.espnest.data.local.SettingsDataStore
import com.bharatupadhyay.espnest.data.repository.DeviceRepository
import com.bharatupadhyay.espnest.data.repository.SettingsRepository
import com.bharatupadhyay.espnest.domain.NetworkMonitor

class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val database: AppDatabase = AppDatabase.getInstance(appContext)
    val settingsDataStore = SettingsDataStore(appContext)
    val deviceRepository = DeviceRepository(database.deviceDao())
    val settingsRepository = SettingsRepository(settingsDataStore)
    val networkMonitor = NetworkMonitor(appContext)
}

val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer is not provided")
}
