package com.example.damprojectfinal.user.feature_posts.ui.post_management

import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.damprojectfinal.core.api.posts.PostsApiService
import com.example.damprojectfinal.core.retro.RetrofitClient
import com.example.damprojectfinal.core.dto.posts.CreateCommentDto
import com.example.damprojectfinal.core.dto.posts.PostResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

open class PostsViewModel : ViewModel() {

    // StateFlow to hold the list of posts
    protected val _posts = MutableStateFlow<List<PostResponse>>(emptyList())
    open val posts: StateFlow<List<PostResponse>> = _posts

    // StateFlow to hold the loading status
    protected open val _isLoading = MutableStateFlow(false)
    open val isLoading: StateFlow<Boolean> = _isLoading

    // StateFlow to hold any error messages
    protected open val _errorMessage = MutableStateFlow<String?>(null)
    open val errorMessage: StateFlow<String?> = _errorMessage

    // --- NEW: Reference to the API service ---
    protected val postsApiService: PostsApiService = RetrofitClient.postsApiService
    // --- END NEW ---

    init {
        // Fetch posts immediately when the ViewModel is created
        fetchPosts()
    }

    open fun fetchPosts() {
        viewModelScope.launch {
            _isLoading.value = true // Set loading to true
            _errorMessage.value = null // Clear any previous errors
            try {
                val fetchedPosts = postsApiService.getPosts() // Use the service reference
                _posts.value = fetchedPosts // Update the posts list
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load posts: ${e.localizedMessage ?: e.message}"
            } finally {
                _isLoading.value = false // Set loading to false regardless of success or failure
            }
        }
    }

    // Corrected: Make this a suspend function and call the API
    open suspend fun getPostById (postId: String): PostResponse { // <--- ADD 'suspend'
        return postsApiService.getPostById(postId) // <--- Call the API service
    }



    // Call this to clear any error messages
    open fun clearError() {
        _errorMessage.value = null
    }

    // --- NEW: INTERACTION AND CONTENT MANAGEMENT FUNCTIONS ---

    // Function to update a post's caption
    open fun updatePostCaption(postId: String, newCaption: String) {
        if (newCaption.isBlank()) {
            _errorMessage.value = "Caption cannot be empty."
            return
        }
        viewModelScope.launch {
            try {
                // Assuming PostsApiService.UpdateCaptionRequest is correctly defined in PostsApiService.kt
                val updatedPost = postsApiService.updatePostCaption(postId, PostsApiService.UpdateCaptionRequest(newCaption))
                _posts.update { currentPosts ->
                    currentPosts.map { post ->
                        if (post._id == postId) updatedPost else post // Replace the old post with the updated one
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update post: ${e.localizedMessage ?: e.message}"
            }
        }
    }

    // Function to delete a post
    open fun deletePost(postId: String) {
        viewModelScope.launch {
            try {
                postsApiService.deletePost(postId)
                _posts.update { currentPosts ->
                    currentPosts.filter { post -> post._id != postId } // Remove the deleted post from the list
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete post: ${e.localizedMessage ?: e.message}"
            }
        }
    }

    // Function to increment like count
    open fun incrementLikeCount(postId: String) {
        viewModelScope.launch {
            try {
                val updatedPost = postsApiService.addLike(postId)
                _posts.update { currentPosts ->
                    currentPosts.map { post ->
                        if (post._id == postId) updatedPost else post
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to like post: ${e.localizedMessage ?: e.message}"
            }
        }
    }

    // Function to decrement like count
    open fun decrementLikeCount(postId: String) {
        viewModelScope.launch {
            try {
                val updatedPost = postsApiService.removeLike(postId)
                _posts.update { currentPosts ->
                    currentPosts.map { post ->
                        if (post._id == postId) updatedPost else post
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to unlike post: ${e.localizedMessage ?: e.message}"
            }
        }
    }

    // Function to create a comment
    open fun createComment(postId: String, commentText: String) {
        if (commentText.isBlank()) {
            _errorMessage.value = "Comment cannot be empty."
            return
        }
        viewModelScope.launch {
            try {
                postsApiService.createComment(postId, CreateCommentDto(commentText))
                // After creating a comment, the post's commentCount is updated on backend.
                // We should re-fetch the post to get the latest counts, or fetch all posts again.
                // For now, re-fetching all posts is simplest but not most efficient.
                // A better approach would be to fetch the updated single post or optimistic update.
                // Let's re-fetch the specific post's data for efficiency.
                val updatedPost = postsApiService.getPostById(postId)
                _posts.update { currentPosts ->
                    currentPosts.map { post ->
                        if (post._id == postId) updatedPost else post
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add comment: ${e.localizedMessage ?: e.message}"
            }
        }
    }

    // Function to increment save count
    open fun incrementSaveCount(postId: String) {
        viewModelScope.launch {
            try {
                val updatedPost = postsApiService.addSave(postId)
                _posts.update { currentPosts ->
                    currentPosts.map { post ->
                        if (post._id == postId) updatedPost else post
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to save post: ${e.localizedMessage ?: e.message}"
            }
        }
    }

    // Function to decrement save count
    open fun decrementSaveCount(postId: String) {
        viewModelScope.launch {
            try {
                val updatedPost = postsApiService.removeSave(postId)
                _posts.update { currentPosts ->
                    currentPosts.map { post ->
                        if (post._id == postId) updatedPost else post
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to unsave post: ${e.localizedMessage ?: e.message}"
            }
        }
    }
    // --- END NEW FUNCTIONS ---
}