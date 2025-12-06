package com.example.damprojectfinal.core.dto.cart

import com.google.gson.annotations.SerializedName

// 1. Updated AddToCartRequest: NOW INCLUDES THE 'name'
data class AddToCartRequest(
    @SerializedName("menuItemId") val menuItemId: String,
    // ⭐ NEW FIELD: The item's human-readable name, needed for the Cart UI and DB record.
    @SerializedName("name") val name: String,
    @SerializedName("image") val image: String?, // ⭐ NEW FIELD: Image path
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("chosenIngredients") val chosenIngredients: List<IngredientDto>,
    @SerializedName("chosenOptions") val chosenOptions: List<OptionDto>,
    @SerializedName("calculatedPrice") val calculatedPrice: Double // Price per unit (base + options)
)

// 2. UpdateQuantityRequest: No change, remains the same.
data class UpdateQuantityRequest(
    @SerializedName("quantity") val quantity: Int
)

// 3. IngredientDto: No change, remains the same.
data class IngredientDto(
    @SerializedName("name") val name: String,
    @SerializedName("isDefault") val isDefault: Boolean
)

// 4. OptionDto: No change, remains the same.
data class OptionDto(
    @SerializedName("name") val name: String,
    @SerializedName("price") val price: Double
)