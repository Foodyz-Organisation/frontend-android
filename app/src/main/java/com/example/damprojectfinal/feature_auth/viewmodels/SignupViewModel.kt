package com.example.damprojectfinal.feature_auth.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.damprojectfinal.core.api.AuthApiService
import com.example.damprojectfinal.core.dto.auth.UserSignupRequest

import io.ktor.client.plugins.*
import io.ktor.client.statement.request
import kotlinx.coroutines.launch
import java.io.IOException

data class SignupUiState(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val phone: String = "",
    val address: String = "",
    val isLoading: Boolean = false,
    val signupSuccess: Boolean = false,
    val googleLoginSuccess: Boolean = false,
    val validationError: String? = null,
    val error: String? = null
)

class SignupViewModel(application: android.app.Application) : androidx.lifecycle.AndroidViewModel(application) {

    private val authApiService = AuthApiService()
    private val tokenManager = com.example.damprojectfinal.core.api.TokenManager(application)
    private val notificationManager = com.example.damprojectfinal.core.utils.NotificationManager(application, tokenManager)

    var uiState by mutableStateOf(SignupUiState())
        private set

    fun updateUsername(input: String) { uiState = uiState.copy(username = input, validationError = null, error = null) }
    fun updateEmail(input: String) { uiState = uiState.copy(email = input, validationError = null, error = null) }
    fun updatePassword(input: String) { uiState = uiState.copy(password = input, validationError = null, error = null) }
    fun updatePhone(input: String) { uiState = uiState.copy(phone = input, validationError = null, error = null) }
    fun updateAddress(input: String) { uiState = uiState.copy(address = input, validationError = null, error = null) }

    /**
     * Validate current step before allowing progression
     * @param step 0 = Personal Data, 1 = Phone, 2 = Address
     * @return true if validation passes
     */
    fun validateStep(step: Int): Boolean {
        return when (step) {
            0 -> validatePersonalDataStep()
            1 -> validatePhoneStep()
            2 -> validateAddressStep()
            else -> false
        }
    }

    private fun validatePersonalDataStep(): Boolean {
        // Validate username
        val usernameValidation = com.example.damprojectfinal.core.utils.ValidationUtils.validateUsername(uiState.username)
        if (!usernameValidation.isValid) {
            uiState = uiState.copy(validationError = usernameValidation.errorMessage)
            return false
        }

        // Validate email
        val emailValidation = com.example.damprojectfinal.core.utils.ValidationUtils.validateEmail(uiState.email)
        if (!emailValidation.isValid) {
            uiState = uiState.copy(validationError = emailValidation.errorMessage)
            return false
        }

        // Validate password
        val passwordValidation = com.example.damprojectfinal.core.utils.ValidationUtils.validatePassword(uiState.password)
        if (!passwordValidation.isValid) {
            uiState = uiState.copy(validationError = passwordValidation.errorMessage)
            return false
        }

        // Check password strength - require STRONG (aligned with backend)
        val passwordStrength = com.example.damprojectfinal.core.utils.ValidationUtils.validatePasswordStrength(uiState.password)
        if (passwordStrength != com.example.damprojectfinal.core.utils.PasswordStrength.STRONG) {
            uiState = uiState.copy(
                validationError = "Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, one number, and one special character"
            )
            return false
        }

        return true
    }

    private fun validatePhoneStep(): Boolean {
        // Phone is optional, but if provided, must be valid
        if (uiState.phone.isNotBlank()) {
            // Phone should already have +216 prefix from UI formatting
            val phoneValidation = com.example.damprojectfinal.core.utils.ValidationUtils.validatePhone(uiState.phone)
            if (!phoneValidation.isValid) {
                uiState = uiState.copy(validationError = phoneValidation.errorMessage)
                return false
            }
        }
        return true
    }

    private fun validateAddressStep(): Boolean {
        // Address is optional in backend, but if provided, must be valid
        if (uiState.address.isNotBlank()) {
            val addressValidation = com.example.damprojectfinal.core.utils.ValidationUtils.validateAddress(uiState.address)
            if (!addressValidation.isValid) {
                uiState = uiState.copy(validationError = addressValidation.errorMessage)
                return false
            }
        }
        return true
    }

    fun signupUser() {
        if (uiState.username.isBlank() || uiState.email.isBlank() || uiState.password.isBlank()) {
            uiState = uiState.copy(error = "Please fill in all required fields.")
            return
        }

        uiState = uiState.copy(isLoading = true, error = null, signupSuccess = false, googleLoginSuccess = false)

        viewModelScope.launch {
            try {
                val request = UserSignupRequest(
                    username = uiState.username,
                    email = uiState.email,
                    password = uiState.password,
                    phone = uiState.phone.ifBlank { null },
                    address = uiState.address.ifBlank { null }
                )

                // 🔥 Try the API call
                val response = authApiService.userSignup(request)

                uiState = uiState.copy(
                    isLoading = false,
                    signupSuccess = true,
                    error = "✅ Registration successful: ${response.message}"
                )

            } catch (e: Exception) {
                // 🔍 Log full error details in Logcat
                Log.e("SignupViewModel", "Signup error", e)

                val errorMsg = when (e) {
                    is io.ktor.client.plugins.ClientRequestException -> e.message ?: "Email already exists or invalid request."
                    is java.io.IOException -> "Network error. Please check your internet connection."
                    else -> e.message ?: "An unexpected error occurred."
                }

                // If email already exists, take user back to Step 0 (handled in UI via LaunchedEffect)
                uiState = uiState.copy(
                    isLoading = false,
                    signupSuccess = false,
                    error = errorMsg
                )
            }
        }
    }

    fun loginWithGoogle(
        idToken: String,
        email: String? = null,
        displayName: String? = null
    ) {
        Log.d("SignupViewModel", "========== loginWithGoogle() called ==========")
        
        uiState = uiState.copy(
            isLoading = true,
            error = null,
            signupSuccess = false,
            googleLoginSuccess = false
        )

        viewModelScope.launch {
            try {
                // Create request with only idToken
                val request = com.example.damprojectfinal.core.dto.auth.GoogleLoginRequest(
                    idToken = idToken
                )
                
                Log.d("SignupViewModel", "Sending Google login request to backend...")
                // Reuse the same loginWithGoogle from authApiService
                val response = authApiService.loginWithGoogle(request)
                Log.d("SignupViewModel", "Google login/signup successful - UserId: ${response.id}")
                
                // We do NOT save tokens here, as the user wants to be redirected to Login to sign in explicitly.
                // Just marking success is enough to trigger navigation.

                // Update UI state - using googleLoginSuccess to trigger navigation to Login
                uiState = uiState.copy(
                    isLoading = false,
                    googleLoginSuccess = true,
                    error = null
                )
                
            } catch (e: Exception) {
                Log.e("SignupViewModel", "Google login failed", e)
                uiState = uiState.copy(
                    isLoading = false,
                    error = "Google Sign-In failed: ${e.message}"
                )
            }
        }
    }

    fun resetState() {
        uiState = uiState.copy(signupSuccess = false, googleLoginSuccess = false, validationError = null, error = null)
    }
}
