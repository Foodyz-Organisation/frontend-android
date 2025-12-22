package com.example.damprojectfinal.core.dto.deals

import com.google.gson.annotations.SerializedName

// Data class pour représenter un Deal
data class Deal(
    @SerializedName("_id")
    val _id: String = "",
    
    @SerializedName("professionalId")
    val professionalId: String = "",
    
    @SerializedName("restaurantName")
    val restaurantName: String = "",
    
    @SerializedName("description")
    val description: String = "",
    
    @SerializedName("image")
    val image: String = "",
    
    @SerializedName("category")
    val category: String = "",
    
    @SerializedName("discountPercentage")
    val discountPercentage: Int = 0, // 0-100
    
    @SerializedName("applicableMenuItems")
    val applicableMenuItems: List<String> = emptyList(), // List of menu item IDs
    
    @SerializedName("applicableCategories")
    val applicableCategories: List<String> = emptyList(), // List of categories (e.g., ["PIZZA", "BURGER"])
    
    @SerializedName("startDate")
    val startDate: String = "",
    
    @SerializedName("endDate")
    val endDate: String = "",
    
    @SerializedName("isActive")
    val isActive: Boolean = true,
    
    @SerializedName("createdAt")
    val createdAt: String? = null,
    
    @SerializedName("updatedAt")
    val updatedAt: String? = null
)

// DTO pour créer un deal
data class CreateDealDto(
    @SerializedName("professionalId")
    val professionalId: String,
    
    @SerializedName("restaurantName")
    val restaurantName: String,
    
    @SerializedName("description")
    val description: String,
    
    @SerializedName("image")
    val image: String,
    
    @SerializedName("category")
    val category: String,
    
    @SerializedName("discountPercentage")
    val discountPercentage: Int, // 0-100
    
    @SerializedName("applicableMenuItems")
    val applicableMenuItems: List<String> = emptyList(),
    
    @SerializedName("applicableCategories")
    val applicableCategories: List<String> = emptyList(),
    
    @SerializedName("startDate")
    val startDate: String,
    
    @SerializedName("endDate")
    val endDate: String,
    
    @SerializedName("isActive")
    val isActive: Boolean = true
)

// DTO pour mettre à jour un deal
data class UpdateDealDto(
    @SerializedName("restaurantName")
    val restaurantName: String? = null,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("image")
    val image: String? = null,
    
    @SerializedName("category")
    val category: String? = null,
    
    @SerializedName("discountPercentage")
    val discountPercentage: Int? = null,
    
    @SerializedName("applicableMenuItems")
    val applicableMenuItems: List<String>? = null,
    
    @SerializedName("applicableCategories")
    val applicableCategories: List<String>? = null,
    
    @SerializedName("startDate")
    val startDate: String? = null,
    
    @SerializedName("endDate")
    val endDate: String? = null,
    
    @SerializedName("isActive")
    val isActive: Boolean? = null
)

// Réponse API générique
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null
)