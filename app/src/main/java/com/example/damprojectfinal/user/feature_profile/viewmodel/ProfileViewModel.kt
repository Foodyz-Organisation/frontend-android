package com.example.damprojectfinal.user.feature_profile.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.damprojectfinal.core.api.TokenManager
import com.example.damprojectfinal.core.dto.user.UpdateUserRequest
import com.example.damprojectfinal.core.dto.user.UserResponse
import com.example.damprojectfinal.core.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class ProfileViewModel(
    private val userRepo: UserRepository,
    private val context: Context
) : ViewModel() {

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

    // --- Helper: get token from TokenManager ---
    private fun getToken(): String? = TokenManager(context).getAccessTokenBlocking()

    // --- 1. Fetch Profile ---
    fun fetchUserProfile() {
        viewModelScope.launch {
            isLoading.value = true
            resetStatus()
            val token = getToken()
            val currentUserId = TokenManager(context).getUserIdBlocking()
            if (token == null || currentUserId == null) {
                errorMessage.value = "Token or User ID missing"
                isLoading.value = false
                return@launch
            }

            try {
                val user = userRepo.getUserById(currentUserId, token)
                userState.value = user
            } catch (e: Exception) {
                errorMessage.value = e.message
            } finally {
                isLoading.value = false
            }
        }
    }

    // --- 2. Update Details ---
    fun updateProfile(updateRequest: UpdateUserRequest, token: String) {
        viewModelScope.launch {
            isLoading.value = true
            resetStatus()
            val token = getToken()
            val currentUserId = userState.value?.id
            if (token == null || currentUserId == null) {
                errorMessage.value = "Token or User ID missing"
                isLoading.value = false
                return@launch
            }

            try {
                // Use the actual user ID instead of "me" to avoid backend ObjectId casting errors
                userRepo.updateProfile(updateRequest, token, userId = currentUserId)
                val updatedUser = userRepo.getUserById(currentUserId, token)
                userState.value = updatedUser
                _updateSuccess.value = true
            } catch (e: Exception) {
                errorMessage.value = e.message
            } finally {
                isLoading.value = false
            }
        }
    }

    // --- 3. Toggle Active Status ---
    fun toggleActive() {
        viewModelScope.launch {
            isLoading.value = true
            resetStatus()
            val token = getToken()
            val currentUserId = userState.value?.id
            if (token == null || currentUserId == null) {
                errorMessage.value = "Token or User ID missing"
                isLoading.value = false
                return@launch
            }

            try {
                userRepo.toggleActive(token)
                val updatedUser = userRepo.getUserById(currentUserId, token)
                userState.value = updatedUser
                _updateSuccess.value = true
            } catch (e: Exception) {
                errorMessage.value = e.message
            } finally {
                isLoading.value = false
            }
        }
    }

    // --- 4. Upload Profile Image (Legacy - uses old endpoint) ---
    /**
     * @deprecated This function uses the old endpoint that may trigger AI validation.
     * Use uploadProfilePicture() instead for profile pictures (bypasses AI validation).
     */
    fun uploadProfileImage(file: File) {
        viewModelScope.launch {
            isLoading.value = true
            resetStatus()
            val token = getToken()
            val currentUserId = userState.value?.id
            if (token == null || currentUserId == null) {
                errorMessage.value = "Token or User ID missing"
                isLoading.value = false
                return@launch
            }

            try {
                userRepo.uploadProfileImage(currentUserId, file, token)
                val updatedUser = userRepo.getUserById(currentUserId, token)
                userState.value = updatedUser
                _updateSuccess.value = true
            } catch (e: Exception) {
                errorMessage.value = "Failed to upload image: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }

    // --- 5. Upload Profile Picture (NEW - uses dedicated endpoint, bypasses AI validation) ---
    /**
     * Uploads a profile picture using the new dedicated endpoint that bypasses all AI validation.
     * This is the recommended method for uploading profile pictures.
     * 
     * @param file Image file to upload (max 5MB, image types only: jpg, jpeg, png, gif, webp)
     */
    fun uploadProfilePicture(file: File) {
        viewModelScope.launch {
            isLoading.value = true
            resetStatus()
            val token = getToken()
            val currentUserId = userState.value?.id
            if (token == null || currentUserId == null) {
                errorMessage.value = "Token or User ID missing"
                isLoading.value = false
                return@launch
            }

            try {
                val response = userRepo.uploadProfilePicture(currentUserId, file, token)
                // Update user state with the returned user data
                userState.value = response.user
                _updateSuccess.value = true
                android.util.Log.d("ProfileViewModel", "Profile picture uploaded successfully: ${response.profilePictureUrl}")
            } catch (e: Exception) {
                errorMessage.value = "Failed to upload profile picture: ${e.message}"
                android.util.Log.e("ProfileViewModel", "Error uploading profile picture: ${e.message}", e)
            } finally {
                isLoading.value = false
            }
        }
    }

    // --- Factory for ViewModel with context dependency ---
    companion object {
        fun Factory(userRepository: UserRepository, context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                        return ProfileViewModel(userRepository, context) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
    }
}
