// src/main/java/com/example/damprojectfinal/feature_profile.ui/UserViewModel.kt
package com.example.damprojectfinal.feature_profile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.damprojectfinal.core.api.posts.RetrofitClient
import com.example.damprojectfinal.core.dto.posts.PostResponse
import com.example.damprojectfinal.core.dto.normalUser.UserProfile
import com.example.damprojectfinal.core.dto.normalUser.ProfileUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.app.Application // <-- NEW IMPORT: Needed to get applicationContext
import androidx.lifecycle.AndroidViewModel // <-- NEW: Change ViewModel to AndroidViewModel
import com.example.damprojectfinal.core.api.TokenManager // <-- NEW IMPORT

// Change ViewModel to AndroidViewModel to access Application context
class UserViewModel(application: Application) : AndroidViewModel(application) { // <-- MODIFIED CONSTRUCTOR

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val tokenManager = TokenManager(application.applicationContext)
    // --- THIS VARIABLE SHOULD NOW HOLD THE REAL ID ---
    private val loggedInUserId: String? = tokenManager.getUserId()
    // --- ENSURE THE OLD `currentLoggedInUserId` IS REMOVED OR COMMENTED OUT ---
    // private val currentLoggedInUserId: String = "YOUR_HARDCODED_USER_ID" // <-- THIS LINE SHOULD NOT EXIST OR BE COMMENTED

    init {
        // --- THIS CALL MUST USE THE REAL ID ---
        if (loggedInUserId != null) {
            fetchUserProfileAndPosts(loggedInUserId)
        } else {
            _uiState.update { it.copy(errorMessage = "User not logged in.") }
        }
    }

    fun fetchUserProfileAndPosts(userId: String) { // <-- 'userId' parameter here is the one used for API calls
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingProfile = true, isLoadingPosts = true, errorMessage = null) }

            try {
                // Fetch User Profile with the CORRECT userId from the parameter
                val userProfile = RetrofitClient.userApiService.getUserProfile(userId)
                _uiState.update { it.copy(userProfile = userProfile, isLoadingProfile = false) }

                // Fetch User Posts with the CORRECT userId from the parameter
                val userPosts = RetrofitClient.postsApiService.getPostsByOwnerId(userId)
                _uiState.update { it.copy(userPosts = userPosts, isLoadingPosts = false) }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = e.message ?: "An unknown error occurred",
                        isLoadingProfile = false,
                        isLoadingPosts = false
                    )
                }
                e.printStackTrace()
            }
        }
    }

    fun onTabSelected(index: Int) {
        _uiState.update { it.copy(selectedTabIndex = index) }
    }

    fun refreshProfile() {
        if (loggedInUserId != null) {
            fetchUserProfileAndPosts(loggedInUserId) // <-- THIS CALL MUST USE THE REAL ID
        } else {
            _uiState.update { it.copy(errorMessage = "User not logged in.") }
        }
    }
}
