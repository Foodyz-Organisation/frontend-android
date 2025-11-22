package com.example.damprojectfinal.core.api

import com.example.damprojectfinal.core.dto.auth.SimpleMessageResponse
import com.example.damprojectfinal.core.dto.user.UpdateUserRequest
import com.example.damprojectfinal.core.dto.user.UserResponse
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
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders // Import for file headers
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import java.io.File // Required for handling the file object

class UserApiService {

    private val BASE_URL = "http://10.0.2.2:3000"

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 15000
        }
    }

    /**
     * Update the logged-in user's profile
     * PATCH /users/me
     */
    suspend fun updateProfile(request: UpdateUserRequest, token: String): UserResponse {
        val url = "$BASE_URL/users/me"
        val response = client.patch(url) {
            contentType(ContentType.Application.Json)
            setBody(request)
            headers {
                append("Authorization", "Bearer $token")
            }
        }
        return response.body()
    }

    /**
     * Toggle / deactivate the logged-in user's account
     * PATCH /users/me/toggle
     */
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

    // ⭐ NEW FUNCTION: Upload Profile Image via Multipart ⭐

    suspend fun uploadProfileImage(id: String, file: File, token: String): UserResponse {
        // Use the ID in the URL path, matching your NestJS controller structure
        val url = "$BASE_URL/users/$id/upload-profile-image"

        val response = client.submitFormWithBinaryData(
            url = url,
            formData = formData {
                // 'file' must match the key used in NestJS FileInterceptor('file', ...)
                append(key = "file",
                    value = file.readBytes(),
                    headers = Headers.build {
                        // Set correct ContentType and filename header for the part
                        append(HttpHeaders.ContentType, "image/${file.extension}")
                        append(HttpHeaders.ContentDisposition, "filename=\"${file.name}\"")
                    })
            }
        ) {
            // Add Authorization header
            headers {
                append("Authorization", "Bearer $token")
            }
        }

        // Return the updated UserResponse, assuming NestJS sends it back
        return response.body()
    }

}