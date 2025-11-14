package com.example.damprojectfinal.feature_auth.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.damprojectfinal.feature_auth.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ForgotPasswordUiState>(ForgotPasswordUiState.Idle)
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState

    fun sendOtp(email: String) {
        if (email.isBlank()) {
            _uiState.value = ForgotPasswordUiState.Error("Email cannot be empty")
            return
        }

        viewModelScope.launch {
            _uiState.value = ForgotPasswordUiState.Loading

            repository.sendOtp(email)
                .onSuccess { response ->
                    if (response.success) {
                        // ✅ IMPORTANT : Passer l'email pour la navigation
                        _uiState.value = ForgotPasswordUiState.OtpSent(email)
                    } else {
                        _uiState.value = ForgotPasswordUiState.Error("Failed to send OTP")
                    }
                }
                .onFailure { exception ->
                    _uiState.value = ForgotPasswordUiState.Error(
                        exception.message ?: "Network error"
                    )
                }
        }
    }

    fun resetState() {
        _uiState.value = ForgotPasswordUiState.Idle
    }
}

sealed class ForgotPasswordUiState {
    object Idle : ForgotPasswordUiState()
    object Loading : ForgotPasswordUiState()
    data class OtpSent(val email: String) : ForgotPasswordUiState()
    data class Error(val message: String) : ForgotPasswordUiState()
}