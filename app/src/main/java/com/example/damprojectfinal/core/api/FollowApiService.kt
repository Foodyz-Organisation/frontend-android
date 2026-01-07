package com.example.damprojectfinal.core.api

import retrofit2.http.*

interface FollowApiService {
    
    data class FollowResponse(
        val _id: String,
        val followerId: String,
        val followerModel: String,
        val followingId: String,
        val followingModel: String,
        val createdAt: String,
        val updatedAt: String
    )
    
    // Follow a professional/user account
    @POST("follows/{followingId}")
    suspend fun follow(
        @Path("followingId") followingId: String,
        @Header("x-following-type") followingType: String // "UserAccount" or "ProfessionalAccount"
    ): FollowResponse
    
    // Unfollow a professional/user account
    @DELETE("follows/{followingId}")
    suspend fun unfollow(
        @Path("followingId") followingId: String,
        @Header("x-following-type") followingType: String // "UserAccount" or "ProfessionalAccount"
    )
    
    // Check if following
    @GET("follows/is-following/{targetId}")
    suspend fun isFollowing(
        @Path("targetId") targetId: String,
        @Header("x-target-type") targetType: String // "UserAccount" or "ProfessionalAccount"
    ): Boolean
    
    // Get following list (detailed)
    @GET("follows/following-details")
    suspend fun getFollowingDetails(): List<FollowDetailResponse>
    
    // Get followers list (detailed)
    @GET("follows/followers-details/{targetId}")
    suspend fun getFollowersDetails(
        @Path("targetId") targetId: String,
        @Header("x-target-type") targetType: String // "UserAccount" or "ProfessionalAccount"
    ): List<FollowDetailResponse>
    
    data class FollowDetailResponse(
        val id: String,
        val type: String,
        val fullName: String,
        val profilePictureUrl: String?,
        val followerCount: Int,
        val followingCount: Int,
        val email: String?
    )
}

