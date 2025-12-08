package com.example.damprojectfinal.feature_auth.viewmodels

import android.util.Log
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
import java.io.IOException
private const val TAG = "LoginViewModel"

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
    private val authApiService: AuthApiService,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val TAG = "LoginViewModel"

    var uiState by mutableStateOf(LoginUiState())
        private set

    private val _userRole = MutableStateFlow<String?>(null)
    val userRole: StateFlow<String?> = _userRole.asStateFlow()

    fun updateEmail(input: String) {
        uiState = uiState.copy(email = input, error = null)
    }

    fun updatePassword(input: String) {
        uiState = uiState.copy(password = input, error = null)
    }

    fun login() {
        Log.d(TAG, "========== login() function called ==========")
        Log.d(TAG, "Current email: '${uiState.email}', password length: ${uiState.password.length}")
        Log.d(TAG, "IsLoading: ${uiState.isLoading}")
        
        if (uiState.email.isBlank() || uiState.password.isBlank()) {
            uiState = uiState.copy(error = "Please enter both email and password.")
            Log.w(TAG, "Login attempt blocked: Empty fields.") // ⬅️ DEBUG
            return
        }

        // Reset all relevant state for the new login attempt
        uiState = uiState.copy(
            isLoading = true,
            error = null,
            loginSuccess = false,
            role = null,
            userId = null
        )
        Log.d(TAG, "Starting login for: ${uiState.email}") // ⬅️ DEBUG

        viewModelScope.launch {
            try {
                val request = LoginRequest(
                    email = uiState.email,
                    password = uiState.password,
                )

                Log.d(TAG, "Attempting login for: ${uiState.email}")
                val response = authApiService.login(request)
                Log.d(TAG, "Login successful - UserId: ${response.id}, Role: ${response.role}")



                // 🛑 FIX: Extract required non-null fields or throw if API contract is violated
                val accessToken = response.access_token
                    ?: throw IllegalStateException("Access token missing from successful response.")
                val refreshToken = response.refresh_token
                    ?: throw IllegalStateException("Refresh token missing from successful response.")
                val userId = response.id
                    ?: throw IllegalStateException("User ID missing from successful response.")

                // --- Save role for LaunchedEffect to observe ---
                _userRole.value = response.role

                // Your priority logic is good
                val prioritizedRole =
                    if (response.role.equals("PROFESSIONAL", ignoreCase = true))
                        "PROFESSIONAL"
                    else
                        "USER"

                Log.d(TAG, "Prioritized Role: $prioritizedRole. Saving tokens...") // ⬅️ DEBUG

                // Save tokens + id + PRIORITIZED ROLE
                tokenManager.saveTokens(
                    accessToken = accessToken, // Now guaranteed String
                    refreshToken = refreshToken, // Now guaranteed String
                    userId = userId, // Now guaranteed String
                    role = prioritizedRole
                )

                Log.i(TAG, "Tokens and User ID saved. Login COMPLETE.") // ⬅️ DEBUG

                // UPDATE UI STATE: This is the single source of truth
                uiState = uiState.copy(
                    isLoading = false,
                    loginSuccess = true,
                    userId = response.id,
                    role = prioritizedRole,
                    accessToken = response.access_token,
                    refreshToken = response.refresh_token,
                    error = null
                )
                // --- END MODIFIED ---



            } catch (e: Exception) {
                Log.e(TAG, "Login failed", e)
                
                // Log the BASE_URL being used for debugging
                try {
                    val baseUrl = com.example.damprojectfinal.core.api.BaseUrlProvider.BASE_URL
                    Log.e(TAG, "🔗 Attempted connection to: $baseUrl/auth/login")
                } catch (ex: Exception) {
                    Log.e(TAG, "Could not retrieve BASE_URL", ex)
                }
                
                val errorMessage = when (e) {
                    is IOException, is java.net.ConnectException -> {
                        val baseUrl = try {
                            com.example.damprojectfinal.core.api.BaseUrlProvider.BASE_URL
                        } catch (ex: Exception) {
                            "unknown"
                        }
                        Log.e(TAG, "Login FAILED: Network Error. ${e.message}") // ⬅️ DEBUG
                        Log.e(TAG, "💡 Troubleshooting:")
                        Log.e(TAG, "   1. Is backend server running on port 3000?")
                        Log.e(TAG, "   2. Is your computer IP correct? (Current: $baseUrl)")
                        Log.e(TAG, "   3. Are phone and computer on same WiFi?")
                        Log.e(TAG, "   4. Is firewall blocking port 3000?")
                        "Cannot connect to server at $baseUrl. Check:\n• Backend server is running\n• Correct IP address\n• Same WiFi network\n• Firewall settings"
                    }
                    is io.ktor.client.plugins.ClientRequestException -> {
                        // This typically means 401 Unauthorized (Invalid credentials)
                        Log.e(TAG, "Login FAILED: Invalid Credentials/Client Error (Status: ${e.response.status}).") // ⬅️ DEBUG
                        "Invalid credentials. Please check your email and password."
                    }
                    is IllegalStateException -> {
                        // Catches the error if the API returned a null token/ID unexpectedly
                        Log.e(TAG, "Login FAILED: Invalid API contract. ${e.message}") // ⬅️ DEBUG
                        "Login failed due to missing server data. Please try again."
                    }
                    else -> {
                        Log.e(TAG, "Login FAILED: Unknown Error.", e) // ⬅️ DEBUG
                        "An unknown error occurred: ${e.message}"
                    }
                }

                uiState = uiState.copy(isLoading = false, error = errorMessage)
            }
        }
    }

    fun resetState() {
        uiState = uiState.copy(
            loginSuccess = false,
            error = null,
            role = null,
            userId = null
        )
        Log.d(TAG, "State reset completed.") // ⬅️ DEBUG
    }
}