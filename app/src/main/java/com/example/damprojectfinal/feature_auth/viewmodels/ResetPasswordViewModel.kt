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
class ResetPasswordViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ResetPasswordUiState>(ResetPasswordUiState.Idle)
    val uiState: StateFlow<ResetPasswordUiState> = _uiState

    fun resetPassword(email: String, resetToken: String, newPassword: String) {
        if (newPassword.length < 8) {
            _uiState.value = ResetPasswordUiState.Error("Password must be at least 8 characters")
            return
        }

        viewModelScope.launch {
            _uiState.value = ResetPasswordUiState.Loading

            repository.resetPasswordWithOtp(email, resetToken, newPassword)
                .onSuccess { response ->
                    if (response.success) {
                        _uiState.value = ResetPasswordUiState.Success(response.message)
                    } else {
                        _uiState.value = ResetPasswordUiState.Error("Failed to reset password")
                    }
                }
                .onFailure { exception ->
                    _uiState.value = ResetPasswordUiState.Error(
                        exception.message ?: "Network error"
                    )
                }
        }
    }

    fun resetState() {
        _uiState.value = ResetPasswordUiState.Idle
    }
}

sealed class ResetPasswordUiState {
    object Idle : ResetPasswordUiState()
    object Loading : ResetPasswordUiState()
    data class Success(val message: String) : ResetPasswordUiState()
    data class Error(val message: String) : ResetPasswordUiState()
}