package com.example.damprojectfinal.core.dto.auth


import kotlinx.serialization.Serializable

@Serializable
data class ProfessionalSignupRequest(
    val email: String,
    val password: String,
    // Collect the minimum required data for the professionalData object
    val fullName: String,
    val licenseNumber: String? = null // Optional for minimal screen
)

@Serializable
data class ProfessionalSignupResponse(
    // 🔑 FIX: Make the missing fields nullable (String?)
    val id: String? = null,
    val role: String? = null,

    // Add other expected fields here (like token, which is often returned)
    val token: String? = null
)