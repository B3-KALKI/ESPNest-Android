package com.bharatupadhyay.espnest.domain

enum class ThemeMode(val storageKey: String) {
    Dark("dark"),
    Light("light"),
    System("system");

    companion object {
        fun fromStorage(value: String?): ThemeMode = entries.firstOrNull { it.storageKey == value } ?: Dark
    }
}
