package com.example.damprojectfinal.core.dto.normalUser

import com.example.damprojectfinal.core.dto.posts.PostResponse // Assuming this is your Kotlin Post data class

data class ProfileUiState(
    val isLoadingProfile: Boolean = false,
    val isLoadingPosts: Boolean = false,
    val isLoadingSavedPosts: Boolean = false,
    val userProfile: UserProfile? = null, // UserProfile is also in this package
    val userPosts: List<PostResponse> = emptyList(),
    val savedPosts: List<PostResponse> = emptyList(),
    val errorMessage: String? = null,
    val selectedTabIndex: Int = 0 // 0 for Posts, 1 for Saved
)
