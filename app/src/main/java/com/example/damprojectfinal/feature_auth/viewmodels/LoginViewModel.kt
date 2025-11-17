package com.example.damprojectfinal.feature_auth.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.damprojectfinal.core.api.AuthApiService
import com.example.damprojectfinal.core.api.TokenManager
import com.example.damprojectfinal.core.dto.auth.LoginRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log
import java.io.IOException

// --- UI State for Login Screen ---
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val loginSuccess: Boolean = false,
    val userId: String? = null,
    val role: String? = null,
    val error: String? = null
)

// Callback type for navigation (Typealias is kept but unused for now)
typealias OnLoginSuccess = (userId: String, role: String) -> Unit

class LoginViewModel(
    private val authApiService: AuthApiService,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val TAG = "LoginViewModel"

    var uiState by mutableStateOf(LoginUiState())
        private set

    // --- Expose role for composable to observe ---
    private val _userRole = MutableStateFlow<String?>(null)
    val userRole: StateFlow<String?> = _userRole.asStateFlow()

    // --- Update form fields ---
    fun updateEmail(input: String) { uiState = uiState.copy(email = input, error = null) }
    fun updatePassword(input: String) { uiState = uiState.copy(password = input, error = null) }

    // --- Primary Login function that updates UI state (Signature: () -> Unit) ---
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

                Log.d(TAG, "Attempting login for: ${uiState.email}")
                val response = authApiService.login(request)
                Log.d(TAG, "Login successful - UserId: ${response.id}, Role: ${response.role}")

                // --- Use snake_case property names from response ---
                val accessToken = response.access_token
                val refreshToken = response.refresh_token

                if (accessToken.isNotEmpty() && refreshToken.isNotEmpty()) {
                    // --- Save tokens using TokenManager ---
                    tokenManager.saveTokens(
                        accessToken = accessToken,
                        refreshToken = refreshToken,
                        userId = response.id,
                        role = response.role
                    )
                    Log.d(TAG, "Tokens saved successfully")

                    // --- Verify tokens were saved ---
                    tokenManager.debugPrintAll()
                } else {
                    Log.w(TAG, "⚠️ Tokens are empty in login response")
                    // Still save user info without tokens
                    tokenManager.saveTokens(
                        accessToken = "",
                        refreshToken = "",
                        userId = response.id,
                        role = response.role
                    )
                }

                // --- Save role for LaunchedEffect to observe ---
                _userRole.value = response.role

                // --- Update UI state to trigger LaunchedEffect in Composable ---
                uiState = uiState.copy(
                    isLoading = false,
                    loginSuccess = true,
                    userId = response.id,
                    role = response.role,
                    error = null
                )

            } catch (e: Exception) {
                Log.e(TAG, "Login failed", e)
                val errorMessage = when (e) {
                    is IOException -> "Network error. Server may be down or URL incorrect."
                    is io.ktor.client.plugins.ClientRequestException -> "Invalid credentials. Please check your email and password."
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