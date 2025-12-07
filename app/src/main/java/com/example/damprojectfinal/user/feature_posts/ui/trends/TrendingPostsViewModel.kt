package com.example.damprojectfinal.user.feature_posts.ui.trends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.damprojectfinal.core.retro.RetrofitClient
import com.example.damprojectfinal.core.dto.posts.PostResponse
import com.example.damprojectfinal.core.dto.posts.TrendingPostsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

class TrendingPostsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TrendingPostsUiState())
    val uiState: StateFlow<TrendingPostsUiState> = _uiState.asStateFlow()

    init {
        fetchTrendingPosts(limit = 5)
    }

    fun fetchTrendingPosts(limit: Int = 5) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val trendingPosts = RetrofitClient.postsApiService.getTrendingPosts(limit)
                _uiState.update { it.copy(posts = trendingPosts, isLoading = false) }
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is IOException -> "Network error. Check your connection."
                    else -> "Failed to load trending posts: ${e.localizedMessage ?: e.message}"
                }
                _uiState.update { it.copy(errorMessage = errorMessage, isLoading = false) }
                e.printStackTrace()
            }
        }
    }

    fun refreshTrendingPosts() {
        fetchTrendingPosts(limit = 5)
    }
}
