package com.example.damprojectfinal.core.dto.professional

import kotlinx.serialization.Serializable

@Serializable
data class UpdateProfessionalRequest(
    val fullName: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val email: String? = null,
    val licenseNumber: String? = null,
    val isActive: Boolean? = null,
    val profilePictureUrl: String? = null,
    val fcmToken: String? = null
)
