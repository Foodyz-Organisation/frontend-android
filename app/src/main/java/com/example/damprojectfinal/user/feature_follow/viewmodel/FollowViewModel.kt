package com.example.damprojectfinal.user.feature_follow.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.damprojectfinal.core.api.FollowApiService
import com.example.damprojectfinal.core.api.TokenManager
import com.example.damprojectfinal.core.retro.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FollowViewModel(
    private val followApiService: FollowApiService = RetrofitClient.followApiService,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val TAG = "FollowViewModel"

    // Map to track following status for different users/professionals
    private val _followingStatus = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val followingStatus: StateFlow<Map<String, Boolean>> = _followingStatus.asStateFlow()

    // Map to track loading states
    private val _loadingStates = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val loadingStates: StateFlow<Map<String, Boolean>> = _loadingStates.asStateFlow()

    // Error message
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /**
     * Check if the current user is following a target account
     * @param targetId The ID of the account to check
     * @param targetType "UserAccount" or "ProfessionalAccount"
     */
    fun checkFollowingStatus(targetId: String, targetType: String) {
        viewModelScope.launch {
            try {
                _loadingStates.value = _loadingStates.value + (targetId to true)
                val isFollowing = followApiService.isFollowing(targetId, targetType)
                _followingStatus.value = _followingStatus.value + (targetId to isFollowing)
                Log.d(TAG, "Following status for $targetId: $isFollowing")
            } catch (e: Exception) {
                Log.e(TAG, "Error checking follow status: ${e.message}", e)
                _errorMessage.value = "Failed to check follow status: ${e.localizedMessage ?: e.message}"
                // Default to false on error
                _followingStatus.value = _followingStatus.value + (targetId to false)
            } finally {
                _loadingStates.value = _loadingStates.value + (targetId to false)
            }
        }
    }

    /**
     * Follow a user or professional account
     * @param followingId The ID of the account to follow
     * @param followingType "UserAccount" or "ProfessionalAccount"
     */
    fun follow(followingId: String, followingType: String) {
        viewModelScope.launch {
            try {
                _loadingStates.value = _loadingStates.value + (followingId to true)
                val response = followApiService.follow(followingId, followingType)
                _followingStatus.value = _followingStatus.value + (followingId to true)
                Log.d(TAG, "Successfully followed $followingId")
                _errorMessage.value = null
            } catch (e: Exception) {
                Log.e(TAG, "Error following: ${e.message}", e)
                _errorMessage.value = "Failed to follow: ${e.localizedMessage ?: e.message}"
                // On error, don't update status - keep current state
            } finally {
                _loadingStates.value = _loadingStates.value + (followingId to false)
            }
        }
    }

    /**
     * Unfollow a user or professional account
     * @param followingId The ID of the account to unfollow
     * @param followingType "UserAccount" or "ProfessionalAccount"
     */
    fun unfollow(followingId: String, followingType: String) {
        viewModelScope.launch {
            try {
                _loadingStates.value = _loadingStates.value + (followingId to true)
                followApiService.unfollow(followingId, followingType)
                _followingStatus.value = _followingStatus.value + (followingId to false)
                Log.d(TAG, "Successfully unfollowed $followingId")
                _errorMessage.value = null
            } catch (e: Exception) {
                Log.e(TAG, "Error unfollowing: ${e.message}", e)
                _errorMessage.value = "Failed to unfollow: ${e.localizedMessage ?: e.message}"
                // On error, don't update status - keep current state
            } finally {
                _loadingStates.value = _loadingStates.value + (followingId to false)
            }
        }
    }

    /**
     * Toggle follow/unfollow status
     * @param targetId The ID of the account
     * @param targetType "UserAccount" or "ProfessionalAccount"
     */
    fun toggleFollow(targetId: String, targetType: String) {
        val currentStatus = _followingStatus.value[targetId] ?: false
        if (currentStatus) {
            unfollow(targetId, targetType)
        } else {
            follow(targetId, targetType)
        }
    }

    /**
     * Get following status for a specific target
     */
    fun isFollowing(targetId: String): Boolean {
        return _followingStatus.value[targetId] ?: false
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }
}

