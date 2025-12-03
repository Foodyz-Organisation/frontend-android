package com.example.damprojectfinal.core.dto.auth

import kotlinx.serialization.Serializable


@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

// Response for successful login
@Serializable
data class LoginResponse(
    val access_token: String,
    val refresh_token: String,
    val role: String,
    val email: String,
    val id: String,
    val username: String? = null,
    val avatarUrl: String? = null,
)