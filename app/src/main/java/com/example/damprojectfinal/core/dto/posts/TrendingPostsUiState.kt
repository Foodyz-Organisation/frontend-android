package com.example.damprojectfinal.core.dto.posts


import com.example.damprojectfinal.core.dto.posts.PostResponse

data class TrendingPostsUiState(
    val isLoading: Boolean = false,
    val posts: List<PostResponse> = emptyList(),
    val errorMessage: String? = null
)
