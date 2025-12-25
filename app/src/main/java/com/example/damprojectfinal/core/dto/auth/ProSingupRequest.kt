package com.example.damprojectfinal.core.dto.auth


import kotlinx.serialization.Serializable
@OptIn(kotlinx.serialization.InternalSerializationApi::class)
@Serializable
data class LocationDto(
    val name: String? = null,
    val address: String? = null,
    val lat: Double,
    val lon: Double
)

@OptIn(kotlinx.serialization.InternalSerializationApi::class)
@Serializable
data class ProfessionalSignupRequest(
    val email: String,
    val password: String,
    val fullName: String,
    val licenseImage: String? = null, // Base64 encoded image for OCR validation
    val linkedUserId: String? = null, // optional link to normal user
    val locations: List<LocationDto>? = null // Optional locations array
)


@OptIn(kotlinx.serialization.InternalSerializationApi::class)
@Serializable
data class ProfessionalSignupResponse(
    val message: String? = null,
    val professionalId: String? = null,
    val permitNumber: String? = null, // Extracted permit number from OCR
    val confidence: String? = null, // OCR confidence level (high, medium, low)
    val id: String? = null,
    val role: String? = null,
    val token: String? = null
)