package com.example.damprojectfinal.core.dto.normalUser

import com.google.gson.annotations.SerializedName

data class UserProfile(
    val _id: String,
    val username: String,
    val fullName: String,
    val bio: String, // <-- NOT IN JSON
    val profilePictureUrl: String?, // <-- NOT IN JSON
    val followerCount: Int,
    val followingCount: Int,
    val postCount: Int, // <-- NOT IN JSON
    val phone: String? = null, // Not in JSON, but nullable, so okay
    val address: String? = null, // Not in JSON, but nullable, so okay
    val email: String? = null, // Not in JSON, but nullable, so okay
    val isActive: Boolean = false // Not in JSON, but nullable, so okay
)

