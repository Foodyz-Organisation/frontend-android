package com.example.damprojectfinal.core.dto.deals

// Data class pour représenter un Deal
data class Deal(
    val _id: String = "",
    val restaurantName: String = "",
    val description: String = "",
    val image: String = "",
    val category: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val isActive: Boolean = true,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

// DTO pour créer un deal
data class CreateDealDto(
    val restaurantName: String,
    val description: String,
    val image: String,
    val category: String,
    val startDate: String,
    val endDate: String
)

// DTO pour mettre à jour un deal
data class UpdateDealDto(
    val restaurantName: String? = null,
    val description: String? = null,
    val image: String? = null,
    val category: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val isActive: Boolean? = null
)

// Réponse API générique
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null
)