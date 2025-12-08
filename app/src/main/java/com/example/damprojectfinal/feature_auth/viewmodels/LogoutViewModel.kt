package com.example.damprojectfinal.feature_auth.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.damprojectfinal.core.api.AuthApiService
import com.example.damprojectfinal.core.api.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LogoutViewModel(
    private val authApiService: AuthApiService,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _logoutSuccess = MutableStateFlow(false)
    val logoutSuccess: StateFlow<Boolean> = _logoutSuccess

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // ✅ CORRECTED LOGIC in LogoutViewModel
    fun logout() {
        viewModelScope.launch {
            try {
                // 1️⃣ (Best Effort) Tell the backend we are logging out
                authApiService.logout()

            } catch (e: Exception) {
                // 2️⃣ (Optional) Log the error or show a non-blocking message
                _error.value = "Couldn't contact server, logging out locally."
                // We don't care if this fails, we STILL log out.

            } finally {
                // 3️⃣ (Mandatory) This block ALWAYS runs
                tokenManager.clearTokens()
                _logoutSuccess.value = true // This triggers the navigation
            }
        }
    }
}
