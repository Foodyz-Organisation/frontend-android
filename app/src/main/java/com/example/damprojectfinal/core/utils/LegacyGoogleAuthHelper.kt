package com.example.damprojectfinal.core.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task

/**
 * Legacy Google Sign-In Helper using GoogleSignInClient
 * Shows browser-based account picker (works without device Google account)
 */
class LegacyGoogleAuthHelper(private val context: Context) {

    companion object {
        private const val TAG = "LegacyGoogleAuthHelper"
        // Web Client ID from Google Cloud Console
        private const val WEB_CLIENT_ID = "152459113648-e01p479h7v2cjidjao7jnp4pph6iho2a.apps.googleusercontent.com"
    }

    /**
     * Data class to hold Google account information
     */
    data class GoogleAccountInfo(
        val idToken: String,
        val email: String?,
        val displayName: String?,
        val profilePictureUrl: String?
    )

    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(WEB_CLIENT_ID)
            .requestEmail()
            .requestProfile()
            .build()

        GoogleSignIn.getClient(context, gso)
    }

    /**
     * Get the sign-in intent to launch with ActivityResultLauncher
     */
    fun getSignInIntent(): Intent {
        Log.d(TAG, "Creating Google Sign-In intent")
        return googleSignInClient.signInIntent
    }

    /**
     * Handle the result from Google Sign-In activity
     * Call this from your ActivityResultLauncher callback
     */
    fun handleSignInResult(
        data: Intent?,
        onSuccess: (GoogleAccountInfo) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)

            if (account != null) {
                val idToken = account.idToken
                
                if (idToken != null) {
                    val accountInfo = GoogleAccountInfo(
                        idToken = idToken,
                        email = account.email,
                        displayName = account.displayName,
                        profilePictureUrl = account.photoUrl?.toString()
                    )
                    
                    Log.d(TAG, "✅ Google Sign-In successful: ${accountInfo.email}")
                    onSuccess(accountInfo)
                } else {
                    Log.e(TAG, "❌ ID token is null")
                    onError("Failed to get ID token from Google")
                }
            } else {
                Log.e(TAG, "❌ Account is null")
                onError("Failed to get account information")
            }
            
        } catch (e: ApiException) {
            Log.e(TAG, "❌ Google Sign-In failed with status code: ${e.statusCode}", e)
            
            val errorMessage = when (e.statusCode) {
                10 -> {
                    Log.e(TAG, "⚠️ DEVELOPER ERROR - Configuration issue!")
                    Log.e(TAG, "Fix: Add your app's SHA-1 fingerprint to Google Cloud Console")
                    Log.e(TAG, "1. Run: ./gradlew signingReport")
                    Log.e(TAG, "2. Copy SHA-1 from debug keystore")
                    Log.e(TAG, "3. Add to Google Cloud Console → Credentials → OAuth Client")
                    "Configuration error. Please check SHA-1 fingerprint in Google Cloud Console."
                }
                12501 -> "Sign-In canceled by user"
                12500 -> "Sign-In failed. Please try again."
                7 -> "Network error. Check your internet connection."
                else -> "Sign-In failed (code ${e.statusCode}): ${e.message}"
            }
            
            onError(errorMessage)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Unexpected error during Google Sign-In", e)
            onError("Unexpected error: ${e.message}")
        }
    }

    /**
     * Sign out from Google
     */
    fun signOut(onComplete: () -> Unit = {}) {
        googleSignInClient.signOut().addOnCompleteListener {
            Log.d(TAG, "🔓 Google Sign-Out complete")
            onComplete()
        }
    }

    /**
     * Revoke access (disconnect account)
     */
    fun revokeAccess(onComplete: () -> Unit = {}) {
        googleSignInClient.revokeAccess().addOnCompleteListener {
            Log.d(TAG, "🔓 Google access revoked")
            onComplete()
        }
    }
}
