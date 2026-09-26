package com.bharatupadhyay.espnest.domain

data class ConnectionTarget(
    val name: String?,
    val ip: String,
    val port: Int
) {
    val displayName: String
        get() = name?.trim()?.takeIf { it.isNotEmpty() } ?: "ESP32 $ip"

    val url: String
        get() = UrlBuilder.build(ip, port)

    val addressLabel: String
        get() = UrlBuilder.hostLabel(ip, port)
}
