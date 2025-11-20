package com.example.damprojectfinal.core.repository

import com.example.damprojectfinal.core.api.MenuItemApi
import com.example.damprojectfinal.core.dto.menu.CreateMenuItemDto
import com.example.damprojectfinal.core.dto.menu.MenuItemResponseDto
import com.example.damprojectfinal.core.`object`.FileUtil
import com.example.damprojectfinal.core.`object`.FileWithMime
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import com.google.gson.Gson

class MenuItemRepository(
    private val api: MenuItemApi,
    private val gson: Gson
) {

    /**
     * Creates a new menu item by submitting a multipart/form-data request.
     * @param payload The menu item data (DTO).
     * @param imageFile The physical file object to upload.
     * @param authToken The user's authentication token.
     * @return The created MenuItemDto from the server.
     */
    suspend fun createMenuItem(
        payload: CreateMenuItemDto,
        imageFile: FileWithMime,
        authToken: String
    ): Result<MenuItemResponseDto> {

        // --- 1. Serialize DTO to JSON String ---
        val jsonString = gson.toJson(payload)

        // --- 2. Create RequestBody for JSON Payload ---
        // Key: "createMenuItemDto" must match @Body('createMenuItemDto') in NestJS
        val jsonPart = jsonString.toRequestBody("text/plain".toMediaTypeOrNull())

        // --- 3. Create MultipartBody.Part for the File ---
        // Use the actual file and its MIME type from FileWithMime
        // 👈 FIX: Explicitly specify the type parameter T for asRequestBody (which is RequestBody)
        val requestFile = imageFile.file.asRequestBody(imageFile.mimeType.toMediaTypeOrNull())

        // Key: "image" must match FileInterceptor('image') in NestJS
        val filePart = MultipartBody.Part.createFormData(
            name = "image",
            filename = imageFile.file.name,
            body = requestFile
        )

        // --- 4. Call API and Handle Response ---
        return try {
            val tokenHeader = "Bearer $authToken"
            val response = api.createMenuItem(
                menuItemDto = jsonPart,
                image = filePart,
                token = tokenHeader
            )

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                // Handle API error messages from NestJS (e.g., validation errors)
                val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                Result.failure(Exception("API Error: $errorMessage"))
            }
        } catch (e: Exception) {
            // Handle network and serialization exceptions
            Result.failure(e)
        }
    }
}