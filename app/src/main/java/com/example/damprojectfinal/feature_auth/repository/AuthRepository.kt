package com.example.damprojectfinal.feature_auth.repository

import com.example.damprojectfinal.core.api.AuthApiService
import com.example.damprojectfinal.core.dto.auth.*
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val apiService: AuthApiService
) {

    // ========== Existing methods ==========
    suspend fun userSignup(request: UserSignupRequest): SimpleMessageResponse {
        return apiService.userSignup(request)
    }

    suspend fun professionalSignup(request: ProfessionalSignupRequest): ProfessionalSignupResponse {
        return apiService.professionalSignup(request)
    }

    suspend fun login(request: LoginRequest): LoginResponse {
        return apiService.login(request)
    }

    // ========== NEW OTP methods ==========
    suspend fun sendOtp(email: String): Result<OtpResponse> {
        return try {
            val response = apiService.sendOtp(SendOtpRequest(email))
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyOtp(email: String, otp: String): Result<VerifyOtpResponse> {
        return try {
            val response = apiService.verifyOtp(VerifyOtpRequest(email, otp))
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetPasswordWithOtp(
        email: String,
        resetToken: String,
        newPassword: String
    ): Result<ResetPasswordResponse> {
        return try {
            val response = apiService.resetPasswordWithOtp(
                ResetPasswordWithOtpRequest(email, resetToken, newPassword)
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}