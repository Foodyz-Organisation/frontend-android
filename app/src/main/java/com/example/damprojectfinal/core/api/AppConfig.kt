package com.example.damprojectfinal.core.api

/**
 * Central app configuration for development/runtime tweaks.
 *
 * Change SOCKET_BASE_URL depending on where you run the app:
 * - Android emulator (AVD): use "http://10.0.2.2:3000" to reach the host machine
 * - Physical device on same Wi-Fi network: use "http://<HOST_IP>:3000" (e.g. http://192.168.1.15:3000)
 * - For production, replace with your production server URL (https://...)
 */
object AppConfig {
    // Default value for development on Android emulator
    const val SOCKET_BASE_URL: String = "http://10.0.2.2:3000"

    // If your Socket.IO server uses a custom path, update this accordingly.
    // Default Socket.IO path is "/socket.io" — most servers use that.
    const val SOCKET_PATH: String = "/socket.io"
}
