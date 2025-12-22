package com.example.damprojectfinal.core.api.posts

import com.example.damprojectfinal.core.dto.posts.CreatePostDto
import com.example.damprojectfinal.core.dto.posts.PostResponse
import com.example.damprojectfinal.core.dto.posts.UploadResponse
import com.example.damprojectfinal.core.dto.posts.CommentResponse // <-- NEW: Import CommentResponse
import com.example.damprojectfinal.core.dto.posts.CreateCommentDto // <-- NEW: Import CreateCommentDto (we'll define this later)
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.*

interface PostsApiService {

    @Multipart
    @POST("posts/uploads") // The path is relative to BASE_URL configured in RetrofitClient
    suspend fun uploadFiles(
        @Part files: List<MultipartBody.Part>
    ): UploadResponse

    // Endpoint for creating a post (POST /posts)
    @POST("posts")
    suspend fun createPost(
        @Body createPostDto: CreatePostDto
    ): PostResponse

    // --- NEW: Endpoint for fetching all posts (GET /posts) ---
    // Note: x-user-id header is automatically added by AuthInterceptor for personalized feed
    @GET("posts") // The path is relative to BASE_URL configured in RetrofitClient
    suspend fun getPosts(): List<PostResponse> // Returns a list of PostResponse objects (personalized if x-user-id header is present)

    // --- NEW: Endpoint for fetching a single post (GET /posts/{id}) ---
    // Note: x-user-id header is automatically added by AuthInterceptor for view tracking
    @GET("posts/{id}")
    suspend fun getPostById(@Path("id") postId: String): PostResponse // View is automatically tracked if x-user-id header is present

    @GET("posts/by-owner/{ownerId}") // Corrected path to match backend
    suspend fun getPostsByOwnerId(@Path("ownerId") ownerId: String): List<PostResponse>

    data class UpdateCaptionRequest(val caption: String) // Simple DTO for this specific update
    @PATCH("posts/{id}")
    suspend fun updatePostCaption(
        @Path("id") postId: String,
        @Body request: UpdateCaptionRequest // Using our simple DTO
    ): PostResponse

    // --- NEW: Endpoint for deleting a post (DELETE /posts/{id}) ---
    @DELETE("posts/{id}")
    suspend fun deletePost(@Path("id") postId: String): PostResponse // Returns the deleted post

    // --- NEW: Endpoints for Likes ---
    @PATCH("posts/{postId}/like") // Changed from POST and /likes to PATCH and /like
    suspend fun addLike(@Path("postId") postId: String): PostResponse

    // Backend expects DELETE /posts/:id/like
    @DELETE("posts/{postId}/like") // Changed from /likes to /like
    suspend fun removeLike(@Path("postId") postId: String): PostResponse
    // --- NEW: Endpoints for Comments ---
    // Note: CreateCommentDto is not yet defined, we'll do that in the next step.
    @POST("posts/{postId}/comments")
    suspend fun createComment(
        @Path("postId") postId: String,
        @Body createCommentDto: CreateCommentDto // This DTO will contain the 'text'
    ): CommentResponse

    @GET("posts/{postId}/comments")
    suspend fun getComments(@Path("postId") postId: String): List<CommentResponse>

    @DELETE("posts/comments/{commentId}") // Backend path is /posts/comments/:commentId
    suspend fun deleteComment(@Path("commentId") commentId: String): Unit // Backend returns { message: 'Comment deleted successfully' } which is Unit in Kotlin

    @PATCH("posts/{postId}/save") // Changed from POST and /bookmarks to PATCH and /save
    suspend fun addSave(@Path("postId") postId: String): PostResponse

    @DELETE("posts/{postId}/save") // Changed from /bookmarks to /save
    suspend fun removeSave(@Path("postId") postId: String): PostResponse
    
    @GET("posts/saved/")
    suspend fun getSavedPosts(): List<PostResponse>
    
    @GET("posts/trends")
    suspend fun getTrendingPosts(@Query("limit") limit: Int = 10): List<PostResponse>
    
    // --- NEW: Endpoint for fetching all food types (GET /posts/food-types) ---
    @GET("posts/food-types")
    suspend fun getFoodTypes(): List<String> // Returns a list of food type strings

    // --- NEW: Endpoint for fetching posts by food type (GET /posts/by-food-type/:foodType) ---
    @GET("posts/by-food-type/{foodType}")
    suspend fun getPostsByFoodType(@Path("foodType") foodType: String): List<PostResponse> // Returns filtered posts by food type

    // Note: preferFoodType endpoint removed - preferences are now learned automatically from user interactions
    // (like, save, comment, view actions automatically update preferences)

}