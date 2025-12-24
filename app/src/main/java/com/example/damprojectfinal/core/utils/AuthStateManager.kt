package com.example.damprojectfinal.core.utils

import android.content.Context
import android.util.Log
import com.example.damprojectfinal.core.api.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Global authentication state manager
 * Handles token expiration and logout across the app
 */
object AuthStateManager {
    private const val TAG = "AuthStateManager"
    
    /**
     * Handle 401 Unauthorized error - clear tokens and trigger logout
     * Call this from any ViewModel when you receive a 401 error
     */
    fun handle401Error(
        context: Context,
        scope: CoroutineScope,
        onLogoutComplete: () -> Unit = {}
    ) {
        Log.w(TAG, "🔓 401 Unauthorized detected - clearing authentication")
        
        scope.launch {
            try {
                val tokenManager = TokenManager(context)
                tokenManager.clearTokens()
                Log.i(TAG, "✅ Tokens cleared successfully")
                onLogoutComplete()
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error clearing tokens: ${e.message}", e)
            }
        }
    }
    
    /**
     * Check if an exception is a 401 Unauthorized error
     */
    fun is401Error(exception: Exception): Boolean {
        return exception is io.ktor.client.plugins.ClientRequestException &&
                exception.response.status.value == 401
    }
}
