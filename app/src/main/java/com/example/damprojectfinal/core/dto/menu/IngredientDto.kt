package com.example.damprojectfinal.core.dto.menu

import com.google.gson.annotations.SerializedName

enum class IntensityType {
    COFFEE,
    HARISSA,
    SAUCE,
    SPICE,
    SUGAR,
    SALT,
    PEPPER,
    CHILI,
    GARLIC,
    LEMON,
    CUSTOM
}

data class IngredientDto(
    @SerializedName("name")
    val name: String,

    @SerializedName("isDefault")
    val isDefault: Boolean? = null,

    @SerializedName("supportsIntensity")
    val supportsIntensity: Boolean? = null, // NEW: optional slider flag

    @SerializedName("intensityType")
    val intensityType: IntensityType? = null, // NEW: intensity type from backend

    @SerializedName("intensityColor")
    val intensityColor: String? = null // NEW: color hex string from backend
)