package com.example.damprojectfinal.user.feature_profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.damprojectfinal.core.dto.user.UpdateUserRequest
import com.example.damprojectfinal.core.dto.user.UserResponse
import com.example.damprojectfinal.core.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class ProfileViewModel(private val userRepo: UserRepository) : ViewModel() {

    // --- State Holders ---
    val userState = MutableStateFlow<UserResponse?>(null)
    val isLoading = MutableStateFlow(false)
    val errorMessage = MutableStateFlow<String?>(null)

    private val _updateSuccess = MutableStateFlow(false)
    val updateSuccess: StateFlow<Boolean> = _updateSuccess.asStateFlow()

    private fun resetStatus() {
        errorMessage.value = null
        _updateSuccess.value = false
    }

    // --- 1. Fetch Profile ---
    fun fetchUserProfile(userId: String, token: String) {
        viewModelScope.launch {
            isLoading.value = true
            resetStatus()
            try {
                val user = userRepo.getUserById(userId, token)
                userState.value = user
            } catch (e: Exception) {
                errorMessage.value = e.message
            } finally {
                isLoading.value = false
            }
        }
    }

    // --- 2. Update Details (FIXED: Using 'id' property) ---
    fun updateProfile(updateRequest: UpdateUserRequest, token: String) {
        viewModelScope.launch {
            isLoading.value = true
            resetStatus()

            // ⭐ FIX: Changed '_id' to 'id' to match Kotlin property name.
            val currentUserId = userState.value?.id

            try {
                // 1. Execute the update API call (Response discarded to avoid serialization error)
                userRepo.updateProfile(updateRequest, token)

                // 2. Re-fetch the entire profile to get complete, updated data
                if (currentUserId != null) {
                    val updatedUser = userRepo.getUserById(currentUserId, token)
                    userState.value = updatedUser
                    _updateSuccess.value = true
                } else {
                    errorMessage.value = "User ID not available for profile refresh."
                }
            } catch (e: Exception) {
                errorMessage.value = e.message
            } finally {
                isLoading.value = false
            }
        }
    }

    // --- 3. Toggle Active Status (FIXED: Using 'id' property) ---
    fun toggleActive(token: String) {
        viewModelScope.launch {
            isLoading.value = true
            resetStatus()

            // ⭐ FIX: Changed '_id' to 'id' to match Kotlin property name.
            val currentUserId = userState.value?.id

            try {
                // 1. Execute the toggle API call (Response discarded)
                userRepo.toggleActive(token)

                // 2. Re-fetch the profile to get the updated 'isActive' status
                if (currentUserId != null) {
                    val updatedUser = userRepo.getUserById(currentUserId, token)
                    userState.value = updatedUser
                    _updateSuccess.value = true
                } else {
                    errorMessage.value = "User ID not available for profile refresh."
                }
            } catch (e: Exception) {
                errorMessage.value = e.message
            } finally {
                isLoading.value = false
            }
        }
    }

    // --- 4. Upload Profile Image (Kept as is, as userId is passed as argument) ---
    fun uploadProfileImage(userId: String, file: File, token: String) {
        viewModelScope.launch {
            isLoading.value = true
            resetStatus()
            try {
                // 1. Execute the upload API call (Response discarded)
                userRepo.uploadProfileImage(userId, file, token)

                // 2. Re-fetch the profile to get the new profilePictureUrl
                val updatedUser = userRepo.getUserById(userId, token)
                userState.value = updatedUser
                _updateSuccess.value = true
            } catch (e: Exception) {
                errorMessage.value = "Failed to upload image: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }

    /**
     * Factory class required to instantiate ProfileViewModel with a custom UserRepository dependency
     * when using the `viewModel()` Composable function.
     */
    companion object {
        fun Factory(userRepository: UserRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                        return ProfileViewModel(userRepository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
    }
}