package com.example.damprojectfinal.core.dto.professionalUser

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfessionalServices(
    val delivery: Boolean = true,
    val takeaway: Boolean = true,
    val dineIn: Boolean = true
)

@Serializable
data class ProfessionalLocation(
    val name: String? = null,
    val address: String? = null,
    val lat: Double,
    val lon: Double
)

@Serializable
data class ProfessionalUserAccount(
    @SerialName("_id") val _id: String,
    val email: String,
    val fullName: String? = null,
    val licenseNumber: String? = null,
    val description: String? = null, // Bio/description
    val address: String? = null,
    val phone: String? = null,
    val hours: String? = null, // Operating hours
    val services: ProfessionalServices? = null,
    val imageUrl: String? = null, // Background image
    val profilePictureUrl: String? = null,
    val documents: List<String> = emptyList(),
    val role: String = "professional",
    val isActive: Boolean = true,
    val linkedUserId: String? = null,
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val locations: List<ProfessionalLocation> = emptyList(),
    val createdAt: String? = null,
    val updatedAt: String? = null
)
