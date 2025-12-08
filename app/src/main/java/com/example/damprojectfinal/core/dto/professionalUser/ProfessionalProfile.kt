package com.example.damprojectfinal.core.dto.professionalUser

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfessionalUserAccount(
    @SerialName("_id") val _id: String,
    val email: String,
    val fullName: String? = null,
    val licenseNumber: String? = null,
    val documents: List<String> = emptyList(), // Array of document paths
    val role: String, // Will be "professional"
    val isActive: Boolean,
    val linkedUserId: String? = null,
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
