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
    val access_token: String? = null,
    val refresh_token: String? = null,
    val role: String? = null,
    val email: String? = null,
    val id: String? = null
)
