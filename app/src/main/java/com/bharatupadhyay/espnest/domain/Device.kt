package com.bharatupadhyay.espnest.domain

data class Device(
    val id: Long,
    val name: String,
    val ip: String,
    val port: Int,
    val favorite: Boolean,
    val lastUsedAt: Long
) {
    val addressLabel: String = UrlBuilder.hostLabel(ip, port)

    fun toTarget(): ConnectionTarget = ConnectionTarget(
        name = name,
        ip = ip,
        port = port
    )
}
