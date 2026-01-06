package com.example.damprojectfinal.core.repository

import com.example.damprojectfinal.core.api.UserApiService
import com.example.damprojectfinal.core.dto.user.UpdateUserRequest
import com.example.damprojectfinal.core.dto.user.UserResponse
import com.example.damprojectfinal.core.dto.user.ProfilePictureUploadResponse
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
     * 
     * @deprecated This function uses the old endpoint that may trigger AI validation.
     * Use uploadProfilePicture() instead for profile pictures (bypasses AI validation).
     */
    suspend fun uploadProfileImage(
        id: String,
        file: File,
        token: String
    ): UserResponse {
        return apiService.uploadProfileImage(id, file, token)
    }

    /**
     * ⭐ NEW FUNCTION: Upload Profile Picture via dedicated endpoint ⭐
     * 
     * Uploads a profile picture using the new dedicated endpoint that bypasses all AI validation.
     * This is the recommended method for uploading profile pictures.
     * 
     * @param id User ID
     * @param file Image file to upload (max 5MB, image types only)
     * @param token Authentication token
     * @return ProfilePictureUploadResponse containing the uploaded profile picture URL and updated user data
     */
    suspend fun uploadProfilePicture(
        id: String,
        file: File,
        token: String
    ): ProfilePictureUploadResponse {
        return apiService.uploadProfilePicture(id, file, token)
    }

}