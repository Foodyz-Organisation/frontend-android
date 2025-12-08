package com.example.damprojectfinal.core.dto.posts

data class PostOwnerDetails(
    val _id: String,
    val username: String,
    val fullName: String?,
    val email: String?,
    val followerCount: Int,
    val followingCount: Int,
    val profilePictureUrl: String? = null
)
