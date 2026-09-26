package com.bharatupadhyay.espnest.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bharatupadhyay.espnest.data.local.SettingsDataStore
import com.bharatupadhyay.espnest.data.repository.DeviceRepository
import com.bharatupadhyay.espnest.data.repository.SettingsRepository
import com.bharatupadhyay.espnest.di.AppContainer
import com.bharatupadhyay.espnest.domain.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.Dark,
    val javaScriptEnabled: Boolean = true,
    val timeoutMs: Int = SettingsDataStore.DEFAULT_TIMEOUT_MS,
    val confirmClearBrowsing: Boolean = false,
    val confirmClearDevices: Boolean = false,
    val message: String? = null
)

class SettingsViewModel(
    application: Application,
    private val settingsRepository: SettingsRepository,
    private val deviceRepository: DeviceRepository
) : AndroidViewModel(application) {

    private val confirmClearBrowsing = MutableStateFlow(false)
    private val confirmClearDevices = MutableStateFlow(false)
    private val message = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.themeMode,
        settingsRepository.javaScriptEnabled,
        settingsRepository.connectionTimeoutMs,
        confirmClearBrowsing,
        confirmClearDevices
    ) { theme, js, timeout, clearBrowse, clearDevices ->
        SettingsUiState(
            themeMode = theme,
            javaScriptEnabled = js,
            timeoutMs = timeout,
            confirmClearBrowsing = clearBrowse,
            confirmClearDevices = clearDevices,
            message = message.value
        )
    }.combine(message) { state, msg -> state.copy(message = msg) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState()
        )

    fun setTheme(mode: ThemeMode) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }

    fun setJavaScript(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setJavaScriptEnabled(enabled) }
    }

    fun setTimeout(ms: Int) {
        viewModelScope.launch { settingsRepository.setConnectionTimeoutMs(ms) }
    }

    fun requestClearBrowsing() {
        confirmClearBrowsing.value = true
    }

    fun requestClearDevices() {
        confirmClearDevices.value = true
    }

    fun dismissDialogs() {
        confirmClearBrowsing.value = false
        confirmClearDevices.value = false
    }

    fun confirmClearBrowsing() {
        viewModelScope.launch {
            settingsRepository.clearBrowsingData(getApplication())
            confirmClearBrowsing.value = false
            message.value = "Browsing data cleared"
        }
    }

    fun confirmClearDevices() {
        viewModelScope.launch {
            deviceRepository.deleteAll()
            confirmClearDevices.value = false
            message.value = "Saved devices cleared"
        }
    }

    fun consumeMessage() {
        message.value = null
    }

    companion object {
        fun factory(app: Application, container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SettingsViewModel(
                        application = app,
                        settingsRepository = container.settingsRepository,
                        deviceRepository = container.deviceRepository
                    ) as T
                }
            }
    }
}
