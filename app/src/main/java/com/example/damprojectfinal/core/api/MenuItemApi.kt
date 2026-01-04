package com.example.damprojectfinal.core.api

import com.example.damprojectfinal.core.dto.menu.GroupedMenuResponse
import com.example.damprojectfinal.core.dto.menu.MenuItemResponseDto
import com.example.damprojectfinal.core.dto.menu.UpdateMenuItemDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface MenuItemApi {

    // POST: Create
    @Multipart
    @POST("menu-items")
    suspend fun createMenuItem(
        @Part("createMenuItemDto") menuItemDto: RequestBody,
        @Part image: MultipartBody.Part,
        @Header("Authorization") token: String
    ): Response<MenuItemResponseDto>

    // GET List: Grouped by Category
    @GET("menu-items/by-professional/{professionalId}")
    suspend fun getGroupedMenu(
        @Path("professionalId") professionalId: String,
        @Header("Authorization") token: String
    ): Response<GroupedMenuResponse>

    // ⭐️ NEW: GET Single Item Details (For Edit Screen)
    @GET("menu-items/{id}")
    suspend fun getMenuItemDetails(
        @Path("id") id: String,
        @Header("Authorization") token: String
    ): Response<MenuItemResponseDto>

    // PUT: Update (JSON only, no image)
    @PUT("menu-items/{id}")
    suspend fun updateMenuItem(
        @Path("id") id: String,
        @Body updateDto: UpdateMenuItemDto,
        @Header("Authorization") token: String
    ): Response<MenuItemResponseDto>

    // PUT: Update with Image (Multipart - needs backend endpoint)
    @Multipart
    @PUT("menu-items/{id}/with-image")
    suspend fun updateMenuItemWithImage(
        @Path("id") id: String,
        @Part("updateMenuItemDto") updateDto: RequestBody,
        @Part image: MultipartBody.Part,
        @Header("Authorization") token: String
    ): Response<MenuItemResponseDto>

    // DELETE: Remove
    @DELETE("menu-items/{id}")
    suspend fun deleteMenuItem(
        @Path("id") id: String,
        @Header("Authorization") token: String
    ): Response<MenuItemResponseDto>

    // GET: Intensity Types Configuration
    @GET("menu-items/intensity-types/config")
    suspend fun getIntensityTypesConfig(
        @Header("Authorization") token: String
    ): Response<Map<String, IntensityTypeConfig>>
    
    // GET: AI Suggestions for Menu Item
    @GET("menu-items/{id}/suggestions")
    suspend fun getMenuItemSuggestions(
        @Path("id") id: String,
        @Header("Authorization") token: String
    ): Response<com.example.damprojectfinal.core.dto.menu.MenuSuggestionsDto>
}

// Data class for intensity type configuration from backend
data class IntensityTypeConfig(
    val type: String,
    val icon: String,
    val defaultColor: String,
    val label: String
)