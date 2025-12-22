package com.example.damprojectfinal.core.dto.cart

import com.example.damprojectfinal.core.dto.menu.IntensityType
import com.google.gson.annotations.SerializedName

// 1. Updated AddToCartRequest
data class AddToCartRequest(
    @SerializedName("menuItemId") val menuItemId: String,
    @SerializedName("name") val name: String,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("chosenIngredients") val chosenIngredients: List<IngredientDto>,
    @SerializedName("chosenOptions") val chosenOptions: List<OptionDto>,
    @SerializedName("calculatedPrice") val calculatedPrice: Double, // Price per unit (discounted if deal is active)
    
    // 🎯 NEW: Deal-related fields
    @SerializedName("originalPrice") val originalPrice: Double? = null, // Original price before discount
    @SerializedName("discountPercentage") val discountPercentage: Int? = null, // Discount percentage (0-100)
    @SerializedName("dealId") val dealId: String? = null // Deal ID if applicable
)

// 2. UpdateQuantityRequest: No change, remains the same.
data class UpdateQuantityRequest(
    @SerializedName("quantity") val quantity: Int
)

// 3. IngredientDto: Updated to include intensity information
data class IngredientDto(
    @SerializedName("name") val name: String,
    @SerializedName("isDefault") val isDefault: Boolean,
    @SerializedName("intensityType") val intensityType: IntensityType? = null,
    @SerializedName("intensityColor") val intensityColor: String? = null,
    @SerializedName("intensityValue") val intensityValue: Double? = null // Changed to Double for JSON parsing (0.0 to 1.0)
)

// 4. OptionDto: No change, remains the same.
data class OptionDto(
    @SerializedName("name") val name: String,
    @SerializedName("price") val price: Double
)