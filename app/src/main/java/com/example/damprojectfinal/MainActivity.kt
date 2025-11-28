package com.example.damprojectfinal

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.damprojectfinal.ui.theme.DamProjectFinalTheme
import dagger.hilt.android.AndroidEntryPoint

class MainActivity : ComponentActivity() {
    private var deepLinkToken by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        deepLinkToken = extractTokenFromIntent(intent)

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