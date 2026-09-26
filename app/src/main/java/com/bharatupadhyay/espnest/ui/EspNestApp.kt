package com.bharatupadhyay.espnest.ui

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bharatupadhyay.espnest.domain.ConnectionTarget
import com.bharatupadhyay.espnest.ui.browser.BrowserScreen
import com.bharatupadhyay.espnest.ui.home.HomeScreen
import com.bharatupadhyay.espnest.ui.settings.SettingsScreen

object Routes {
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val BROWSER = "browser/{ip}/{port}/{name}"

    fun browser(target: ConnectionTarget): String {
        val ip = Uri.encode(target.ip)
        val name = Uri.encode(target.name?.trim().orEmpty().ifBlank { " " })
        return "browser/$ip/${target.port}/$name"
    }
}

@Composable
fun EspNestApp() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onConnect = { target ->
                    navController.navigate(Routes.browser(target))
                },
                onOpenSettings = {
                    navController.navigate(Routes.SETTINGS)
                }
            )
        }
        composable(
            route = Routes.BROWSER,
            arguments = listOf(
                navArgument("ip") { type = NavType.StringType },
                navArgument("port") { type = NavType.IntType },
                navArgument("name") { type = NavType.StringType; defaultValue = " " }
            )
        ) { entry ->
            val ip = Uri.decode(entry.arguments?.getString("ip").orEmpty())
            val port = entry.arguments?.getInt("port") ?: 80
            val name = Uri.decode(entry.arguments?.getString("name").orEmpty()).trim()
            BrowserScreen(
                target = ConnectionTarget(
                    name = name.ifBlank { null },
                    ip = ip,
                    port = port
                ),
                onBackToHome = { navController.popBackStack() }
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
