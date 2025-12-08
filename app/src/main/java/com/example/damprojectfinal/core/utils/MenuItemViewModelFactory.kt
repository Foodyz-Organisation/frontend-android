/*package com.example.damprojectfinal.core.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.damprojectfinal.core.repository.MenuItemRepository
import com.example.damprojectfinal.professional.feature_menu.viewmodel.MenuItemViewModel

class MenuItemViewModel(private val repository: MenuItemRepository) : ViewModel() {
    // your existing code

    companion object {
        fun provideFactory(repository: MenuItemRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(MenuItemViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return MenuItemViewModel(repository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
    }
}
*/