package com.bharatupadhyay.espnest.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bharatupadhyay.espnest.R
import com.bharatupadhyay.espnest.di.LocalAppContainer
import com.bharatupadhyay.espnest.domain.ConnectionTarget
import com.bharatupadhyay.espnest.ui.components.ConnectCard
import com.bharatupadhyay.espnest.ui.components.DeviceCard
import com.bharatupadhyay.espnest.ui.components.EmptyDevices
import com.bharatupadhyay.espnest.ui.components.EspTopBar
import com.bharatupadhyay.espnest.ui.components.InnerShape
import com.bharatupadhyay.espnest.ui.components.NetworkChip
import com.bharatupadhyay.espnest.ui.components.SectionLabel
import com.bharatupadhyay.espnest.ui.components.SheetActionRow
import com.bharatupadhyay.espnest.ui.components.fieldColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onConnect: (ConnectionTarget) -> Unit,
    onOpenSettings: () -> Unit
) {
    val container = LocalAppContainer.current
    val viewModel: HomeViewModel = viewModel(factory = HomeViewModel.factory(container))
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            EspTopBar(
                title = stringResource(R.string.app_name),
                subtitle = stringResource(R.string.app_tagline),
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = stringResource(R.string.action_settings)
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                NetworkChip(wifiReady = state.network.wifiReady)
            }
            item {
                ConnectCard(
                    ip = state.form.ip,
                    port = state.form.port,
                    ipError = state.form.ipError,
                    portError = state.form.portError,
                    onIpChange = viewModel::onIpChange,
                    onPortChange = viewModel::onPortChange,
                    onConnect = {
                        viewModel.connectFromForm()?.let(onConnect)
                    }
                )
            }
            item {
                Spacer(Modifier.height(8.dp))
                SectionLabel(stringResource(R.string.saved_devices_title))
            }
            if (state.devices.isEmpty()) {
                item { EmptyDevices() }
            } else {
                items(state.devices, key = { it.id }) { device ->
                    DeviceCard(
                        device = device,
                        onClick = { onConnect(viewModel.connectDevice(device)) },
                        onFavorite = { viewModel.toggleFavorite(device) },
                        onMore = { viewModel.openDeviceSheet(device) }
                    )
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    state.selectedDevice?.let { device ->
        ModalBottomSheet(
            onDismissRequest = viewModel::dismissSheets,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(device.name, style = MaterialTheme.typography.titleLarge)
                Text(
                    device.addressLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                SheetActionRow(
                    icon = Icons.Outlined.Link,
                    label = stringResource(R.string.sheet_connect),
                    onClick = { onConnect(viewModel.connectDevice(device)) }
                )
                SheetActionRow(
                    icon = if (device.favorite) Icons.Outlined.StarBorder else Icons.Outlined.Star,
                    label = stringResource(
                        if (device.favorite) R.string.action_unfavorite else R.string.action_favorite
                    ),
                    onClick = {
                        viewModel.toggleFavorite(device)
                        viewModel.dismissSheets()
                    }
                )
                SheetActionRow(
                    icon = Icons.Outlined.Edit,
                    label = stringResource(R.string.action_rename),
                    onClick = { viewModel.startRename(device) }
                )
                SheetActionRow(
                    icon = Icons.Outlined.Edit,
                    label = stringResource(R.string.action_edit),
                    onClick = { viewModel.startEdit(device) }
                )
                SheetActionRow(
                    icon = Icons.Outlined.Delete,
                    label = stringResource(R.string.action_delete),
                    onClick = { viewModel.startDelete(device) },
                    destructive = true
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    if (state.renamingDevice != null) {
        AlertDialog(
            onDismissRequest = viewModel::dismissSheets,
            title = { Text(stringResource(R.string.rename_device_title)) },
            text = {
                OutlinedTextField(
                    value = state.editName,
                    onValueChange = viewModel::onEditName,
                    label = { Text(stringResource(R.string.label_name)) },
                    isError = state.editNameError != null,
                    supportingText = state.editNameError?.let { { Text(it) } },
                    singleLine = true,
                    shape = InnerShape,
                    colors = fieldColors()
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::confirmRename) {
                    Text(stringResource(R.string.action_save_changes))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissSheets) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    if (state.editingDevice != null) {
        AlertDialog(
            onDismissRequest = viewModel::dismissSheets,
            title = { Text(stringResource(R.string.edit_device_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = state.editName,
                        onValueChange = viewModel::onEditName,
                        label = { Text(stringResource(R.string.label_name)) },
                        isError = state.editNameError != null,
                        supportingText = state.editNameError?.let { { Text(it) } },
                        singleLine = true,
                        shape = InnerShape,
                        colors = fieldColors()
                    )
                    OutlinedTextField(
                        value = state.editIp,
                        onValueChange = viewModel::onEditIp,
                        label = { Text(stringResource(R.string.label_ip)) },
                        isError = state.editIpError != null,
                        supportingText = state.editIpError?.let { { Text(it) } },
                        singleLine = true,
                        shape = InnerShape,
                        colors = fieldColors()
                    )
                    OutlinedTextField(
                        value = state.editPort,
                        onValueChange = viewModel::onEditPort,
                        label = { Text(stringResource(R.string.label_port)) },
                        isError = state.editPortError != null,
                        supportingText = state.editPortError?.let { { Text(it) } },
                        singleLine = true,
                        shape = InnerShape,
                        colors = fieldColors()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = viewModel::confirmEdit) {
                    Text(stringResource(R.string.action_save_changes))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissSheets) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    state.deletingDevice?.let { device ->
        AlertDialog(
            onDismissRequest = viewModel::dismissSheets,
            title = { Text(stringResource(R.string.delete_device_title)) },
            text = { Text(stringResource(R.string.delete_device_body, device.name)) },
            confirmButton = {
                TextButton(onClick = viewModel::confirmDelete) {
                    Text(stringResource(R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissSheets) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}
