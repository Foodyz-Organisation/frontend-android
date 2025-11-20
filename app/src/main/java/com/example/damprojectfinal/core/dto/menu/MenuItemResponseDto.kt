package com.example.damprojectfinal.core.dto.menu

import com.google.gson.annotations.SerializedName

// This DTO models the complete JSON object returned by the server on success
data class MenuItemResponseDto(
    // Fields from your request payload
    @SerializedName("professionalId")
    val professionalId: String,

    @SerializedName("name")
    val name: String,
    // ... include all other fields like description, price, category, etc.

    @SerializedName("ingredients")
    val ingredients: List<IngredientDto>,

    @SerializedName("options")
    val options: List<OptionDto>,

    // Fields added by the server (NestJS/MongoDB)
    @SerializedName("image")
    val image: String, // Now required, as the server adds the path

    @SerializedName("_id")
    val id: String,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("updatedAt")
    val updatedAt: String
)