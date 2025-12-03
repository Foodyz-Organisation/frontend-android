package com.example.damprojectfinal.core.api

import com.example.damprojectfinal.core.dto.auth.*
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class AuthApiService {

    private val BASE_URL = "http://192.168.1.10:3000"

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

    // ========== Existing endpoints ==========
    suspend fun userSignup(request: UserSignupRequest): SimpleMessageResponse {
        val url = "$BASE_URL/auth/signup/user"
        val response = client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return response.body()
    }

    suspend fun professionalSignup(request: ProfessionalSignupRequest): ProfessionalSignupResponse {
        val url = "$BASE_URL/auth/signup/professional"
        val response = client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return response.body()
    }

    suspend fun login(request: LoginRequest): LoginResponse {
        val url = "$BASE_URL/auth/login"
        val response = client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(request)
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