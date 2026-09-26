package com.bharatupadhyay.espnest.ui.browser

import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bharatupadhyay.espnest.R
import com.bharatupadhyay.espnest.di.LocalAppContainer
import com.bharatupadhyay.espnest.domain.ConnectionTarget
import com.bharatupadhyay.espnest.ui.components.ErrorPanel
import com.bharatupadhyay.espnest.ui.components.EspTopBar
import com.bharatupadhyay.espnest.ui.components.InnerShape
import com.bharatupadhyay.espnest.ui.components.fieldColors
import com.bharatupadhyay.espnest.ui.theme.Moss

@Composable
fun BrowserScreen(
    target: ConnectionTarget,
    onBackToHome: () -> Unit
) {
    val container = LocalAppContainer.current
    val viewModel: BrowserViewModel = viewModel(
        key = "${target.ip}:${target.port}",
        factory = BrowserViewModel.factory(container, target)
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var webView by remember { mutableStateOf<WebView?>(null) }
    val snackbar = remember { SnackbarHostState() }

    DisposableEffect(Unit) {
        onDispose {
            webView?.apply {
                stopLoading()
                destroy()
            }
            webView = null
        }
    }

    LaunchedEffect(state.savedMessage) {
        val msg = state.savedMessage ?: return@LaunchedEffect
        snackbar.showSnackbar(msg)
        viewModel.consumeSavedMessage()
    }

    BackHandler {
        val view = webView
        if (view?.canGoBack() == true) view.goBack() else onBackToHome()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            EspTopBar(
                title = state.target.displayName,
                subtitle = state.currentUrl,
                onBack = {
                    val view = webView
                    if (view?.canGoBack() == true) view.goBack() else onBackToHome()
                },
                actions = {
                    StatusChip(state.status)
                    if (!state.alreadySaved) {
                        IconButton(onClick = viewModel::openSave) {
                            Icon(
                                imageVector = Icons.Outlined.BookmarkAdd,
                                contentDescription = stringResource(R.string.action_save)
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .navigationBarsPadding()
                    .height(52.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { webView?.goBack() },
                    enabled = state.canGoBack
                ) {
                    Icon(
                        Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = stringResource(R.string.action_back)
                    )
                }
                IconButton(
                    onClick = { webView?.goForward() },
                    enabled = state.canGoForward
                ) {
                    Icon(
                        Icons.AutoMirrored.Outlined.ArrowForward,
                        contentDescription = stringResource(R.string.action_forward)
                    )
                }
                IconButton(onClick = { viewModel.retry(webView) }) {
                    Icon(Icons.Outlined.Refresh, contentDescription = stringResource(R.string.action_reload))
                }
                IconButton(onClick = { webView?.loadUrl(target.url) }) {
                    Icon(Icons.Outlined.Home, contentDescription = stringResource(R.string.action_home))
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            EspWebView(
                url = target.url,
                javaScriptEnabled = state.javaScriptEnabled,
                modifier = Modifier.fillMaxSize(),
                onCreated = { created ->
                    webView = created
                },
                onProgress = viewModel::onProgress,
                onUrlChange = viewModel::onUrlChange,
                onHistoryChange = viewModel::onHistoryChange,
                onPageStarted = viewModel::onPageStarted,
                onPageFinished = viewModel::onPageFinished,
                onReceivedError = viewModel::onReceivedError
            )

            AnimatedVisibility(
                visible = state.status == BrowserStatus.Connecting && state.progress < 100,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
            ) {
                LinearProgressIndicator(
                    progress = { state.progress / 100f },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            AnimatedVisibility(
                visible = state.status == BrowserStatus.Unreachable || state.status == BrowserStatus.Offline,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.96f))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ErrorPanel(
                        title = stringResource(
                            if (state.status == BrowserStatus.Offline) {
                                R.string.browser_offline_title
                            } else {
                                R.string.browser_unreachable_title
                            }
                        ),
                        body = stringResource(
                            if (state.status == BrowserStatus.Offline) {
                                R.string.browser_offline_body
                            } else {
                                R.string.browser_unreachable_body
                            }
                        ),
                        actionLabel = stringResource(R.string.action_retry),
                        onAction = { viewModel.retry(webView) }
                    )
                }
            }
        }
    }

    if (state.showSaveDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissSave,
            title = { Text(stringResource(R.string.save_device_title)) },
            text = {
                OutlinedTextField(
                    value = state.saveName,
                    onValueChange = viewModel::onSaveName,
                    label = { Text(stringResource(R.string.label_name)) },
                    isError = state.saveNameError != null,
                    supportingText = state.saveNameError?.let { { Text(it) } },
                    singleLine = true,
                    shape = InnerShape,
                    colors = fieldColors()
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::confirmSave) {
                    Text(stringResource(R.string.action_save))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissSave) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
private fun StatusChip(status: BrowserStatus) {
    val (label, color) = when (status) {
        BrowserStatus.Connecting -> stringResource(R.string.status_connecting) to MaterialTheme.colorScheme.tertiary
        BrowserStatus.Connected -> stringResource(R.string.status_connected) to Moss
        BrowserStatus.Offline -> stringResource(R.string.status_offline) to MaterialTheme.colorScheme.error
        BrowserStatus.Unreachable -> stringResource(R.string.status_unreachable) to MaterialTheme.colorScheme.error
    }
    Row(
        modifier = Modifier.padding(end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .background(color, MaterialTheme.shapes.small)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
