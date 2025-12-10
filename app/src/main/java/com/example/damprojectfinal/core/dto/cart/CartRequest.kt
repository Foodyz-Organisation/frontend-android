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
    @SerializedName("calculatedPrice") val calculatedPrice: Double // Price per unit (base + options)
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