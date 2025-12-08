package com.example.damprojectfinal.core.dto.menu

import com.google.gson.annotations.SerializedName

data class IngredientDto(
    @SerializedName("name")
    val name: String,

    @SerializedName("isDefault")
    val isDefault: Boolean? = null // Optional Boolean
)