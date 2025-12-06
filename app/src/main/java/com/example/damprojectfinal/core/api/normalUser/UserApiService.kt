package com.example.damprojectfinal.core.api.normalUser

// src/main/java/com/example/damprojectfinal/core/api.user/UserApiService.kt

import com.example.damprojectfinal.core.dto.normalUser.UserProfile
import retrofit2.http.GET
import retrofit2.http.Path

interface UserApiService {

    @GET("users/{id}/profile")
    suspend fun getUserProfile(@Path("id") userId: String): UserProfile

    // Add other user-related endpoints here as needed (e.g., update profile, follow/unfollow)
}
