package com.example.damprojectfinal.core.dto.auth

import kotlinx.serialization.SerialName
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
    @SerialName("_id") val id: String, // maps _id from backend
    val email: String,
    val fullName: String? = null,
    val licenseNumber: String? = null,
    val documents: List<ProfessionalDocumentDto> = emptyList(),
    val role: String? = null,
    val isActive: Boolean = true,
    val linkedUserId: String? = null
)
