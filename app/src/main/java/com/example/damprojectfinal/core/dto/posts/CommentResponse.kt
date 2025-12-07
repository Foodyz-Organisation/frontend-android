package com.example.damprojectfinal.core.dto.posts

import com.google.gson.annotations.SerializedName

data class CommentResponse(
    @SerializedName("_id") val id: String,
    val text: String,
    // No user info in public phase for comments
    // val author: AuthorResponse, // Will be added later when we link comments to users
    val createdAt: String,
    val updatedAt: String
)