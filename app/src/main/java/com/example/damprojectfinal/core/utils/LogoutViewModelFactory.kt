package com.example.damprojectfinal.core.utils


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.damprojectfinal.core.api.AuthApiService
import com.example.damprojectfinal.core.api.TokenManager
import com.example.damprojectfinal.feature_auth.viewmodels.LogoutViewModel

class LogoutViewModelFactory(
    private val authApiService: AuthApiService,
    private val tokenManager: TokenManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LogoutViewModel::class.java)) {
            return LogoutViewModel(authApiService, tokenManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
