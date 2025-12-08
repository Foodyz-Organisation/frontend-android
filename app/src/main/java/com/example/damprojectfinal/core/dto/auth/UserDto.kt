package com.example.damprojectfinal.core.dto.auth


import kotlinx.serialization.Serializable

@Serializable
data class UserInfoResponse(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val phone: String? = null,
    val address: String? = null
)

@Serializable
data class OrderResponse(
    val id: String,
    val orderNumber: String,
    val status: String,
    val totalAmount: Double,
    val createdAt: String,
    val restaurantName: String? = null
)