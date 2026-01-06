package com.example.damprojectfinal.core.api

import com.example.damprojectfinal.core.dto.auth.LoginRequest
import com.example.damprojectfinal.core.dto.auth.LoginResponse
import com.example.damprojectfinal.core.dto.auth.GoogleLoginRequest
import com.example.damprojectfinal.core.dto.auth.OtpResponse
import com.example.damprojectfinal.core.dto.auth.ProfessionalSignupRequest
import com.example.damprojectfinal.core.dto.auth.ProfessionalSignupResponse
import com.example.damprojectfinal.core.dto.auth.ResetPasswordResponse
import com.example.damprojectfinal.core.dto.auth.ResetPasswordWithOtpRequest
import com.example.damprojectfinal.core.dto.auth.SendOtpRequest
import com.example.damprojectfinal.core.dto.auth.SimpleMessageResponse
import com.example.damprojectfinal.core.dto.auth.UserSignupRequest
import com.example.damprojectfinal.core.dto.auth.VerifyOtpRequest
import com.example.damprojectfinal.core.dto.auth.VerifyOtpResponse
import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.timeout
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import io.ktor.client.plugins.HttpResponseValidator
import kotlinx.serialization.Serializable

// Error response DTO
@Serializable
data class ErrorResponse(
    val statusCode: Int? = null,
    val message: String? = null,
    val reason: String? = null,
    val error: String? = null
)

class AuthApiService {

    private val TAG = "AuthApiService"
    
    // Use centralized BaseUrlProvider instead of hardcoded URL
    private val BASE_URL = BaseUrlProvider.BASE_URL

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
        // Don't throw exceptions for non-2xx responses
        HttpResponseValidator {
            handleResponseExceptionWithRequest { cause, _ ->
                throw cause
            }
        }
    }

    // ========== Existing endpoints ==========
    suspend fun userSignup(request: UserSignupRequest): SimpleMessageResponse {
        val url = "$BASE_URL/auth/signup/user"
        val response: HttpResponse = client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        
        if (!response.status.isSuccess()) {
            val errorBody = response.bodyAsText()
            Log.e(TAG, "❌ User signup failed with status ${response.status.value}")
            Log.e(TAG, "❌ Error body: $errorBody")
            
            try {
                val json = Json { ignoreUnknownKeys = true; isLenient = true }
                val jsonObj = json.parseToJsonElement(errorBody).jsonObject
                
                val messageElement = jsonObj["message"]
                val errorMsg = when {
                    messageElement is JsonArray -> {
                        messageElement[0].jsonPrimitive.content
                    }
                    messageElement is kotlinx.serialization.json.JsonPrimitive -> {
                        messageElement.content
                    }
                    else -> jsonObj["reason"]?.jsonPrimitive?.content 
                        ?: jsonObj["error"]?.jsonPrimitive?.content 
                        ?: "Signup failed"
                }
                
                Log.e(TAG, "📝 Parsed error message: $errorMsg")
                throw Exception(errorMsg)
            } catch (e: Exception) {
                if (e is Exception && e.message?.contains("Parsed error message") == false) {
                    throw e
                }
                Log.e(TAG, "⚠️ Failed to parse error JSON or extract message: ${e.message}")
                throw Exception("Registration failed: ${response.status.description}")
            }
        }
        
        return response.body()
    }

    suspend fun professionalSignup(request: ProfessionalSignupRequest): ProfessionalSignupResponse {
        val url = "$BASE_URL/auth/signup/professional"
        Log.d(TAG, "🔄 Professional signup request to: $url")
        Log.d(TAG, "📧 Email: ${request.email}, Has image: ${request.licenseImage != null}")
        
        val response: HttpResponse = client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(request)
            // Increase timeout for OCR processing (30 seconds)
            timeout {
                requestTimeoutMillis = 30000
            }
        }
        
        Log.d(TAG, "📡 Response status: ${response.status.value} ${response.status.description}")
        
        // Check if response is successful
        if (!response.status.isSuccess()) {
            val errorBody = response.bodyAsText()
            Log.e(TAG, "❌ Professional signup failed with status ${response.status.value}")
            Log.e(TAG, "❌ Error body: $errorBody")
            
            // Try to parse error response
            try {
                val json = Json { ignoreUnknownKeys = true; isLenient = true }
                val jsonObj = json.parseToJsonElement(errorBody).jsonObject
                
                // Get message which could be a String or Array
                val messageElement = jsonObj["message"]
                val errorMsg = when {
                    messageElement is kotlinx.serialization.json.JsonArray -> {
                        messageElement[0].jsonPrimitive.content
                    }
                    messageElement is kotlinx.serialization.json.JsonPrimitive -> {
                        messageElement.content
                    }
                    else -> jsonObj["reason"]?.jsonPrimitive?.content 
                        ?: jsonObj["error"]?.jsonPrimitive?.content 
                        ?: "Signup failed"
                }
                
                Log.e(TAG, "📝 Parsed error message: $errorMsg")
                throw Exception(errorMsg)
            } catch (e: Exception) {
                Log.e(TAG, "⚠️ Failed to parse error JSON or extract message: ${e.message}")
                throw Exception("Signup failed: ${response.status.description}")
            }
        }
        
        val successResponse = response.body<ProfessionalSignupResponse>()
        Log.d(TAG, "✅ Professional signup successful! Permit: ${successResponse.permitNumber}")
        return successResponse
    }

    suspend fun login(request: LoginRequest): LoginResponse {
        val url = "$BASE_URL/auth/login"
        return client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    /**
     * Google Sign-In authentication endpoint
     * Sends Google ID token to backend for verification
     */
    suspend fun loginWithGoogle(request: GoogleLoginRequest): LoginResponse {
        val url = "$BASE_URL/auth/google"
        return client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun logout(): SimpleMessageResponse {
        val url = "$BASE_URL/auth/logout"
        val response = client.post(url) {
            contentType(ContentType.Application.Json)
        }
        return response.body()
    }

    // ========== NEW OTP endpoints ==========
    suspend fun sendOtp(request: SendOtpRequest): OtpResponse {
        val url = "$BASE_URL/auth/forgot-password"
        val response = client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return response.body()
    }

    suspend fun verifyOtp(request: VerifyOtpRequest): VerifyOtpResponse {
        val url = "$BASE_URL/auth/verify-otp"
        val response = client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return response.body()
    }

    suspend fun resetPasswordWithOtp(request: ResetPasswordWithOtpRequest): ResetPasswordResponse {
        val url = "$BASE_URL/auth/reset-password"
        val response = client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return response.body()
    }
}