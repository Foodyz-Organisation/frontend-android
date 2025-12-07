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

    // Endpoint for uploading files (POST /posts/uploads)
    // @Multipart indicates that the request body will be multipart/form-data
    @Multipart
    @POST("posts/uploads") // The path is relative to BASE_URL configured in RetrofitClient
    suspend fun uploadFiles(
        // @Part specifies a part of the multipart request.
        // "files" must exactly match the name of the field in your NestJS controller's FilesInterceptor.
        @Part files: List<MultipartBody.Part>
    ): UploadResponse

    // Endpoint for creating a post (POST /posts)
    @POST("posts")
    suspend fun createPost(
        //@Header("x-owner-type") ownerType: String, // <--- NEW: This header is now required
        // @Body indicates that the createPostDto object will be serialized into the request body (as JSON)
        @Body createPostDto: CreatePostDto
    ): PostResponse

    // --- NEW: Endpoint for fetching all posts (GET /posts) ---
    @GET("posts") // The path is relative to BASE_URL configured in RetrofitClient
    suspend fun getPosts(): List<PostResponse> // Returns a list of PostResponse objects

    // --- NEW: Endpoint for fetching a single post (GET /posts/{id}) ---
    @GET("posts/{id}")
    suspend fun getPostById(@Path("id") postId: String): PostResponse

    // --- NEW: Endpoint for fetching posts by a specific user (GET /posts/user/{userId}) ---
    @GET("posts/by-owner/{ownerId}") // Corrected path to match backend
    suspend fun getPostsByOwnerId(@Path("ownerId") ownerId: String): List<PostResponse>

    // --- NEW: Endpoint for updating a post (PATCH /posts/{id}) ---
    // UpdatePostDto would go here, but for now, we only update caption.
    // If you want a specific DTO for updating the caption only:
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

    // For DELETE requests with a body, Retrofit requires using @HTTP.
    // However, our backend delete comment endpoint currently doesn't expect a body.
    // It just expects the ID in the path.
    @DELETE("posts/comments/{commentId}") // Backend path is /posts/comments/:commentId
    suspend fun deleteComment(@Path("commentId") commentId: String): Unit // Backend returns { message: 'Comment deleted successfully' } which is Unit in Kotlin

    // --- NEW: Endpoints for Bookmarks/Saves ---
    @PATCH("posts/{postId}/save") // Changed from POST and /bookmarks to PATCH and /save
    suspend fun addSave(@Path("postId") postId: String): PostResponse

    // Backend expects DELETE /posts/:id/save
    @DELETE("posts/{postId}/save") // Changed from /bookmarks to /save
    suspend fun removeSave(@Path("postId") postId: String): PostResponse
    
    // --- NEW: Endpoint for fetching saved posts (GET /posts/saved) ---
    @GET("posts/saved/")
    suspend fun getSavedPosts(): List<PostResponse>
    
    // --- NEW: Endpoint for fetching trending posts (GET /posts/trends) ---
    @GET("posts/trends")
    suspend fun getTrendingPosts(@Query("limit") limit: Int = 10): List<PostResponse>
    // --- END NEW ---

}