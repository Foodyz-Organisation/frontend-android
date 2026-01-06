package com.example.damprojectfinal.feature_auth.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.damprojectfinal.core.api.AuthApiService
import com.example.damprojectfinal.core.api.TokenManager
import com.example.damprojectfinal.core.utils.NotificationManager
import com.example.damprojectfinal.core.dto.auth.LoginRequest
import kotlinx.coroutines.launch
import io.ktor.client.plugins.ClientRequestException
import java.io.IOException

private const val TAG = "LoginViewModel"

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val loginSuccess: Boolean = false,
    val userNotFound: Boolean = false,
    val validationError: String? = null,
    val userId: String? = null,
    val role: String? = null,
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val error: String? = null,
)

class LoginViewModel(
    private val authApiService: AuthApiService,
    private val tokenManager: TokenManager,
    private val notificationManager: NotificationManager
) : ViewModel() {

    var uiState by mutableStateOf(LoginUiState())
        private set

    // Expose user role from TokenManager as a Flow
    val userRole: kotlinx.coroutines.flow.Flow<String?> = tokenManager.getUserRole()

    fun updateEmail(email: String) {
        uiState = uiState.copy(email = email, validationError = null, error = null)
    }

    fun updatePassword(password: String) {
        uiState = uiState.copy(password = password, validationError = null, error = null)
    }

    fun login() {
        // Validate email
        val emailValidation = com.example.damprojectfinal.core.utils.ValidationUtils.validateEmail(uiState.email)
        if (!emailValidation.isValid) {
            uiState = uiState.copy(validationError = emailValidation.errorMessage)
            return
        }

        // Validate password
        val passwordValidation = com.example.damprojectfinal.core.utils.ValidationUtils.validatePassword(uiState.password)
        if (!passwordValidation.isValid) {
            uiState = uiState.copy(validationError = passwordValidation.errorMessage)
            return
        }

        uiState = uiState.copy(isLoading = true, error = null, validationError = null, loginSuccess = false)

        viewModelScope.launch {
            try {
                val request = LoginRequest(
                    email = uiState.email,
                    password = uiState.password
                )
                val response = authApiService.login(request)
                
                Log.d(TAG, "Login successful: ${response.id}")

                // Save tokens
                tokenManager.saveTokens(
                    accessToken = response.access_token,
                    refreshToken = response.refresh_token,
                    userId = response.id,
                    role = response.role
                )

                // Sync FCM Token
                try {
                    com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val fcmToken = task.result
                            notificationManager.syncTokenWithBackend(fcmToken)
                        } 
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "FCM Sync failed", e)
                }

                uiState = uiState.copy(
                    isLoading = false,
                    loginSuccess = true,
                    userId = response.id,
                    role = response.role,
                    accessToken = response.access_token,
                    refreshToken = response.refresh_token
                )
            } catch (e: Exception) {
                Log.e(TAG, "Login failed", e)
                uiState = uiState.copy(
                    isLoading = false,
                    error = e.message ?: "Login failed"
                )
            }
        }
    }

    fun loginWithGoogle(
        idToken: String,
        email: String?,
        displayName: String?,
        profilePictureUrl: String?
    ) {
        Log.d(TAG, "========== loginWithGoogle() called ==========")
        Log.d(TAG, "Email: $email, Name: $displayName")
        
        uiState = uiState.copy(
            isLoading = true,
            error = null,
            loginSuccess = false,
            userNotFound = false
        )

        viewModelScope.launch {
            try {
                // Create request with only idToken
                val request = com.example.damprojectfinal.core.dto.auth.GoogleLoginRequest(
                    idToken = idToken
                )
                
                Log.d(TAG, "Sending Google login request to backend...")
                val response = authApiService.loginWithGoogle(request)
                Log.d(TAG, "Google login successful - UserId: ${response.id}, Role: ${response.role}")
                
                val accessToken = response.access_token
                val refreshToken = response.refresh_token
                val userId = response.id
                
                // Determine role priority
                val prioritizedRole = if (response.role.equals("PROFESSIONAL", ignoreCase = true))
                    "PROFESSIONAL"
                else
                    "USER"
                
                Log.d(TAG, "Saving tokens for Google user...")
                
                tokenManager.saveTokens(
                    accessToken = accessToken,
                    refreshToken = refreshToken,
                    userId = userId,
                    role = prioritizedRole
                )
                
                Log.i(TAG, "Google login complete. Tokens saved.")
                
                // Sync FCM Token
                try {
                    com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val fcmToken = task.result
                            Log.d(TAG, "🔥 Syncing FCM token after Google login: $fcmToken")
                            notificationManager.syncTokenWithBackend(fcmToken)
                        } else {
                            Log.w(TAG, "Failed to get FCM token after Google login", task.exception)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error getting FCM token after Google login", e)
                }
                
                uiState = uiState.copy(
                    isLoading = false,
                    loginSuccess = true,
                    userId = userId,
                    role = prioritizedRole,
                    accessToken = accessToken,
                    refreshToken = refreshToken,
                    error = null
                )
                
            } catch (e: Exception) {
                Log.e(TAG, "Google login failed", e)
                
                if (e is ClientRequestException) {
                     val status = e.response.status.value
                     if (status == 404 || status == 401) { 
                         uiState = uiState.copy(
                             isLoading = false,
                             userNotFound = true,
                             error = null
                         )
                         return@launch
                     }
                }
                
                uiState = uiState.copy(
                    isLoading = false,
                    error = "Google Sign-In failed: ${e.message}"
                )
            }
        }
    }

    fun resetState() {
        uiState = uiState.copy(
            loginSuccess = false,
            userNotFound = false,
            validationError = null,
            error = null,
            role = null,
            userId = null
        )
        Log.d(TAG, "State reset completed.")
    }
}