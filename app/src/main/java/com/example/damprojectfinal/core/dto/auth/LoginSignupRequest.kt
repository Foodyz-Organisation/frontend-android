package com.example.damprojectfinal.core.dto.auth

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable

@OptIn(InternalSerializationApi::class)
@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@OptIn(InternalSerializationApi::class)
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
