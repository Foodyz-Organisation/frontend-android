package com.example.damprojectfinal.core.dto.posts

import com.google.gson.annotations.SerializedName

data class PostOwnerDetails(
    val _id: String,
    val email: String, // As seen in JSON
    val followerCount: Int, // As seen in JSON
    val followingCount: Int, // As seen in JSON
    // Add other fields from the JSON if they are consistently present.
    // Fields like username, fullName, profilePictureUrl are NOT present in the JSON you provided
    // for ownerId, so they should be nullable if you decide to include them.
    val username: String? = null,
    val fullName: String? = null,
    val profilePictureUrl: String? = null
)