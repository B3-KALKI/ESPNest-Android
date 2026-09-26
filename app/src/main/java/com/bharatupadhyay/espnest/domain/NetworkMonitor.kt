package com.bharatupadhyay.espnest.domain

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NetworkMonitor(context: Context) {
    data class NetworkState(
        val hasNetwork: Boolean = false,
        val hasWifi: Boolean = false,
        val isValidated: Boolean = false
    ) {
        val wifiReady: Boolean get() = hasWifi
    }

    private val connectivityManager =
        context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _state = MutableStateFlow(readCurrent())
    val state: StateFlow<NetworkState> = _state.asStateFlow()

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) = publish()
        override fun onLost(network: Network) = publish()
        override fun onUnavailable() = publish()
        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) = publish()
    }

    init {
        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
            .build()
        try {
            connectivityManager.registerDefaultNetworkCallback(callback)
        } catch (_: RuntimeException) {
            connectivityManager.registerNetworkCallback(request, callback)
        }
        publish()
    }

    private fun publish() {
        _state.value = readCurrent()
    }

    private fun readCurrent(): NetworkState {
        val active = connectivityManager.activeNetwork
        val caps = active?.let { connectivityManager.getNetworkCapabilities(it) }
        val hasNetwork = caps != null && (
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ||
                caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
            )
        val hasWifi = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        val isValidated = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true
        return NetworkState(
            hasNetwork = hasNetwork,
            hasWifi = hasWifi,
            isValidated = isValidated
        )
    }
}
