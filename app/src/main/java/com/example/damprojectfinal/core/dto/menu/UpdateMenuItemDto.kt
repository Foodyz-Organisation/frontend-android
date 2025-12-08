package com.example.damprojectfinal.core.dto.menu

import com.google.gson.annotations.SerializedName

/**
 * DTO used for updating an existing menu item.
 * All fields are optional since you only submit the data you want to change.
 */
data class UpdateMenuItemDto(
    @SerializedName("name")
    val name: String? = null,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("price")
    val price: Double? = null,

    @SerializedName("category")
    val category: Category? = null,

    // Note: To update arrays (ingredients/options), you must send the entire new list
    @SerializedName("ingredients")
    val ingredients: List<IngredientDto>? = null,

    @SerializedName("options")
    val options: List<OptionDto>? = null,

    // If you update the image without a multipart form, you could potentially send
    // a new image URL/path, but typically image updates require a new multipart request.
    @SerializedName("image")
    val image: String? = null
)