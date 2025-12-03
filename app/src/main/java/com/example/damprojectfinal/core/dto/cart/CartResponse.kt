package com.example.damprojectfinal.core.dto.cart

import com.google.gson.annotations.SerializedName

data class CartResponse(
    @SerializedName("_id") val id: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("items") val items: List<CartItemResponse>,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("updatedAt") val updatedAt: String?
)

data class CartItemResponse(
    @SerializedName("menuItemId") val menuItemId: String, // ObjectId comes as String
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("name") val name: String, // <-- ADD THIS FIELD
    @SerializedName("chosenIngredients") val chosenIngredients: List<IngredientDto>,
    @SerializedName("chosenOptions") val chosenOptions: List<OptionDto>,
    @SerializedName("calculatedPrice") val calculatedPrice: Double
)