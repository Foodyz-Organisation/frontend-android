package com.example.damprojectfinal.feature_relamation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.damprojectfinal.core.repository.ReclamationRepository

class ReclamationsRestaurantViewModelFactory(
    private val repository: ReclamationRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReclamationsRestaurantViewModel::class.java)) {
            return ReclamationsRestaurantViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}