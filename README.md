# ESPNest

**Your ESP32, one tap away.**

ESPNest is a local-first Android app for opening ESP32-hosted web panels in a dedicated WebView. There are no accounts, no cloud, no MQTT, no mDNS, and no analytics. Your phone talks HTTP to the ESP32 on the same Wi-Fi network.

- Application ID: `com.bharatupadhyay.espnest`
- Version: `1.0.0` (versionCode 1)
- minSdk 26 · targetSdk 35 · compileSdk 35
- Kotlin 2.0.21 · AGP 8.7.3 · Compose BOM 2024.12.01
- Dark-first Material 3

## Open in Android Studio

1. Install **Android Studio Ladybug / Koala or newer** (AGP 8.7 needs a recent IDE).
2. **File → Open** and select this folder: `ESPNest-Android`.
3. When prompted, use the Gradle wrapper included in the project (Gradle 8.9).
4. Wait for Gradle sync. If SDK 35 is missing, install it from SDK Manager.
5. Connect a phone or start an emulator with API 26+.
6. Click **Run**.

If `local.properties` is missing, Android Studio writes `sdk.dir` on first open. You can also create it yourself:

```
sdk.dir=/path/to/Android/sdk
```

## Same Wi-Fi as the ESP32

ESPNest loads `http://<ip>` or `http://<ip>:<port>` (port 80 is omitted). The phone and the ESP32 must share a network:

- Station mode: both on the same home/office Wi-Fi.
- SoftAP mode: join the ESP32’s own AP (often `192.168.4.1`).

Cleartext HTTP is already enabled for local use (`usesCleartextTraffic` plus `network_security_config.xml`). Do not expect this app to reach devices across the internet.

## Permissions

| Permission | Why |
| --- | --- |
| `INTERNET` | Load the ESP32 web UI in WebView and probe reachability. |
| `ACCESS_NETWORK_STATE` | Show Wi-Fi ready / No Wi-Fi and skip a false “connected” state. |
| `ACCESS_WIFI_STATE` | Distinguish Wi-Fi from cellular so the chip stays honest. |

No location, camera, storage, or notification permission is requested.

## Build a debug APK

In Android Studio:

1. **Build → Build Bundle(s) / APK(s) → Build APK(s)**
2. When the build finishes, click **locate** and copy `app/build/outputs/apk/debug/app-debug.apk`

From a terminal (SDK configured):

```bash
./gradlew :app:assembleDebug
```

Release builds are R8-minified (`assembleRelease`). You still need a signing config for a Play-ready AAB.

## How it works

1. Type an IPv4 address and port (default 80) and tap **CONNECT**, or tap a saved device.
2. ESPNest probes the URL with `HttpURLConnection` (timeout 5 / 8 / 10 / 15 s, default 8 s).
3. A full-screen WebView loads the panel. Chrome, Custom Tabs, and the system browser are never opened.
4. Hardware back goes through WebView history, then Home.
5. Save the device from the browser if it is not already in the list.

First launch seeds three samples **only if the database is empty**:

| Name | Address |
| --- | --- |
| VeerKavach | 192.168.4.1:80 (favorite) |
| Garden Controller | 192.168.1.105:80 (favorite) |
| ESP32 Sensor | 192.168.1.120:80 |

## Project structure

```
ESPNest-Android/
  settings.gradle.kts
  build.gradle.kts
  gradle/libs.versions.toml
  app/
    build.gradle.kts
    src/main/
      AndroidManifest.xml
      java/com/bharatupadhyay/espnest/
        ESPNestApplication.kt
        MainActivity.kt
        di/AppContainer.kt
        data/local/          Room + DataStore
        data/repository/
        domain/              Device, IP validation, URL, NetworkMonitor
        ui/home/
        ui/browser/          WebView + probe overlay
        ui/settings/
        ui/theme/
        ui/components/
      res/                   strings, theme, network security, adaptive icon
```

Architecture is MVVM with a small manual `AppContainer` (no Hilt). Room stores devices locally. DataStore holds theme, JavaScript, and probe timeout.

## Settings

- Appearance: Dark / Light / System (Dark is the default).
- Browser: JavaScript (on by default), clear cookies/cache/WebStorage, delete all saved devices.
- Network: connection timeout.
- About: ESPNest 1.0.0, developer Bharat Upadhyay, contact 9929974317.

## Future-ready (not implemented)

These were deliberately left out so the app stays local and simple:

- mDNS / `_http._tcp` discovery
- QR provisioning
- MQTT / Firebase / accounts
- HTTPS client certificates
- Widget or Wear companion

The `Device` model and repository are small enough to grow into those later without a rewrite.

## License

Private project for Bharat Upadhyay.
