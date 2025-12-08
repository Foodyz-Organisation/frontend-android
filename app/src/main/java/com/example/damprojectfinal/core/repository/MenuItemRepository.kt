package com.example.damprojectfinal.core.repository

import com.example.damprojectfinal.core.api.MenuItemApi
import com.example.damprojectfinal.core.dto.menu.CreateMenuItemDto
import com.example.damprojectfinal.core.dto.menu.MenuItemResponseDto
import com.example.damprojectfinal.core.dto.menu.GroupedMenuResponse // Alias for map
import com.example.damprojectfinal.core.dto.menu.UpdateMenuItemDto // DTO for update
import com.example.damprojectfinal.core.`object`.FileWithMime
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class MenuItemRepository(
    private val api: MenuItemApi,
    private val gson: Gson
) {

    // Helper to format the token for the Authorization header
    private fun formatToken(authToken: String) = "Bearer $authToken"

    // --------------------------------------------------
    // 1. CREATE (Existing Logic)
    // --------------------------------------------------
    suspend fun createMenuItem(
        payload: CreateMenuItemDto,
        imageFile: FileWithMime,
        authToken: String
    ): Result<MenuItemResponseDto> {
        val jsonString = gson.toJson(payload)
        val jsonPart = jsonString.toRequestBody("text/plain".toMediaTypeOrNull())
        val requestFile = imageFile.file.asRequestBody(imageFile.mimeType.toMediaTypeOrNull())
        val filePart = MultipartBody.Part.createFormData("image", imageFile.file.name, requestFile)

        return try {
            val response = api.createMenuItem(jsonPart, filePart, formatToken(authToken))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Unknown creation error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --------------------------------------------------
    // 2. FETCH (Grouped Menu List)
    // --------------------------------------------------
    suspend fun getGroupedMenu(
        professionalId: String,
        authToken: String
    ): Result<GroupedMenuResponse> {
        return try {
            val response = api.getGroupedMenu(professionalId, formatToken(authToken))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to fetch menu."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    // ⭐️ NEW: Get Single Item Details
    suspend fun getMenuItemDetails(id: String, authToken: String): Result<MenuItemResponseDto> {
        return try {
            val response = api.getMenuItemDetails(id, formatToken(authToken))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to load item details"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    // --------------------------------------------------
    // 3. UPDATE (PUT)
    // --------------------------------------------------
    /**
     * Updates an existing menu item by ID with a JSON payload.
     */
    suspend fun updateMenuItem(id: String, payload: UpdateMenuItemDto, authToken: String): Result<MenuItemResponseDto> {
        return try {
            val response = api.updateMenuItem(id, payload, formatToken(authToken))
            if (response.isSuccessful && response.body() != null) Result.success(response.body()!!)
            else Result.failure(Exception(response.errorBody()?.string() ?: "Update Error"))
        } catch (e: Exception) { Result.failure(e) }
    }


    // --------------------------------------------------
    // 4. DELETE
    // --------------------------------------------------
    /**
     * Deletes a menu item by ID.
     */
    suspend fun deleteMenuItem(
        id: String,
        authToken: String
    ): Result<MenuItemResponseDto> {
        return try {
            val response = api.deleteMenuItem(id, formatToken(authToken))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                // NestJS usually sends back the deleted item on success, check status codes if needed.
                Result.failure(Exception(response.errorBody()?.string() ?: "Unknown deletion error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}