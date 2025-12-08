package com.example.damprojectfinal.core.api.normalUser

import com.example.damprojectfinal.core.dto.normalUser.UserProfile
import retrofit2.http.*
import retrofit2.http.DELETE
import retrofit2.http.PATCH

interface UserApiService {
    @GET("users/{id}/profile")
    suspend fun getUserProfile(@Path("id") userId: String): UserProfile

    // Follow/Unfollow endpoints
    @PATCH("users/{userId}/follow")
    suspend fun followUser(@Path("userId") userId: String): UserProfile

    @DELETE("users/{userId}/follow")
    suspend fun unfollowUser(@Path("userId") userId: String): UserProfile
}
