package com.example.damprojectfinal.core.dto.auth

import kotlinx.serialization.Serializable


@OptIn(kotlinx.serialization.InternalSerializationApi::class)
@Serializable
data class UserSignupRequest(
    val username: String,
    val email: String,
    val password: String,
    // Optional fields must be nullable and have a default value of null
    val phone: String? = null,
    val address: String? = null
)

@OptIn(kotlinx.serialization.InternalSerializationApi::class)
@Serializable
data class SimpleMessageResponse(
    val message: String
)