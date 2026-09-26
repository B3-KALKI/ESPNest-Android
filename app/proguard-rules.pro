# ESPNest — keep local Room models and WebView callbacks intact.

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers class * {
    @androidx.room.* <fields>;
}
-dontwarn androidx.room.paging.**
-dontwarn androidx.room.paging.LimitOffsetDataSource

# Kotlin / Coroutines
-dontwarn kotlinx.coroutines.**
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# WebView
-keepclassmembers class * extends android.webkit.WebViewClient {
    public *(android.webkit.WebView, java.lang.String);
    public *(android.webkit.WebView, java.lang.String, android.graphics.Bitmap);
    public *(android.webkit.WebView, android.webkit.WebResourceRequest);
    public *(android.webkit.WebView, android.webkit.WebResourceRequest, android.webkit.WebResourceError);
    public *(android.webkit.WebView, android.webkit.SslErrorHandler, android.net.http.SslError);
}
-keepclassmembers class * extends android.webkit.WebChromeClient {
    public *(android.webkit.WebView, int);
}
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# DataStore / Preferences
-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite {
    <fields>;
}

# Compose
-dontwarn androidx.compose.**
