package com.example.damprojectfinal.core.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.UUID

/**
 * Helper class for Google OAuth2 authentication
 * Handles Google Sign-In using Credential Manager API
 */
class GoogleAuthHelper(private val context: Context) {

    private val credentialManager = CredentialManager.create(context)
    
    companion object {
        private const val TAG = "GoogleAuthHelper"
        // Web Client ID from Google Cloud Console - MUST match backend
        private const val WEB_CLIENT_ID = "152459113648-e01p479h7v2cjidjao7jnp4pph6iho2a.apps.googleusercontent.com"
    }

    /**
     * Data class to hold Google account information
     */
    data class GoogleAccountInfo(
        val idToken: String,
        val email: String?,
        val displayName: String?,
        val profilePictureUrl: String?,
        val givenName: String?,
        val familyName: String?
    )

    /**
     * Launch Google Sign-In flow using Credential Manager
     * @param onSuccess Callback with Google account information
     * @param onError Callback with error message
     */
    fun signIn(
        scope: CoroutineScope,
        onSuccess: (GoogleAccountInfo) -> Unit,
        onError: (String) -> Unit
    ) {
        scope.launch {
            try {
                Log.d(TAG, "========== Google Sign-In Debug ==========")
                Log.d(TAG, "Web Client ID: $WEB_CLIENT_ID")
                Log.d(TAG, "Attempting to get credentials from Credential Manager...")
                
                val result = withContext(Dispatchers.IO) {
                    // Generate a nonce for security
                    val nonce = generateNonce()
                    val hashedNonce = hashNonce(nonce)

                    // Configure Google ID option
                    val googleIdOption = GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(false) // Show all accounts
                        .setServerClientId(WEB_CLIENT_ID)
                        .setNonce(hashedNonce)
                        .build()

                    // Build credential request
                    val request = GetCredentialRequest.Builder()
                        .addCredentialOption(googleIdOption)
                        .build()

                    // Get credential
                    credentialManager.getCredential(
                        request = request,
                        context = context
                    )
                }

                // Handle the credential result
                val credential = result.credential
                
                when (credential) {
                    is com.google.android.libraries.identity.googleid.GoogleIdTokenCredential -> {
                        // Extract Google account information
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        
                        val accountInfo = GoogleAccountInfo(
                            idToken = googleIdTokenCredential.idToken,
                            email = googleIdTokenCredential.id, // Email is in the ID field
                            displayName = googleIdTokenCredential.displayName,
                            profilePictureUrl = googleIdTokenCredential.profilePictureUri?.toString(),
                            givenName = googleIdTokenCredential.givenName,
                            familyName = googleIdTokenCredential.familyName
                        )
                        
                        Log.d(TAG, "Google Sign-In successful: ${accountInfo.email}")
                        onSuccess(accountInfo)
                    }
                    else -> {
                        Log.e(TAG, "Unexpected credential type: ${credential::class.java.name}")
                        onError("Unexpected credential type received")
                    }
                }
                
            } catch (e: GetCredentialException) {
                Log.e(TAG, "========== Google Sign-In Failed ==========")
                Log.e(TAG, "Error Type: ${e::class.simpleName}")
                Log.e(TAG, "Error Message: ${e.message}")
                Log.e(TAG, "===========================================")
                
                // Provide user-friendly error messages with troubleshooting
                val errorMsg = when {
                    e.message?.contains("No credentials available") == true -> {
                        Log.w(TAG, "⚠️ TROUBLESHOOTING:")
                        Log.w(TAG, "1. Open Settings app on your device")
                        Log.w(TAG, "2. Go to Accounts (or Users & accounts)")
                        Log.w(TAG, "3. Tap 'Add account' → Select 'Google'")
                        Log.w(TAG, "4. Sign in with any Gmail account")
                        Log.w(TAG, "5. Return to this app and try again")
                        "No Google account found. Please add a Google account to your device in Settings."
                    }
                    e.message?.contains("user canceled") == true || 
                    e.message?.contains("CANCELED") == true -> {
                        Log.i(TAG, "User canceled the sign-in flow")
                        "Sign-In canceled. Please try again."
                    }
                    else -> {
                        Log.e(TAG, "Unexpected error during Google Sign-In", e)
                        "Sign-In failed: ${e.message ?: "Unknown error"}"
                    }
                }
                
                onError(errorMsg)
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error during Google Sign-In", e)
                onError("Unexpected error: ${e.message}")
            }
        }
    }

    /**
     * Generate a random nonce for security
     */
    private fun generateNonce(): String {
        return UUID.randomUUID().toString()
    }

    /**
     * Hash the nonce using SHA-256
     */
    private fun hashNonce(nonce: String): String {
        val bytes = nonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    /**
     * Sign out from Google (clears cached credentials)
     */
    fun signOut(onComplete: () -> Unit) {
        // Credential Manager doesn't have explicit sign-out
        // The user will be prompted to select account on next sign-in
        Log.d(TAG, "Google Sign-Out requested")
        onComplete()
    }
}
