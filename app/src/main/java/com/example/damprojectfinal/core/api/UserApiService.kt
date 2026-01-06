package com.example.damprojectfinal.core.api

import com.example.damprojectfinal.core.dto.user.UpdateUserRequest
import com.example.damprojectfinal.core.dto.user.UserResponse
import com.example.damprojectfinal.core.dto.user.ProfilePictureUploadResponse
import com.example.damprojectfinal.core.dto.auth.UserInfoResponse
import com.example.damprojectfinal.core.dto.auth.OrderResponse
import com.example.damprojectfinal.core.dto.posts.UploadResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.formData // Import for multipart body construction
import io.ktor.client.request.forms.submitFormWithBinaryData // Import for file submission
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.contentType
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import java.io.File // Required for handling the file object
import com.example.damprojectfinal.core.api.TokenManager
import com.example.damprojectfinal.core.api.BaseUrlProvider

class UserApiService(private val tokenManager: TokenManager) {

    private val BASE_URL = BaseUrlProvider.BASE_URL

    private val client = HttpClient(Android) {
        // Conversion JSON automatique
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }

        // Timeout des requêtes
        install(HttpTimeout) {
            requestTimeoutMillis = 15000
        }
    }
    suspend fun updateProfile(request: UpdateUserRequest, token: String, userId: String? = null): UserResponse {
        // Use actual user ID if provided, otherwise use /users/me (if backend supports it)
        val url = if (userId != null) {
            "$BASE_URL/users/$userId"
        } else {
            "$BASE_URL/users/me"
        }
        val response = client.patch(url) {
            contentType(ContentType.Application.Json)
            setBody(request)
            headers {
                append("Authorization", "Bearer $token")
            }
        }
        return response.body()
    }

    private suspend fun addAuthHeader(builder: io.ktor.client.request.HttpRequestBuilder) {
        val token = tokenManager.getAccessTokenAsync()
        if (token.isNullOrEmpty()) {
            builder.header(HttpHeaders.Authorization, "Bearer $token")
        }
    }

    suspend fun toggleActive(token: String): UserResponse {
        val url = "$BASE_URL/users/me/toggle"
        val response = client.patch(url) {
            headers {
                append("Authorization", "Bearer $token")
            }
        }
        return response.body()
    }

    suspend fun getUserById(id: String, token: String): UserResponse {
        val url = "$BASE_URL/users/$id"
        val response = client.get(url) {
            headers {
                append("Authorization", "Bearer $token")
            }
        }
        return response.body()

    }

    suspend fun getUserInfo(userId: String): UserInfoResponse {
        return client.get("$BASE_URL/users/$userId") {
            addAuthHeader(this)
        }.body()
    }

    // ⭐ NEW FUNCTION: Upload Profile Image via Multipart ⭐
    // Strategy: Upload image to /posts/uploads, then update user profile with the URL

    suspend fun uploadProfileImage(id: String, file: File, token: String): UserResponse {
        // Step 1: Upload the image using the posts/uploads endpoint
        val uploadUrl = "$BASE_URL/posts/uploads"
        
        val uploadResponse = client.submitFormWithBinaryData(
            url = uploadUrl,
            formData = formData {
                // 'files' must match the key used in the backend (plural, as it accepts multiple files)
                append(
                    key = "files",
                    value = file.readBytes(),
                    headers = Headers.build {
                        append(HttpHeaders.ContentType, "image/${file.extension}")
                        append(HttpHeaders.ContentDisposition, "filename=\"${file.name}\"")
                    })
            }
        ) {
            headers {
                append("Authorization", "Bearer $token")
            }
        }
        
        // Check if upload was successful
        if (uploadResponse.status != HttpStatusCode.OK && uploadResponse.status != HttpStatusCode.Created) {
            val errorBody = try {
                uploadResponse.body<String>()
            } catch (e: Exception) {
                "Unknown error: ${uploadResponse.status}"
            }
            throw Exception("Failed to upload image: HTTP ${uploadResponse.status} - $errorBody")
        }
        
        // Step 2: Parse the upload response to get the image URL
        val uploadResult = try {
            uploadResponse.body<UploadResponse>()
        } catch (e: Exception) {
            android.util.Log.e("UserApiService", "Failed to parse upload response: ${e.message}", e)
            throw Exception("Failed to parse upload response: ${e.message}")
        }
        
        if (uploadResult.urls.isEmpty()) {
            throw Exception("Upload succeeded but no URL was returned")
        }
        
        val imageUrl = uploadResult.urls.first()
        android.util.Log.d("UserApiService", "Image uploaded successfully, URL: $imageUrl")
        
        // Step 3: Update the user profile with the image URL using the actual user ID
        val updateRequest = UpdateUserRequest(profilePictureUrl = imageUrl)
        val updatedUser = updateProfile(updateRequest, token, userId = id)
        
        return updatedUser
    }

    suspend fun getUserOrders(userId: String): List<OrderResponse> {
        return client.get("$BASE_URL/users/$userId/orders") {
            addAuthHeader(this)
        }.body()
    }

    /**
     * ⭐ NEW FUNCTION: Upload Profile Picture via dedicated endpoint ⭐
     * 
     * This endpoint bypasses all AI validation and is specifically designed for profile pictures.
     * Endpoint: POST /users/:id/profile-picture
     * Form Data Key: profilePicture (⚠️ Important: use this exact key name)
     * 
     * @param id User ID
     * @param file Image file to upload (max 5MB, image types only)
     * @param token Authentication token
     * @return ProfilePictureUploadResponse containing the uploaded profile picture URL and updated user data
     * @throws Exception if file validation fails or upload fails
     */
    suspend fun uploadProfilePicture(id: String, file: File, token: String): ProfilePictureUploadResponse {
        // Validate file type
        val fileExtension = file.extension.lowercase()
        val validImageExtensions = listOf("jpg", "jpeg", "png", "gif", "webp")
        if (!validImageExtensions.contains(fileExtension)) {
            throw Exception("Only image files are allowed (jpg, jpeg, png, gif, webp)")
        }

        // Validate file size (5MB max)
        val maxSizeBytes = 5 * 1024 * 1024 // 5MB
        if (file.length() > maxSizeBytes) {
            throw Exception("File size must be less than 5MB")
        }

        // Determine MIME type
        val mimeType = when (fileExtension) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            else -> "image/jpeg" // Default fallback
        }

        android.util.Log.d("UserApiService", "Uploading profile picture: ${file.name}, size: ${file.length()} bytes, MIME: $mimeType")

        // Upload to the new dedicated endpoint
        val uploadUrl = "$BASE_URL/users/$id/profile-picture"
        
        val uploadResponse = client.submitFormWithBinaryData(
            url = uploadUrl,
            formData = formData {
                // ⚠️ Important: Use 'profilePicture' as the key (not 'files' or 'image')
                append(
                    key = "profilePicture",
                    value = file.readBytes(),
                    headers = Headers.build {
                        append(HttpHeaders.ContentType, mimeType)
                        append(HttpHeaders.ContentDisposition, "filename=\"${file.name}\"")
                    }
                )
            }
        ) {
            headers {
                append("Authorization", "Bearer $token")
            }
        }
        
        // Check if upload was successful
        if (uploadResponse.status != HttpStatusCode.OK && uploadResponse.status != HttpStatusCode.Created) {
            val errorBody = try {
                uploadResponse.body<String>()
            } catch (e: Exception) {
                "Unknown error: ${uploadResponse.status}"
            }
            android.util.Log.e("UserApiService", "Profile picture upload failed: HTTP ${uploadResponse.status} - $errorBody")
            throw Exception("Failed to upload profile picture: HTTP ${uploadResponse.status} - $errorBody")
        }
        
        // Parse the response
        val uploadResult = try {
            uploadResponse.body<ProfilePictureUploadResponse>()
        } catch (e: Exception) {
            android.util.Log.e("UserApiService", "Failed to parse profile picture upload response: ${e.message}", e)
            throw Exception("Failed to parse upload response: ${e.message}")
        }
        
        android.util.Log.d("UserApiService", "Profile picture uploaded successfully: ${uploadResult.profilePictureUrl}")
        return uploadResult
    }
}