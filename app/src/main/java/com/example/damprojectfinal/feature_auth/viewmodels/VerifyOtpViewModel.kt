package com.example.damprojectfinal.feature_auth.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.damprojectfinal.core.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VerifyOtpViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<VerifyOtpUiState>(VerifyOtpUiState.Idle)
    val uiState: StateFlow<VerifyOtpUiState> = _uiState

    fun verifyOtp(email: String, otp: String) {
        if (otp.length != 6) {
            _uiState.value = VerifyOtpUiState.Error("OTP must be 6 digits")
            return
        }

        viewModelScope.launch {
            _uiState.value = VerifyOtpUiState.Loading

            repository.verifyOtp(email, otp)
                .onSuccess { response ->
                    if (response.success && !response.resetToken.isNullOrBlank()) {
                        // ✅ IMPORTANT : Retourner email ET resetToken
                        _uiState.value = VerifyOtpUiState.Verified(
                            email = email,
                            resetToken = response.resetToken
                        )
                    } else {
                        _uiState.value = VerifyOtpUiState.Error("Invalid OTP code")
                    }
                }
                .onFailure { exception ->
                    _uiState.value = VerifyOtpUiState.Error(
                        exception.message ?: "Network error"
                    )
                }
        }
    }

    fun resetState() {
        _uiState.value = VerifyOtpUiState.Idle
    }
}

sealed class VerifyOtpUiState {
    object Idle : VerifyOtpUiState()
    object Loading : VerifyOtpUiState()
    data class Verified(val email: String, val resetToken: String) : VerifyOtpUiState()
    data class Error(val message: String) : VerifyOtpUiState()
}