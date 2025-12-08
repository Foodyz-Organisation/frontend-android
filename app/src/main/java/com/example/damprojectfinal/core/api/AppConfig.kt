package com.example.damprojectfinal.core.api

object AppConfig {
    // Default value for development on Android emulator
    // Use centralized BaseUrlProvider
    val SOCKET_BASE_URL: String = BaseUrlProvider.BASE_URL

    // If your Socket.IO server uses a custom path, update this accordingly.
    // Default Socket.IO path is "/socket.io" — most servers use that.
    const val SOCKET_PATH: String = "/socket.io"
}
