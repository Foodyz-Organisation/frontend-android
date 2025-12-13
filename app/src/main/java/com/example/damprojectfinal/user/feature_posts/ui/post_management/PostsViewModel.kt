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
import android.util.Log
import retrofit2.HttpException

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

    // --- NEW: Food Type Preference State ---
    // Track user's preferred food types (cached locally)
    private val _userPreferences = MutableStateFlow<List<String>>(emptyList())
    open val userPreferences: StateFlow<List<String>> = _userPreferences

    // Track which posts are currently being processed for preference (loading state per post)
    private val _preferringPosts = MutableStateFlow<Set<String>>(emptySet())
    open val preferringPosts: StateFlow<Set<String>> = _preferringPosts

    // Success message for snackbar
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    open val snackbarMessage: StateFlow<String?> = _snackbarMessage
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

    // Function to fetch posts filtered by food type
    open fun fetchPostsByFoodType(foodType: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                // Let Retrofit handle URL encoding automatically for path parameters
                // Retrofit will properly encode "Street food" to "Street%20food" in the URL
                val fetchedPosts = postsApiService.getPostsByFoodType(foodType)
                _posts.value = fetchedPosts
            } catch (e: HttpException) {
                // Handle HTTP errors (400 Bad Request = invalid food type or no posts)
                if (e.code() == 400) {
                    // No posts for this food type or invalid food type - show empty list gracefully
                    _posts.value = emptyList()
                    _errorMessage.value = null // Clear any previous errors
                    Log.d("PostsViewModel", "No posts found for food type: $foodType (400 response)")
                } else {
                    // For other HTTP errors, show error message
                    _errorMessage.value = "Failed to load posts: ${e.message()}"
                }
            } catch (e: Exception) {
                // Handle other exceptions (network errors, etc.)
                _errorMessage.value = "Failed to load posts: ${e.localizedMessage ?: e.message}"
            } finally {
                _isLoading.value = false
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
        viewModelScope.launch {
            try {
                createCommentImmediate(postId, commentText)
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

    // Suspend version used by screens when they need immediate result
    open suspend fun createCommentImmediate(postId: String, commentText: String) =
        postsApiService.createComment(postId, CreateCommentDto(commentText))

    // Simple validator that callers can use before invoking a suspend create
    fun validateComment(commentText: String): String? {
        return if (commentText.isBlank()) "Comment cannot be empty." else null
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

    // --- NEW: Food Type Preference Functions ---

    /**
     * Prefer a post's food type - adds it to user's preferred food types
     * The x-user-id header is automatically added by AuthInterceptor
     */
    open fun preferFoodType(postId: String) {
        viewModelScope.launch {
            // Check if already processing this post
            if (_preferringPosts.value.contains(postId)) {
                Log.d("PostsViewModel", "Already processing preference for post: $postId")
                return@launch
            }

            // Get the post to extract food type
            val post = _posts.value.find { it._id == postId }
            if (post == null || post.foodType.isNullOrBlank()) {
                _errorMessage.value = "Post not found or has no food type"
                return@launch
            }

            val foodType = post.foodType

            // Check if already preferred
            if (_userPreferences.value.contains(foodType)) {
                Log.d("PostsViewModel", "Food type '$foodType' is already preferred")
                _snackbarMessage.value = "Already in your preferences"
                return@launch
            }

            // Mark as processing
            _preferringPosts.update { it + postId }

            try {
                // Call API to prefer food type
                val updatedUser = postsApiService.preferFoodType(postId)

                // Update local preferences
                _userPreferences.value = updatedUser.preferredFoodTypes

                // Show success message
                _snackbarMessage.value = "Added to preferences"

                // Optionally refresh feed to get personalized results
                // Uncomment if you want immediate feed refresh after preference
                // fetchPosts()

                Log.d("PostsViewModel", "Successfully preferred food type: $foodType")
                Log.d("PostsViewModel", "Updated preferences: ${updatedUser.preferredFoodTypes}")

            } catch (e: Exception) {
                val errorMsg = when {
                    e.message?.contains("404") == true -> "Post or user not found"
                    e.message?.contains("400") == true -> "Invalid request"
                    e.message?.contains("network", ignoreCase = true) == true -> "No internet connection"
                    else -> "Failed to add preference: ${e.localizedMessage ?: e.message}"
                }
                _errorMessage.value = errorMsg
                _snackbarMessage.value = errorMsg
                Log.e("PostsViewModel", "Error preferring food type: ${e.message}", e)
            } finally {
                // Remove from processing set
                _preferringPosts.update { it - postId }
            }
        }
    }

    /**
     * Check if a food type is preferred
     */
    open fun isFoodTypePreferred(foodType: String?): Boolean {
        if (foodType.isNullOrBlank()) return false
        return _userPreferences.value.contains(foodType)
    }

    /**
     * Check if a specific post's food type is preferred
     */
    open fun isPostFoodTypePreferred(postId: String): Boolean {
        val post = _posts.value.find { it._id == postId }
        return post?.foodType?.let { isFoodTypePreferred(it) } ?: false
    }

    /**
     * Check if a post is currently being processed for preference
     */
    open fun isPreferring(postId: String): Boolean {
        return _preferringPosts.value.contains(postId)
    }

    /**
     * Clear snackbar message
     */
    open fun clearSnackbarMessage() {
        _snackbarMessage.value = null
    }
    // --- END NEW FUNCTIONS ---
}