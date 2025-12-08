package com.example.damprojectfinal.core.dto.auth


import kotlinx.serialization.Serializable
@OptIn(kotlinx.serialization.InternalSerializationApi::class)
@Serializable
data class ProfessionalSignupRequest(
    val email: String,
    val password: String,
    val fullName: String,
    val licenseNumber: String? = null,
    val linkedUserId: String? = null // optional link to normal user
)


@OptIn(kotlinx.serialization.InternalSerializationApi::class)
@Serializable
data class ProfessionalSignupResponse(
    // 🔑 FIX: Make the missing fields nullable (String?)
    val id: String? = null,
    val role: String? = null,
    val token: String? = null
)