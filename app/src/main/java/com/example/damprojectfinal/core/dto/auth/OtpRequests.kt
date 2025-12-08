package com.example.damprojectfinal.core.dto.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SendOtpRequest(
    @SerialName("email")
    val email: String
)

@Serializable
data class VerifyOtpRequest(
    @SerialName("email")
    val email: String,
    @SerialName("otp")
    val otp: String
)

@Serializable
data class ResetPasswordWithOtpRequest(
    @SerialName("email")
    val email: String,
    @SerialName("resetToken")
    val resetToken: String,
    @SerialName("newPassword")
    val newPassword: String
)