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
            android.util.Log.d("MenuItemRepository", "🔄 Updating menu item (JSON): id=$id, endpoint=PUT /menu-items/$id")
            android.util.Log.d("MenuItemRepository", "📦 Payload: name=${payload.name}, price=${payload.price}, ingredients=${payload.ingredients?.size ?: 0}")
            
            // Verify the ID is valid
            if (id.isBlank()) {
                android.util.Log.e("MenuItemRepository", "❌ Invalid item ID: empty or blank")
                return Result.failure(Exception("Invalid item ID: cannot be empty"))
            }
            
            val formattedToken = formatToken(authToken)
            android.util.Log.d("MenuItemRepository", "🔑 Token formatted: ${formattedToken.take(20)}...")
            
            val response = api.updateMenuItem(id, payload, formattedToken)
            
            android.util.Log.d("MenuItemRepository", "📡 Response code: ${response.code()}")
            android.util.Log.d("MenuItemRepository", "📡 Response message: ${response.message()}")
            
            if (response.isSuccessful && response.body() != null) {
                android.util.Log.d("MenuItemRepository", "✅ Update successful")
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Update Error"
                android.util.Log.e("MenuItemRepository", "❌ Update failed: HTTP ${response.code()} - $errorBody")
                android.util.Log.e("MenuItemRepository", "📡 Request URL: PUT /menu-items/$id")
                android.util.Log.e("MenuItemRepository", "📡 Full error response: $errorBody")
                
                // Check if it's a 404 - route doesn't exist
                if (response.code() == 404) {
                    android.util.Log.e("MenuItemRepository", "⚠️ 404 Error: Backend route PUT /menu-items/:id may not be registered!")
                    android.util.Log.e("MenuItemRepository", "⚠️ Please verify your backend controller has @Put(':id') route")
                }
                
                Result.failure(Exception("HTTP ${response.code()}: $errorBody"))
            }
        } catch (e: Exception) {
            android.util.Log.e("MenuItemRepository", "❌ Exception during update: ${e.message}", e)
            android.util.Log.e("MenuItemRepository", "❌ Stack trace: ${e.stackTraceToString()}")
            Result.failure(e)
        }
    }

    /**
     * Updates an existing menu item with image upload (Multipart).
     */
    suspend fun updateMenuItemWithImage(
        id: String,
        payload: UpdateMenuItemDto,
        imageFile: FileWithMime,
        authToken: String
    ): Result<MenuItemResponseDto> {
        return try {
            android.util.Log.d("MenuItemRepository", "🔄 Updating menu item (Multipart): id=$id, endpoint=PUT /menu-items/$id/with-image")
            val jsonString = gson.toJson(payload)
            val jsonPart = jsonString.toRequestBody("text/plain".toMediaTypeOrNull())
            
            val requestFile = imageFile.file.asRequestBody(imageFile.mimeType.toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("image", imageFile.file.name, requestFile)
            
            android.util.Log.d("MenuItemRepository", "📦 Sending: updateMenuItemDto=${jsonString.take(100)}..., image=${imageFile.file.name}")
            
            val response = api.updateMenuItemWithImage(id, jsonPart, filePart, formatToken(authToken))
            if (response.isSuccessful && response.body() != null) {
                android.util.Log.d("MenuItemRepository", "✅ Update with image successful")
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Update with Image Error"
                android.util.Log.e("MenuItemRepository", "❌ Update with image failed: HTTP ${response.code()} - $errorBody")
                android.util.Log.e("MenuItemRepository", "📡 Request URL: PUT /menu-items/$id/with-image")
                Result.failure(Exception("HTTP ${response.code()}: $errorBody"))
            }
        } catch (e: Exception) {
            android.util.Log.e("MenuItemRepository", "❌ Exception during update with image: ${e.message}", e)
            Result.failure(e)
        }
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
    
    // --------------------------------------------------
    // 5. GET AI SUGGESTIONS
    // --------------------------------------------------
    /**
     * Fetches AI-generated suggestions for a menu item.
     * Endpoint: GET /menu-items/:id/suggestions
     */
    suspend fun getMenuItemSuggestions(
        id: String,
        authToken: String
    ): Result<com.example.damprojectfinal.core.dto.menu.MenuSuggestionsDto> {
        return try {
            android.util.Log.d("MenuItemRepository", "🤖 Fetching AI suggestions for item: $id")
            val response = api.getMenuItemSuggestions(id, formatToken(authToken))
            
            if (response.isSuccessful && response.body() != null) {
                android.util.Log.d("MenuItemRepository", "✅ AI suggestions received")
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Failed to fetch suggestions"
                android.util.Log.e("MenuItemRepository", "❌ Failed to fetch suggestions: $errorBody")
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            android.util.Log.e("MenuItemRepository", "❌ Exception fetching suggestions: ${e.message}", e)
            Result.failure(e)
        }
    }
}