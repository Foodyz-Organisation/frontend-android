package com.example.damprojectfinal.core.api.posts


import com.example.damprojectfinal.core.dto.posts.PostResponse
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.PATCH // <-- NEW: Import PATCH annotation

interface ReelsApiService {

    // CORRECTED: Path changed to "posts/reels-feed" to match backend
    // CORRECTED: Removed 'offset' parameter as backend uses cursor-based pagination
    @GET("posts/reels-feed") // Removed leading slash as BASE_URL handles it
    suspend fun getReels(
        @Query("limit") limit: Int,
        @Query("cursor") cursor: String? // Nullable for the first request
        // The x-user-id header will be automatically added by AuthInterceptor
    ): List<PostResponse>

    // CORRECTED: Changed from POST to PATCH, and path to "posts/{id}/view/increment"
    @PATCH("posts/{id}/view/increment") // Correct path and method for incrementing view
    suspend fun incrementReelView(@Path("id") reelId: String)
}
