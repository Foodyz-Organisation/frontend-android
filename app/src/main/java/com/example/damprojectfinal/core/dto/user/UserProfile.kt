package com.example.damprojectfinal.core.dto.user

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName // <--- Import this


@Serializable
data class UpdateUserRequest(
    val username: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val email: String? = null,      // <- added
    val password: String? = null,
    val isActive: Boolean? = null,
    val profilePictureUrl: String? = null
)


@OptIn(kotlinx.serialization.InternalSerializationApi::class)
@Serializable
data class UserResponse(
    @SerialName("_id")
    val id: String,
    val username: String,
    val email: String,
    val phone: String?,
    val address: String?,
    val role : String,
    val isActive: Boolean,
    val profilePictureUrl: String?
)