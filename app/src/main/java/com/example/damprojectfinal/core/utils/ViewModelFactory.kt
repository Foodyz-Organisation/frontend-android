package com.example.damprojectfinal.core.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.damprojectfinal.core.api.AuthApiService
import com.example.damprojectfinal.feature_auth.repository.AuthRepository
import com.example.damprojectfinal.feature_auth.viewmodels.ForgotPasswordViewModel
import com.example.damprojectfinal.feature_auth.viewmodels.LoginViewModel
import com.example.damprojectfinal.feature_auth.viewmodels.ResetPasswordViewModel
import com.example.damprojectfinal.feature_auth.viewmodels.VerifyOtpViewModel

// ========== LoginViewModelFactory ==========
class LoginViewModelFactory(
    private val authApiService: AuthApiService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(authApiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

// ========== ForgotPasswordViewModelFactory ==========
class ForgotPasswordViewModelFactory(
    private val repository: AuthRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ForgotPasswordViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ForgotPasswordViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

// ========== VerifyOtpViewModelFactory ==========
class VerifyOtpViewModelFactory(
    private val repository: AuthRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VerifyOtpViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VerifyOtpViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

// ========== ResetPasswordViewModelFactory ==========
class ResetPasswordViewModelFactory(
    private val repository: AuthRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ResetPasswordViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ResetPasswordViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

// ========== Generic ViewModelFactory (pour usage simple) ==========
class ViewModelFactory<T : ViewModel>(
    private val creator: () -> T
) : ViewModelProvider.Factory {
    override fun <VM : ViewModel> create(modelClass: Class<VM>): VM {
        @Suppress("UNCHECKED_CAST")
        return creator() as VM
    }
}