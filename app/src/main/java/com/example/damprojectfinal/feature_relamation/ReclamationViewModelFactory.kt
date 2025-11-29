package com.example.damprojectfinal.feature_relamation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.damprojectfinal.core.api.TokenManager
import com.example.damprojectfinal.core.api.UserApiService
import com.example.foodyz_dam.ui.theme.screens.reclamation.ReclamationViewModel
import com.example.foodyz_dam.ui.theme.screens.reclamation.ReclamationRepository

class ReclamationViewModelFactory(
    private val userApiService: UserApiService,
    private val tokenManager: TokenManager,
    private val context: Context // ✅ AJOUT
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReclamationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            val repository = ReclamationRepository(tokenManager, context)
            return ReclamationViewModel(userApiService, tokenManager, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
