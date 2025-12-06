// src/main/java/com.example/damprojectfinal/feature_auth.viewmodels/LoginViewModel.kt
package com.example.damprojectfinal.feature_auth.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.damprojectfinal.core.api.AuthApiService
import com.example.damprojectfinal.core.dto.auth.LoginRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import io.ktor.client.plugins.ClientRequestException
import com.example.damprojectfinal.core.api.TokenManager // <-- NEW IMPORT: TokenManager
import android.app.Application // <-- NEW IMPORT: Application context for TokenManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel // <-- CHANGE: Use AndroidViewModel to get Application context


// --- UI State for Login Screen ---
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val loginSuccess: Boolean = false,
    val userId: String? = null,
    val role: String? = null,
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val error: String? = null,
)

// Callback type for navigation (Typealias is kept but unused for now)
typealias OnLoginSuccess = (userId: String, role: String) -> Unit

// --- CHANGE: Extend AndroidViewModel to get access to Application context ---
class LoginViewModel(
    application: Application, // <--- NEW: Application context
    private val authApiService: AuthApiService
) : AndroidViewModel(application) { // <--- CHANGE: Extend AndroidViewModel

    // --- NEW: Initialize TokenManager ---
    private val tokenManager: TokenManager = TokenManager(application)
    // --- END NEW ---

    var uiState by mutableStateOf(LoginUiState())
        private set

    private val _userRole = MutableStateFlow<String?>(null)
    val userRole: StateFlow<String?> = _userRole.asStateFlow()

    fun updateEmail(input: String) { uiState = uiState.copy(email = input, error = null) }
    fun updatePassword(input: String) { uiState = uiState.copy(password = input, error = null) }

    fun login() {
        if (uiState.email.isBlank() || uiState.password.isBlank()) {
            uiState = uiState.copy(error = "Please enter both email and password.")
            return
        }

        uiState = uiState.copy(isLoading = true, error = null, loginSuccess = false)
        _userRole.value = null

        viewModelScope.launch {
            try {
                val request = LoginRequest(
                    email = uiState.email,
                    password = uiState.password,
                )

                val response = authApiService.login(request)

                // --- NEW: Format the role before saving ---
                val formattedOwnerType: String = when (response.role) {
                    "user" -> "UserAccount"
                    "professional" -> "ProfessionalAccount"
                    else -> {
                        // Handle unexpected role from backend
                        Log.e("LoginViewModel", "Received unexpected role from backend: ${response.role}")
                        uiState = uiState.copy(
                            isLoading = false,
                            error = "Login failed: Unknown user role."
                        )
                        return@launch // Stop execution if role is invalid
                    }
                }
                // --- END NEW ---

                // --- Save role for LaunchedEffect to observe (using formatted role) ---
                _userRole.value = formattedOwnerType

                // --- MODIFIED: Update UI state to trigger LaunchedEffect in Composable ---
                uiState = uiState.copy(
                    isLoading = false,
                    loginSuccess = true,
                    userId = response.id,
                    role = formattedOwnerType, // <--- Save formatted role to UI state
                    accessToken = response.access_token,
                    refreshToken = response.refresh_token,
                    error = null
                )
                // --- END MODIFIED ---

                // --- NEW: Save tokens and formatted role to TokenManager ---
                tokenManager.saveTokens(
                    accessToken = response.access_token,
                    refreshToken = response.refresh_token,
                    userId = response.id,
                    role = formattedOwnerType // <--- Save the formatted role here
                )
                // --- END NEW ---

            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is IOException -> "Network error. Server may be down or URL incorrect."
                    is ClientRequestException -> "Invalid credentials. Please check your email and password."
                    else -> "An unknown error occurred: ${e.message}"
                }
                uiState = uiState.copy(isLoading = false, error = errorMessage)
            }
        }
    }

    fun resetState() {
        uiState = uiState.copy(loginSuccess = false, error = null)
        _userRole.value = null
    }
}
