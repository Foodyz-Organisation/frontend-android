package com.example.damprojectfinal.core.dto.menu

import com.google.gson.annotations.SerializedName

data class CreateMenuItemDto(
    @SerializedName("professionalId")
    val professionalId: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String? = null, // Optional String

    @SerializedName("price")
    val price: Double, // Matches NestJS Number

    @SerializedName("category")
    val category: Category, // Uses the Kotlin enum

    @SerializedName("ingredients")
    val ingredients: List<IngredientDto>, // Nested DTO list

    @SerializedName("options")
    val options: List<OptionDto>, // Nested DTO list

    // Preparation time in minutes (base time for this dish)
    @SerializedName("preparationTimeMinutes")
    val preparationTimeMinutes: Int = 15

    // NOTE: The 'image' field is NOT included here as it is sent as a separate MultipartBody.Part (the File)
    // The server handles attaching the path to the DTO after serialization.
)