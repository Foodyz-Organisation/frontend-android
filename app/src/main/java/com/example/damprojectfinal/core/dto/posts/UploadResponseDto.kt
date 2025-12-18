package com.example.damprojectfinal.core.dto.posts

import kotlinx.serialization.Serializable

// Matches backend's UploadResponseDto
@Serializable
data class UploadResponse(
    val urls: List<String>
)