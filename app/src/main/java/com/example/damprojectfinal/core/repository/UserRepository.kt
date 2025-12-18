package com.example.damprojectfinal.core.repository

import com.example.damprojectfinal.core.api.UserApiService
import com.example.damprojectfinal.core.dto.user.UpdateUserRequest
import com.example.damprojectfinal.core.dto.user.UserResponse
import java.io.File // Required for the file parameter

class UserRepository(private val apiService: UserApiService) {

    /**
     * Update the profile of the logged-in user
     */
    suspend fun updateProfile(updateRequest: UpdateUserRequest, token: String, userId: String? = null): UserResponse {
        return apiService.updateProfile(updateRequest, token, userId)
    }

    /**
     * Toggle / deactivate the account of the logged-in user
     */
    suspend fun toggleActive(token: String): UserResponse {
        return apiService.toggleActive(token)
    }

    suspend fun getUserById(id: String, token: String): UserResponse {
        return apiService.getUserById(id, token)
    }

    // ⭐ NEW FUNCTION: Upload Profile Image ⭐
    /**
     * Uploads a new profile image for a specific user.
     * Delegates the multipart form submission to the UserApiService.
     */
    suspend fun uploadProfileImage(
        id: String,
        file: File,
        token: String
    ): UserResponse {
        return apiService.uploadProfileImage(id, file, token)
    }

}