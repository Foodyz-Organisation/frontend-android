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
    @SerializedName("menuItemId") val menuItemId: String,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("name") val name: String,
    @SerializedName("image") val image: String?,  // Add image field
    @SerializedName("chosenIngredients") val chosenIngredients: List<IngredientDto>,
    @SerializedName("chosenOptions") val chosenOptions: List<OptionDto>,
    @SerializedName("calculatedPrice") val calculatedPrice: Double
)