package com.bharatupadhyay.espnest.domain

object IpValidator {
    fun validateIp(raw: String): String? {
        val ip = raw.trim()
        if (ip.isEmpty()) return "Enter the IP address of your ESP32"
        val parts = ip.split('.')
        if (parts.size != 4) return "Use an IPv4 address like 192.168.1.50"
        for (part in parts) {
            if (part.isEmpty()) return "Use an IPv4 address like 192.168.1.50"
            if (!part.all { it.isDigit() }) return "Use an IPv4 address like 192.168.1.50"
            if (part.length > 1 && part.startsWith('0')) {
                return "Remove leading zeros from each octet"
            }
            val value = part.toIntOrNull() ?: return "Use an IPv4 address like 192.168.1.50"
            if (value !in 0..255) return "Each number in the IP must be between 0 and 255"
        }
        return null
    }

    fun validatePort(raw: String): String? {
        val port = raw.trim()
        if (port.isEmpty()) return "Enter a port between 1 and 65535"
        val value = port.toIntOrNull() ?: return "Port must be a number"
        if (value !in 1..65535) return "Port must be between 1 and 65535"
        return null
    }

    fun validateName(raw: String): String? {
        if (raw.trim().isEmpty()) return "Give this device a name"
        return null
    }

    fun parsePort(raw: String): Int? {
        if (validatePort(raw) != null) return null
        return raw.trim().toInt()
    }

    fun isValid(ip: String, port: String): Boolean =
        validateIp(ip) == null && validatePort(port) == null
}
