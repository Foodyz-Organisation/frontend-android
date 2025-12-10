package com.example.damprojectfinal.core.dto.cart

import com.google.gson.annotations.SerializedName

// Note: IngredientDto and OptionDto are defined in CartRequest.kt (same package)

data class CartResponse(
    @SerializedName("_id") val id: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("items") val items: List<CartItemResponse>,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("updatedAt") val updatedAt: String?
)

data class CartItemResponse(
    @SerializedName("menuItemId") val menuItemId: String,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("name") val name: String,
    @SerializedName("image") val image: String?,  // Add image field
    @SerializedName("chosenIngredients") val chosenIngredients: List<IngredientDto>, // Uses IngredientDto from CartRequest.kt
    @SerializedName("chosenOptions") val chosenOptions: List<OptionDto>, // Uses OptionDto from CartRequest.kt
    @SerializedName("calculatedPrice") val calculatedPrice: Double
)