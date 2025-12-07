package com.example.damprojectfinal.core.api

object AppConfig {
    // Default value for development on Android emulator
    const val SOCKET_BASE_URL: String = "http://10.0.2.2:3000"

    // If your Socket.IO server uses a custom path, update this accordingly.
    // Default Socket.IO path is "/socket.io" — most servers use that.
    const val SOCKET_PATH: String = "/socket.io"
}
