package com.example.damprojectfinal.core.dto.menu

import com.google.gson.annotations.SerializedName

// Assuming Category, IngredientDto, and OptionDto are defined and imported
data class MenuItemResponseDto(
    @SerializedName("professionalId")
    val professionalId: String,

    @SerializedName("name")
    val name: String,

    // ⭐️ ADDED: These fields resolve the UI errors ⭐️
    @SerializedName("description")
    val description: String? = null, // Set to null default since it's optional on the server

    @SerializedName("price")
    val price: Double,
    
    // 🎯 NEW: Deal-related fields
    @SerializedName("discountedPrice")
    val discountedPrice: Double? = null, // Price after discount (if deal is active)
    
    @SerializedName("activeDealId")
    val activeDealId: String? = null, // Reference to the active deal
    
    @SerializedName("discountPercentage")
    val discountPercentage: Int? = null, // Current discount percentage (0-100)

    @SerializedName("category")
    val category: Category,
    // ----------------------------------------------------

    @SerializedName("ingredients")
    val ingredients: List<IngredientDto>,

    @SerializedName("options")
    val options: List<OptionDto>,

    @SerializedName("image")
    val image: String,

    @SerializedName("_id")
    val id: String,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("updatedAt")
    val updatedAt: String
)