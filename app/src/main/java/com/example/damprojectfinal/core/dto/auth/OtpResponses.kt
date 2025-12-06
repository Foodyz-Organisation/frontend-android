package com.example.damprojectfinal.core.dto.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OtpResponse(
    @SerialName("success")
    val success: Boolean,
    @SerialName("message")
    val message: String
)

@Serializable
data class VerifyOtpResponse(
    @SerialName("success")
    val success: Boolean,
    @SerialName("message")
    val message: String,
    @SerialName("resetToken")
    val resetToken: String? = null
)

@Serializable
data class ResetPasswordResponse(
    @SerialName("success")
    val success: Boolean,
    @SerialName("message")
    val message: String
)