package com.example.damprojectfinal.core.dto.auth

import kotlinx.serialization.Serializable



@OptIn(kotlinx.serialization.InternalSerializationApi::class)
@Serializable
data class ProfessionalDocumentDto(
    val filename: String,
    val path: String,
    val verified: Boolean? = null,
    val ocrText: String? = null
)

@OptIn(kotlinx.serialization.InternalSerializationApi::class)
@Serializable
data class ProfessionalDto(
    val id: String,
    val email: String,
    val fullName: String? = null,
    val licenseNumber: String? = null,
    val documents: List<ProfessionalDocumentDto> = emptyList(),
    val role: String? = null,
    val isActive: Boolean = true,
    val linkedUserId: String? = null
)
