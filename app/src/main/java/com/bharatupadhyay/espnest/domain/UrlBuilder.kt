package com.bharatupadhyay.espnest.domain

object UrlBuilder {
    fun build(ip: String, port: Int): String {
        val host = ip.trim()
        return if (port == 80) "http://$host" else "http://$host:$port"
    }

    fun hostLabel(ip: String, port: Int): String {
        val host = ip.trim()
        return if (port == 80) host else "$host:$port"
    }
}
