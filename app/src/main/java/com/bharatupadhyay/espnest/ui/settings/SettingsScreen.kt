package com.bharatupadhyay.espnest.ui.settings

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bharatupadhyay.espnest.R
import com.bharatupadhyay.espnest.di.LocalAppContainer
import com.bharatupadhyay.espnest.domain.ThemeMode
import com.bharatupadhyay.espnest.ui.components.EspTopBar
import com.bharatupadhyay.espnest.ui.components.SectionLabel

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val app = LocalContext.current.applicationContext as Application
    val viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.factory(app, container))
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        val msg = state.message ?: return@LaunchedEffect
        snackbar.showSnackbar(msg)
        viewModel.consumeMessage()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            EspTopBar(
                title = stringResource(R.string.nav_settings),
                onBack = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            SectionLabel(stringResource(R.string.settings_appearance))
            ThemeMode.entries.forEach { mode ->
                val label = when (mode) {
                    ThemeMode.Dark -> stringResource(R.string.settings_theme_dark)
                    ThemeMode.Light -> stringResource(R.string.settings_theme_light)
                    ThemeMode.System -> stringResource(R.string.settings_theme_system)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = state.themeMode == mode,
                            onClick = { viewModel.setTheme(mode) },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = state.themeMode == mode,
                        onClick = null
                    )
                    Text(
                        text = label,
                        modifier = Modifier.padding(start = 12.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            SectionLabel(stringResource(R.string.settings_browser))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Text(stringResource(R.string.settings_javascript), style = MaterialTheme.typography.bodyLarge)
                    Text(
                        stringResource(R.string.settings_javascript_body),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = state.javaScriptEnabled,
                    onCheckedChange = viewModel::setJavaScript
                )
            }
            TextButton(onClick = viewModel::requestClearBrowsing) {
                Text(stringResource(R.string.settings_clear_browsing))
            }
            Text(
                stringResource(R.string.settings_clear_browsing_body),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = viewModel::requestClearDevices) {
                Text(stringResource(R.string.settings_clear_devices))
            }
            Text(
                stringResource(R.string.settings_clear_devices_body),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))
            SectionLabel(stringResource(R.string.settings_network))
            Text(
                stringResource(R.string.settings_timeout),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                stringResource(R.string.settings_timeout_body),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            val timeouts = listOf(5000, 8000, 10000, 15000)
            timeouts.forEach { ms ->
                val label = when (ms) {
                    5000 -> stringResource(R.string.timeout_5)
                    8000 -> stringResource(R.string.timeout_8)
                    10000 -> stringResource(R.string.timeout_10)
                    else -> stringResource(R.string.timeout_15)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = state.timeoutMs == ms,
                            onClick = { viewModel.setTimeout(ms) },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = state.timeoutMs == ms, onClick = null)
                    Text(label, modifier = Modifier.padding(start = 12.dp), style = MaterialTheme.typography.bodyLarge)
                }
            }

            Spacer(Modifier.height(16.dp))
            SectionLabel(stringResource(R.string.settings_about))
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineSmall)
            Text(
                stringResource(R.string.app_tagline),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            AboutRow(stringResource(R.string.settings_version_label), stringResource(R.string.app_version))
            AboutRow(stringResource(R.string.settings_developer), stringResource(R.string.developer_name))
            AboutRow(stringResource(R.string.settings_contact), stringResource(R.string.developer_phone))
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.about_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(32.dp))
        }
    }

    if (state.confirmClearBrowsing) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDialogs,
            title = { Text(stringResource(R.string.settings_clear_browsing_confirm_title)) },
            text = { Text(stringResource(R.string.settings_clear_browsing_confirm_body)) },
            confirmButton = {
                TextButton(onClick = viewModel::confirmClearBrowsing) {
                    Text(stringResource(R.string.action_clear))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDialogs) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
    if (state.confirmClearDevices) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDialogs,
            title = { Text(stringResource(R.string.settings_clear_devices_confirm_title)) },
            text = { Text(stringResource(R.string.settings_clear_devices_confirm_body)) },
            confirmButton = {
                TextButton(onClick = viewModel::confirmClearDevices) {
                    Text(stringResource(R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDialogs) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
private fun AboutRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
