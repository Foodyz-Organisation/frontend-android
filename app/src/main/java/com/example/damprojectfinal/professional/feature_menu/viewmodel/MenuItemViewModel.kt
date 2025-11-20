package com.example.damprojectfinal.professional.feature_menu.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.damprojectfinal.core.dto.menu.CreateMenuItemDto
import com.example.damprojectfinal.core.dto.menu.MenuItemResponseDto
import com.example.damprojectfinal.core.`object`.FileWithMime
import com.example.damprojectfinal.core.repository.MenuItemRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// UI state sealed class
sealed class MenuItemUiState {
    object Idle : MenuItemUiState()
    object Loading : MenuItemUiState()
    data class Success(val menuItem: MenuItemResponseDto) : MenuItemUiState()
    data class Error(val message: String) : MenuItemUiState()
}

class MenuViewModel(
    private val repository: MenuItemRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MenuItemUiState>(MenuItemUiState.Idle)
    val uiState: StateFlow<MenuItemUiState> = _uiState

    /**
     * Create a menu item with DTO + image file.
     */
    fun createMenuItem(
        payload: CreateMenuItemDto,
        imageFile: FileWithMime,
        authToken: String
    ) {
        _uiState.value = MenuItemUiState.Loading

        viewModelScope.launch {
            val result = repository.createMenuItem(payload, imageFile, authToken)

            result
                .onSuccess { menuItem ->
                    _uiState.value = MenuItemUiState.Success(menuItem)
                }
                .onFailure { throwable ->
                    _uiState.value = MenuItemUiState.Error(
                        throwable.message ?: "Unknown error during menu item creation."
                    )
                }
        }
    }

    /**
     * Reset UI state to Idle.
     */
    fun resetUiState() {
        _uiState.value = MenuItemUiState.Idle
    }

    // ------------------ ViewModel Factory ------------------
    class Factory(private val repository: MenuItemRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MenuViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MenuViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
