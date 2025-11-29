package com.example.damprojectfinal.core.dto.professional

import kotlinx.serialization.Serializable

@OptIn(kotlinx.serialization.InternalSerializationApi::class)
data class UpdateUserDto(
    val username: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val password: String? = null,
    val isActive: Boolean? = null
)


@OptIn(kotlinx.serialization.InternalSerializationApi::class)
@Serializable
data class UserUpdateResponse(
    val id: String,
    val email: String,
    val username: String,
    val phone: String?,
    val address: String?,
    val isActive: Boolean,
    val role: String,
    val updatedAt: String // Optional timestamp confirmation
)