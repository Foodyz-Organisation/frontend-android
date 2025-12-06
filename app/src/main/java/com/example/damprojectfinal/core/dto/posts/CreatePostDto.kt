package com.example.damprojectfinal.core.dto.posts

import com.example.damprojectfinal.core.dto.normalUser.UserProfile
import com.google.gson.annotations.SerializedName

// Matches backend's CreatePostDto
data class CreatePostDto(
    val caption: String,
    val mediaUrls: List<String>,
    val mediaType: String // "image", "reel", or "carousel"
)

// Data class for the response when a post is successfully created
// This mirrors your backend's Post schema, now with reel-specific fields
data class PostResponse(
    val _id: String, // MongoDB ObjectId
    val caption: String,
    val mediaUrls: List<String>,
    val mediaType: String,
    val createdAt: String, // ISO 8601 date string
    val updatedAt: String, // ISO 8601 date string
    @SerializedName("__v") // For Mongoose's version key
    val version: Int,

    val ownerId: UserProfile?, // Changed name from userId to ownerId
    val ownerModel: String, // NEW: Added to map the "ownerModel" field from backend

    val viewsCount: Int = 0, // Added for reel analytics, default to 0
    val thumbnailUrl: String? = null, // Added for reel thumbnail, nullable as not all posts have it
    val duration: Double? = null, // Added for reel duration, nullable
    val aspectRatio: String? = null, // Added for reel aspect ratio, nullable

    val likeCount: Int,
    val commentCount: Int,
    val saveCount: Int,

    // --- NEW FIELDS FOR POST DETAILS SCREEN ---
    val description: String? = null, // Optional description
    val ingredients: List<String>? = null, // Optional list of ingredients
    val postRating: Double? = null, // Optional overall rating for the post
    val reviewsCount: Int? = null, // Optional count of reviews for the post

)
