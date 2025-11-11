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
    val error: String? = null
)

class SignupViewModel : ViewModel() {

    private val authApiService = AuthApiService()

    var uiState by mutableStateOf(SignupUiState())
        private set

    fun updateUsername(input: String) { uiState = uiState.copy(username = input, error = null) }
    fun updateEmail(input: String) { uiState = uiState.copy(email = input, error = null) }
    fun updatePassword(input: String) { uiState = uiState.copy(password = input, error = null) }
    fun updatePhone(input: String) { uiState = uiState.copy(phone = input, error = null) }
    fun updateAddress(input: String) { uiState = uiState.copy(address = input, error = null) }

    fun signupUser() {
        if (uiState.username.isBlank() || uiState.email.isBlank() || uiState.password.isBlank()) {
            uiState = uiState.copy(error = "Please fill in all required fields.")
            return
        }

        uiState = uiState.copy(isLoading = true, error = null, signupSuccess = false)

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

                val errorMessage = when (e) {
                    is IOException -> """
                        🌐 Network error.
                        Cause: ${e.cause}
                        Message: ${e.message}
                        Check: Internet connection or BASE_URL.
                    """.trimIndent()

                    is ClientRequestException -> """
                        🚫 Client request failed.
                        Status: ${e.response.status}
                        URL: ${e.response.request.url}
                        Message: ${e.message}
                    """.trimIndent()

                    is ServerResponseException -> """
                        💥 Server error (5xx).
                        Status: ${e.response.status}
                        URL: ${e.response.request.url}
                        Message: ${e.message}
                    """.trimIndent()

                    is RedirectResponseException -> """
                        🔁 Redirect issue.
                        Status: ${e.response.status}
                        URL: ${e.response.request.url}
                    """.trimIndent()

                    else -> """
                        ❓ Unknown error of type: ${e::class.simpleName}
                        Message: ${e.message}
                        Cause: ${e.cause}
                    """.trimIndent()
                }

                uiState = uiState.copy(
                    isLoading = false,
                    signupSuccess = false,
                    error = errorMessage
                )
            }
        }
    }

    fun resetState() {
        uiState = uiState.copy(signupSuccess = false, error = null)
    }
}
