package com.bharatupadhyay.espnest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bharatupadhyay.espnest.di.LocalAppContainer
import com.bharatupadhyay.espnest.domain.ThemeMode
import com.bharatupadhyay.espnest.ui.EspNestApp
import com.bharatupadhyay.espnest.ui.theme.ESPNestTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val app = application as ESPNestApplication
        setContent {
            val themeMode by app.container.settingsRepository.themeMode
                .collectAsStateWithLifecycle(initialValue = ThemeMode.Dark)
            CompositionLocalProvider(LocalAppContainer provides app.container) {
                ESPNestTheme(themeMode = themeMode) {
                    EspNestApp()
                }
            }
        }
    }
}
