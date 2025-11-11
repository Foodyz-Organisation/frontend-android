package com.example.damprojectfinal.core.dto.auth

import kotlinx.serialization.Serializable

// CORRECT Kotlin syntax: no semicolons inside the constructor
// Matches NestJS SignupDto
@Serializable
data class UserSignupRequest(
    val username: String,
    val email: String,
    val password: String,
    // Optional fields must be nullable and have a default value of null
    val phone: String? = null,
    val address: String? = null
)

@Serializable
data class SimpleMessageResponse(
    val message: String
)