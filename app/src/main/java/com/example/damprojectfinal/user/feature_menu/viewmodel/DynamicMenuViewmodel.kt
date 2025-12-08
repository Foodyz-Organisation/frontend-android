package com.example.damprojectfinal.user.feature_menu.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.damprojectfinal.core.dto.menu.MenuItemResponseDto
import com.example.damprojectfinal.core.repository.MenuItemRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DynamicMenuViewModel(
    private val repository: MenuItemRepository,
    private val professionalId: String,
    private val authToken: String
) : ViewModel() {

    private val _menuItems = MutableStateFlow<List<MenuItemResponseDto>>(emptyList())
    val menuItems: StateFlow<List<MenuItemResponseDto>> = _menuItems

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        fetchMenu()
    }

    private fun fetchMenu() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                // Call repository with authToken
                val result = repository.getGroupedMenu(professionalId, authToken)

                result.fold(
                    onSuccess = { groupedMap ->
                        // Flatten all categories into a single list
                        val allItems = groupedMap.values.flatten()
                        _menuItems.value = allItems
                    },
                    onFailure = { throwable ->
                        _errorMessage.value = throwable.localizedMessage ?: "Failed to load menu"
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Unexpected error"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

// ⭐️ VIEW MODEL FACTORY ⭐️
class DynamicMenuViewModelFactory(
    private val repository: MenuItemRepository,
    private val professionalId: String,
    private val authToken: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DynamicMenuViewModel::class.java)) {
            return DynamicMenuViewModel(repository, professionalId, authToken) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}