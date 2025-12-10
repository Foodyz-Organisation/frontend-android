package com.example.damprojectfinal.professional.feature_menu.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.damprojectfinal.core.dto.menu.CreateMenuItemDto
import com.example.damprojectfinal.core.dto.menu.MenuItemResponseDto
import com.example.damprojectfinal.core.dto.menu.GroupedMenuResponse
import com.example.damprojectfinal.core.dto.menu.UpdateMenuItemDto
import com.example.damprojectfinal.core.`object`.FileWithMime
import com.example.damprojectfinal.core.repository.MenuItemRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

// State for List
sealed class MenuListUiState {
    object Idle : MenuListUiState()
    object Loading : MenuListUiState()
    data class Success(val groupedMenu: GroupedMenuResponse) : MenuListUiState()
    data class Error(val message: String) : MenuListUiState()
}

// ⭐️ NEW: State for Single Item Details
sealed class ItemDetailsUiState {
    object Idle : ItemDetailsUiState()
    object Loading : ItemDetailsUiState()
    data class Success(val item: MenuItemResponseDto) : ItemDetailsUiState()
    data class Error(val message: String) : ItemDetailsUiState()
}

// State for Actions (Create/Update/Delete)
sealed class MenuItemUiState {
    object Idle : MenuItemUiState()
    object Loading : MenuItemUiState()
    data class Success(val menuItem: MenuItemResponseDto) : MenuItemUiState()
    data class Error(val message: String) : MenuItemUiState()
}

class MenuViewModel(
    private val repository: MenuItemRepository
) : ViewModel() {

    // 1. List State
    private val _menuListUiState = MutableStateFlow<MenuListUiState>(MenuListUiState.Idle)
    val menuListUiState: StateFlow<MenuListUiState> = _menuListUiState

    // 2. ⭐️ NEW: Details State
    private val _itemDetailsUiState = MutableStateFlow<ItemDetailsUiState>(ItemDetailsUiState.Idle)
    val itemDetailsUiState: StateFlow<ItemDetailsUiState> = _itemDetailsUiState

    // 3. Action State (Create/Update/Delete)
    private val _uiState = MutableStateFlow<MenuItemUiState>(MenuItemUiState.Idle)
    val uiState: StateFlow<MenuItemUiState> = _uiState

    // --- FETCH LIST ---
    fun fetchGroupedMenu(professionalId: String, authToken: String) {
        _menuListUiState.value = MenuListUiState.Loading
        viewModelScope.launch {
            repository.getGroupedMenu(professionalId, authToken)
                .onSuccess { _menuListUiState.value = MenuListUiState.Success(it) }
                .onFailure { _menuListUiState.value = MenuListUiState.Error(it.message ?: "Error") }
        }
    }

    // --- ⭐️ NEW: FETCH SINGLE ITEM ---
    fun fetchMenuItemDetails(itemId: String, authToken: String) {
        _itemDetailsUiState.value = ItemDetailsUiState.Loading
        viewModelScope.launch {
            repository.getMenuItemDetails(itemId, authToken)
                .onSuccess { _itemDetailsUiState.value = ItemDetailsUiState.Success(it) }
                .onFailure { _itemDetailsUiState.value = ItemDetailsUiState.Error(it.message ?: "Error") }
        }
    }

    // --- UPDATE ---
    fun updateMenuItem(id: String, professionalId: String, payload: UpdateMenuItemDto, authToken: String) {
        _uiState.value = MenuItemUiState.Loading
        viewModelScope.launch {
            repository.updateMenuItem(id, payload, authToken)
                .onSuccess {
                    _uiState.value = MenuItemUiState.Success(it)
                    fetchGroupedMenu(professionalId, authToken) // Refresh list
                }
                .onFailure { _uiState.value = MenuItemUiState.Error(it.message ?: "Update Error") }
        }
    }

    // --- UPDATE WITH IMAGE ---
    fun updateMenuItemWithImage(
        id: String,
        professionalId: String,
        payload: UpdateMenuItemDto,
        imageFile: FileWithMime,
        authToken: String
    ) {
        _uiState.value = MenuItemUiState.Loading
        viewModelScope.launch {
            repository.updateMenuItemWithImage(id, payload, imageFile, authToken)
                .onSuccess {
                    _uiState.value = MenuItemUiState.Success(it)
                    fetchGroupedMenu(professionalId, authToken) // Refresh list
                }
                .onFailure { _uiState.value = MenuItemUiState.Error(it.message ?: "Update with Image Error") }
        }
    }

    // --- CREATE ---
    fun createMenuItem(payload: CreateMenuItemDto, imageFile: FileWithMime, authToken: String) {
        _uiState.value = MenuItemUiState.Loading
        viewModelScope.launch {
            repository.createMenuItem(payload, imageFile, authToken)
                .onSuccess {
                    _uiState.value = MenuItemUiState.Success(it)
                    fetchGroupedMenu(payload.professionalId, authToken)
                }
                .onFailure { _uiState.value = MenuItemUiState.Error(it.message ?: "Create Error") }
        }
    }

    // --- DELETE ---
    fun deleteMenuItem(id: String, professionalId: String, authToken: String) {
        _uiState.value = MenuItemUiState.Loading
        viewModelScope.launch {
            repository.deleteMenuItem(id, authToken)
                .onSuccess {
                    _uiState.value = MenuItemUiState.Success(it)
                    fetchGroupedMenu(professionalId, authToken)
                }
                .onFailure { _uiState.value = MenuItemUiState.Error(it.message ?: "Delete Error") }
        }
    }

    fun resetUiState() { _uiState.value = MenuItemUiState.Idle }

    class Factory(private val repository: MenuItemRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MenuViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST") return MenuViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}