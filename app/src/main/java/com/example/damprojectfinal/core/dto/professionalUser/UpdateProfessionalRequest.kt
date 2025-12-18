package com.example.damprojectfinal.core.dto.professionalUser

import kotlinx.serialization.Serializable

@Serializable
data class UpdateProfessionalRequest(
    val phone: String? = null,
    val hours: String? = null,
    val address: String? = null,
    val description: String? = null,
    val profilePictureUrl: String? = null,
    val imageUrl: String? = null,
    val locations: List<ProfessionalLocation>? = null
)

