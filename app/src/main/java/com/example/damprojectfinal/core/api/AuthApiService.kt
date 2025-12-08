package com.example.damprojectfinal.core.api

import com.example.damprojectfinal.core.dto.auth.LoginRequest
import com.example.damprojectfinal.core.dto.auth.LoginResponse
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
import io.ktor.client.plugins.HttpResponseValidator


class AuthApiService {

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