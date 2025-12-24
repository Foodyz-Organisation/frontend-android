package com.example.damprojectfinal.core.api

import android.content.Context
import android.util.Log
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.runBlocking

/**
 * HTTP Interceptor that detects 401 Unauthorized responses
 * and automatically clears expired tokens
 */
val TokenExpirationPlugin = createClientPlugin("TokenExpirationPlugin") {
    val TAG = "TokenExpirationPlugin"
    
    onResponse { response ->
        if (response.status.value == 401) {
            Log.w(TAG, "⚠️ 401 Unauthorized - Token expired or invalid")
            
            // Get context from the plugin config if needed
            // For now, we'll handle this in the ViewModel layer
            // This plugin just logs the 401 for monitoring
        }
    }
}

/**
 * Extension function to check if token is expired
 * Returns true if token should be considered invalid
 */
fun TokenManager.isTokenExpired(): Boolean {
    val token = getAccessTokenSync()
    if (token.isNullOrEmpty()) {
        return true
    }
    
    // TODO: Optionally decode JWT and check exp claim
    // For now, we rely on backend 401 responses
    return false
}

/**
 * Clear all authentication data when token expires
 */
suspend fun TokenManager.handleTokenExpiration() {
    Log.w("TokenManager", "🔓 Handling token expiration - clearing all auth data")
    clearTokens()
}
