package com.example.damprojectfinal

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.damprojectfinal.core.retro.RetrofitClient
import com.example.damprojectfinal.core.`object`.KtorClient
import com.example.damprojectfinal.ui.theme.DamProjectFinalTheme
import java.net.ConnectException
import java.net.SocketTimeoutException

class MainActivity : ComponentActivity() {
    private var deepLinkToken by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set up global exception handler to catch uncaught network exceptions
        // This is a safety net for exceptions that might slip through coroutine error handling
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            // Check if this is a network-related exception from OkHttp/Retrofit
            val isNetworkException = exception is ConnectException || 
                                    exception is SocketTimeoutException ||
                                    (exception.message?.contains("Failed to connect", ignoreCase = true) == true) ||
                                    (exception.message?.contains("Connection refused", ignoreCase = true) == true)
            
            if (isNetworkException && thread.name.startsWith("OkHttp")) {
                // Network exception from OkHttp thread - log but don't crash
                Log.w("MainActivity", "Caught uncaught network exception in ${thread.name}: ${exception.message}")
                Log.w("MainActivity", "This is expected when backend is unavailable. App will continue running.")
            } else {
                // For all other exceptions, use the default handler
                defaultHandler?.uncaughtException(thread, exception)
            }
        }
        
        deepLinkToken = extractTokenFromIntent(intent)

        enableEdgeToEdge() // Keep your edge-to-edge setup
        RetrofitClient.appContext = applicationContext
        KtorClient.initialize(applicationContext)
        enableEdgeToEdge()
        setContent {
            DamProjectFinalTheme {
                AppNavigation(initialDeepLinkToken = deepLinkToken)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        val newToken = extractTokenFromIntent(intent)

        if (newToken != null) {
            Log.d("MainActivity", "✅ New deep link token: $newToken")
            deepLinkToken = newToken

            setContent {
                DamProjectFinalTheme {
                    AppNavigation(initialDeepLinkToken = newToken)
                }
            }
        }
    }

    private fun extractTokenFromIntent(intent: Intent?): String? {
        val data: Uri? = intent?.data

        if (data == null) {
            Log.d("MainActivity", "❌ No deep link data")
            return null
        }

        Log.d("MainActivity", "=== Deep Link Debug ===")
        Log.d("MainActivity", "Full URI: $data")

        // Méthode 1: Token dans le path (myapp://reset-password/TOKEN)
        val pathSegments = data.pathSegments
        if (pathSegments.isNotEmpty()) {
            val token = pathSegments.last()
            if (token.isNotBlank() && token != "reset-password") {
                Log.d("MainActivity", "✅ Token from path: $token")
                return token
            }
        }

        // Méthode 2: Token dans query params (?token=TOKEN)
        val queryToken = data.getQueryParameter("token")
        if (!queryToken.isNullOrBlank()) {
            Log.d("MainActivity", "✅ Token from query: $queryToken")
            return queryToken
        }

        Log.d("MainActivity", "❌ No token found")
        return null
    }
}