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

    // --- Food Type Preference State (Implicit System) ---
    // Preferences are now tracked implicitly by the backend from user interactions
    // This state is kept for UI display purposes only (e.g., showing preferences in settings)
    // Preferences are automatically updated by backend when user interacts with posts
    private val _userPreferences = MutableStateFlow<List<String>>(emptyList())
    open val userPreferences: StateFlow<List<String>> = _userPreferences

    // Success message for snackbar
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    open val snackbarMessage: StateFlow<String?> = _snackbarMessage
    // --- END ---

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

    // Get post by ID
    // Note: Backend automatically tracks view interaction if x-user-id header is present
    // The AuthInterceptor automatically adds x-user-id header to all requests
    open suspend fun getPostById (postId: String): PostResponse {
        // Backend will automatically track view interaction and update preferences
        return postsApiService.getPostById(postId)
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
    // Note: Backend automatically tracks preference from this interaction
    open fun incrementLikeCount(postId: String) {
        viewModelScope.launch {
            try {
                val updatedPost = postsApiService.addLike(postId)
                _posts.update { currentPosts ->
                    currentPosts.map { post ->
                        if (post._id == postId) updatedPost else post
                    }
                }
                // Backend automatically updates user preferences from this like interaction
                Log.d("PostsViewModel", "Post liked - backend will track preference automatically")
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
    // Note: Backend automatically tracks preference from this interaction
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
                // Backend automatically updates user preferences from this comment interaction
                Log.d("PostsViewModel", "Comment added - backend will track preference automatically")
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
    // Note: Backend automatically tracks preference from this interaction
    open fun incrementSaveCount(postId: String) {
        viewModelScope.launch {
            try {
                val updatedPost = postsApiService.addSave(postId)
                _posts.update { currentPosts ->
                    currentPosts.map { post ->
                        if (post._id == postId) updatedPost else post
                    }
                }
                // Backend automatically updates user preferences from this save interaction
                Log.d("PostsViewModel", "Post saved - backend will track preference automatically")
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

    // --- Food Type Preference Functions (Implicit System) ---

    /**
     * @deprecated Preferences are now tracked implicitly by the backend from user interactions.
     * This method is kept for backward compatibility but is no longer needed.
     * Preferences are automatically updated when users like, comment, save, or view posts.
     * 
     * If you still want to support explicit preference actions, you can keep this method.
     * Otherwise, it can be removed in a future version.
     */
    @Deprecated(
        message = "Preferences are now tracked implicitly. Use interactions (like, comment, save, view) instead.",
        replaceWith = ReplaceWith("// Preferences are automatically tracked from interactions")
    )
    open fun preferFoodType(postId: String) {
        viewModelScope.launch {
            try {
                // Call API to prefer food type (still works but not recommended)
                val updatedUser = postsApiService.preferFoodType(postId)
                
                // Update local preferences for display purposes
                _userPreferences.value = updatedUser.preferredFoodTypes
                
                _snackbarMessage.value = "Added to preferences"
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
            }
        }
    }

    /**
     * Check if a food type is in user's preferences (for display purposes only)
     * Note: Preferences are now tracked implicitly by backend, this is just for UI display
     */
    open fun isFoodTypePreferred(foodType: String?): Boolean {
        if (foodType.isNullOrBlank()) return false
        return _userPreferences.value.contains(foodType)
    }

    /**
     * Check if a specific post's food type is in user's preferences (for display purposes only)
     * Note: Preferences are now tracked implicitly by backend, this is just for UI display
     */
    @Deprecated(
        message = "Preferences are tracked implicitly. This method is kept for display purposes only.",
        replaceWith = ReplaceWith("// No longer needed - preferences tracked automatically")
    )
    open fun isPostFoodTypePreferred(postId: String): Boolean {
        val post = _posts.value.find { it._id == postId }
        return post?.foodType?.let { isFoodTypePreferred(it) } ?: false
    }

    /**
     * Clear snackbar message
     */
    open fun clearSnackbarMessage() {
        _snackbarMessage.value = null
    }
    // --- END NEW FUNCTIONS ---
}