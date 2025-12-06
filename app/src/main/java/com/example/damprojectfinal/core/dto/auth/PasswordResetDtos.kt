package com.example.damprojectfinal.core.dto.auth


import kotlinx.serialization.Serializable

@Serializable
data class ForgotPasswordRequest(
    val email: String
)

@Serializable
data class ForgotPasswordResponse(
    val message: String,
    val success: Boolean
)

@Serializable
data class ResetPasswordRequest(
    val token: String,
    val newPassword: String
)

