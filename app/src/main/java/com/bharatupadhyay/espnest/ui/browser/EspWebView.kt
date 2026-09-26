package com.bharatupadhyay.espnest.ui.browser

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.http.SslError
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun EspWebView(
    url: String,
    javaScriptEnabled: Boolean,
    modifier: Modifier = Modifier,
    onCreated: (WebView) -> Unit,
    onProgress: (Int) -> Unit,
    onUrlChange: (String) -> Unit,
    onHistoryChange: (Boolean, Boolean) -> Unit,
    onPageStarted: () -> Unit,
    onPageFinished: () -> Unit,
    onReceivedError: () -> Unit
) {
    val holder = remember { WebViewHolder() }

    DisposableEffect(Unit) {
        onDispose {
            val view = holder.webView ?: return@onDispose
            view.stopLoading()
            (view.parent as? ViewGroup)?.removeView(view)
            view.webChromeClient = object : WebChromeClient() {}
            view.webViewClient = WebViewClient()
            view.destroy()
            holder.webView = null
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                settings.javaScriptEnabled = javaScriptEnabled
                settings.domStorageEnabled = true
                settings.databaseEnabled = true
                settings.cacheMode = WebSettings.LOAD_DEFAULT
                settings.mediaPlaybackRequiresUserGesture = false
                settings.setSupportZoom(true)
                settings.builtInZoomControls = true
                settings.displayZoomControls = false
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
                settings.allowFileAccess = false
                settings.allowContentAccess = true
                settings.javaScriptCanOpenWindowsAutomatically = false
                settings.setSupportMultipleWindows(false)
                isFocusable = true
                isFocusableInTouchMode = true

                CookieManager.getInstance().setAcceptCookie(true)
                CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

                webChromeClient = object : WebChromeClient() {
                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        onProgress(newProgress)
                    }
                }
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView,
                        request: WebResourceRequest
                    ): Boolean {
                        val scheme = request.url.scheme?.lowercase()
                        return scheme != "http" && scheme != "https"
                    }

                    override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                        onPageStarted()
                        if (url != null) onUrlChange(url)
                        onHistoryChange(view.canGoBack(), view.canGoForward())
                    }

                    override fun onPageFinished(view: WebView, url: String?) {
                        if (url != null) onUrlChange(url)
                        onHistoryChange(view.canGoBack(), view.canGoForward())
                        onPageFinished()
                    }

                    override fun onReceivedError(
                        view: WebView,
                        request: WebResourceRequest,
                        error: WebResourceError
                    ) {
                        if (request.isForMainFrame) onReceivedError()
                    }

                    override fun onReceivedSslError(
                        view: WebView?,
                        handler: SslErrorHandler?,
                        error: SslError?
                    ) {
                        handler?.cancel()
                    }
                }
                holder.webView = this
                onCreated(this)
                loadUrl(url)
            }
        },
        update = { webView ->
            webView.settings.javaScriptEnabled = javaScriptEnabled
            holder.webView = webView
            onCreated(webView)
        }
    )
}

private class WebViewHolder {
    var webView: WebView? = null
}
