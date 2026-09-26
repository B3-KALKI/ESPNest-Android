package com.bharatupadhyay.espnest.ui.browser

import android.webkit.WebView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bharatupadhyay.espnest.data.repository.DeviceRepository
import com.bharatupadhyay.espnest.data.repository.SettingsRepository
import com.bharatupadhyay.espnest.di.AppContainer
import com.bharatupadhyay.espnest.domain.ConnectionTarget
import com.bharatupadhyay.espnest.domain.NetworkMonitor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

enum class BrowserStatus {
    Connecting, Connected, Offline, Unreachable
}

data class BrowserUiState(
    val target: ConnectionTarget,
    val currentUrl: String,
    val status: BrowserStatus = BrowserStatus.Connecting,
    val progress: Int = 0,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val javaScriptEnabled: Boolean = true,
    val timeoutMs: Int = 8000,
    val wifiReady: Boolean = true,
    val alreadySaved: Boolean = false,
    val showSaveDialog: Boolean = false,
    val saveName: String = "",
    val saveNameError: String? = null,
    val savedMessage: String? = null
)

class BrowserViewModel(
    private val target: ConnectionTarget,
    private val deviceRepository: DeviceRepository,
    private val settingsRepository: SettingsRepository,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val status = MutableStateFlow(BrowserStatus.Connecting)
    private val progress = MutableStateFlow(8)
    private val currentUrl = MutableStateFlow(target.url)
    private val canGoBack = MutableStateFlow(false)
    private val canGoForward = MutableStateFlow(false)
    private val alreadySaved = MutableStateFlow(false)
    private val showSaveDialog = MutableStateFlow(false)
    private val saveName = MutableStateFlow(target.displayName)
    private val saveNameError = MutableStateFlow<String?>(null)
    private val savedMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<BrowserUiState> = combine(
        combine(status, progress, currentUrl, canGoBack, canGoForward) { s, p, url, back, forward ->
            BrowserChrome(s, p, url, back, forward)
        },
        combine(
            settingsRepository.javaScriptEnabled,
            settingsRepository.connectionTimeoutMs,
            networkMonitor.state
        ) { js, timeout, net -> Triple(js, timeout, net) },
        combine(alreadySaved, showSaveDialog, saveName, saveNameError, savedMessage) { saved, dialog, name, err, msg ->
            SaveChrome(saved, dialog, name, err, msg)
        }
    ) { chrome, settings, save ->
        BrowserUiState(
            target = target,
            currentUrl = chrome.url,
            status = chrome.status,
            progress = chrome.progress,
            canGoBack = chrome.back,
            canGoForward = chrome.forward,
            javaScriptEnabled = settings.first,
            timeoutMs = settings.second,
            wifiReady = settings.third.wifiReady,
            alreadySaved = save.saved,
            showSaveDialog = save.dialog,
            saveName = save.name,
            saveNameError = save.err,
            savedMessage = save.msg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = BrowserUiState(target = target, currentUrl = target.url, saveName = target.displayName)
    )

    init {
        viewModelScope.launch {
            alreadySaved.value = deviceRepository.isSaved(target.ip, target.port)
            probeAndConnect()
        }
    }

    fun onProgress(value: Int) {
        progress.value = value.coerceIn(0, 100)
    }

    fun onUrlChange(url: String) {
        currentUrl.value = url
    }

    fun onHistoryChange(back: Boolean, forward: Boolean) {
        canGoBack.value = back
        canGoForward.value = forward
    }

    fun onPageStarted() {
        if (status.value == BrowserStatus.Unreachable || status.value == BrowserStatus.Offline) return
        status.value = BrowserStatus.Connecting
    }

    fun onPageFinished() {
        if (status.value == BrowserStatus.Unreachable || status.value == BrowserStatus.Offline) return
        status.value = BrowserStatus.Connected
        progress.value = 100
        viewModelScope.launch { deviceRepository.markLastUsed(target.ip, target.port) }
    }

    fun onReceivedError() {
        status.value = if (!uiState.value.wifiReady) BrowserStatus.Offline else BrowserStatus.Unreachable
    }

    fun retry(webView: WebView?) {
        viewModelScope.launch {
            probeAndConnect()
            withContext(Dispatchers.Main) {
                webView?.loadUrl(target.url)
            }
        }
    }

    fun openSave() {
        saveName.value = target.displayName
        saveNameError.value = null
        showSaveDialog.value = true
    }

    fun dismissSave() {
        showSaveDialog.value = false
    }

    fun onSaveName(value: String) {
        saveName.value = value
        saveNameError.value = null
    }

    fun confirmSave() {
        val name = saveName.value.trim()
        if (name.isEmpty()) {
            saveNameError.value = "Give this device a name"
            return
        }
        viewModelScope.launch {
            deviceRepository.save(name, target.ip, target.port)
            alreadySaved.value = true
            showSaveDialog.value = false
            savedMessage.value = "Saved to your devices"
        }
    }

    fun consumeSavedMessage() {
        savedMessage.value = null
    }

    private suspend fun probeAndConnect() {
        val net = networkMonitor.state.value
        if (!net.hasWifi && !net.hasNetwork) {
            status.value = BrowserStatus.Offline
            return
        }
        status.value = BrowserStatus.Connecting
        progress.value = 12
        val timeout = uiState.value.timeoutMs
        val ok = probe(target.url, timeout)
        if (ok) {
            status.value = BrowserStatus.Connected
        } else {
            status.value = if (networkMonitor.state.value.hasWifi) {
                BrowserStatus.Unreachable
            } else {
                BrowserStatus.Offline
            }
        }
    }

    private suspend fun probe(url: String, timeoutMs: Int): Boolean = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            connection = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = timeoutMs
                readTimeout = timeoutMs
                instanceFollowRedirects = true
                requestMethod = "GET"
                useCaches = false
            }
            connection.connect()
            connection.responseCode < 500
        } catch (_: Exception) {
            false
        } finally {
            connection?.disconnect()
        }
    }

    companion object {
        fun factory(container: AppContainer, target: ConnectionTarget): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return BrowserViewModel(
                        target = target,
                        deviceRepository = container.deviceRepository,
                        settingsRepository = container.settingsRepository,
                        networkMonitor = container.networkMonitor
                    ) as T
                }
            }
    }
}

private data class BrowserChrome(
    val status: BrowserStatus,
    val progress: Int,
    val url: String,
    val back: Boolean,
    val forward: Boolean
)

private data class SaveChrome(
    val saved: Boolean,
    val dialog: Boolean,
    val name: String,
    val err: String?,
    val msg: String?
)
