package com.example.damprojectfinal.core.api

import com.example.damprojectfinal.core.dto.user.UpdateUserRequest
import com.example.damprojectfinal.core.dto.user.UserResponse
import com.example.damprojectfinal.core.dto.auth.UserInfoResponse
import com.example.damprojectfinal.core.dto.auth.OrderResponse
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
import io.ktor.http.contentType
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import java.io.File // Required for handling the file object
import com.example.damprojectfinal.core.api.TokenManager
class UserApiService(private val tokenManager: TokenManager) {

    private val BASE_URL = "http://10.0.2.2:3000"

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

    suspend fun uploadProfileImage(id: String, file: File, token: String): UserResponse {
        // Use the ID in the URL path, matching your NestJS controller structure
        val url = "$BASE_URL/users/$id/upload-profile-image"

        val response = client.submitFormWithBinaryData(
            url = url,
            formData = formData {
                // 'file' must match the key used in NestJS FileInterceptor('file', ...)
                append(
                    key = "file",
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
        return response.body()
    }

    suspend fun getUserOrders(userId: String): List<OrderResponse> {
        return client.get("$BASE_URL/users/$userId/orders") {
            addAuthHeader(this)
        }.body()
    }
}