package com.example.damprojectfinal.core.dto.posts

import com.google.gson.annotations.SerializedName

data class CommentResponse(
    @SerializedName("_id") val id: String,
    val text: String,
    // Optional author fields (backend may or may not send them)
    @SerializedName("authorName") val authorName: String? = null,
    @SerializedName("authorId") val authorId: String? = null,
    @SerializedName("authorUsername") val authorUsername: String? = null,
    @SerializedName("authorAvatar") val authorAvatar: String? = null,
    val createdAt: String,
    val updatedAt: String
)