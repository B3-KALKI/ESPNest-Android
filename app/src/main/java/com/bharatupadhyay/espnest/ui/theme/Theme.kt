package com.bharatupadhyay.espnest.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.bharatupadhyay.espnest.domain.ThemeMode

private val DarkColors = darkColorScheme(
    primary = Snow,
    onPrimary = Ink,
    primaryContainer = InkHigh,
    onPrimaryContainer = Snow,
    secondary = Steel,
    onSecondary = Ink,
    secondaryContainer = InkHigh,
    onSecondaryContainer = SteelSoft,
    tertiary = SteelSoft,
    onTertiary = Ink,
    background = Ink,
    onBackground = Snow,
    surface = Ink,
    onSurface = Snow,
    surfaceVariant = InkElevated,
    onSurfaceVariant = Zinc,
    surfaceContainerLowest = Ink,
    surfaceContainerLow = InkElevated,
    surfaceContainer = InkHigh,
    surfaceContainerHigh = InkHigh,
    surfaceContainerHighest = InkLine,
    outline = InkLine,
    outlineVariant = InkLine,
    error = Ember,
    onError = Ink,
    errorContainer = InkHigh,
    onErrorContainer = Ember
)

private val LightColors = lightColorScheme(
    primary = LightInk,
    onPrimary = Snow,
    primaryContainer = LightHigh,
    onPrimaryContainer = LightInk,
    secondary = SteelDeep,
    onSecondary = Snow,
    secondaryContainer = LightHigh,
    onSecondaryContainer = SteelDeep,
    tertiary = Steel,
    onTertiary = LightInk,
    background = LightCanvas,
    onBackground = LightInk,
    surface = LightCanvas,
    onSurface = LightInk,
    surfaceVariant = LightSurface,
    onSurfaceVariant = LightMuted,
    surfaceContainerLowest = LightSurface,
    surfaceContainerLow = LightSurface,
    surfaceContainer = LightHigh,
    surfaceContainerHigh = LightHigh,
    surfaceContainerHighest = LightLine,
    outline = LightLine,
    outlineVariant = LightLine,
    error = Ember,
    onError = Ink,
    errorContainer = LightHigh,
    onErrorContainer = Ember
)

@Composable
fun ESPNestTheme(
    themeMode: ThemeMode = ThemeMode.Dark,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val darkTheme = when (themeMode) {
        ThemeMode.Dark -> true
        ThemeMode.Light -> false
        ThemeMode.System -> systemDark
    }
    val colors = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            @Suppress("DEPRECATION")
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }
    MaterialTheme(
        colorScheme = colors,
        typography = EspNestTypography,
        content = content
    )
}
