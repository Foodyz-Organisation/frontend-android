package com.example.damprojectfinal.core.dto.menu

import com.google.gson.annotations.SerializedName

data class OptionDto(
    @SerializedName("name")
    val name: String,

    @SerializedName("price")
    val price: Double // Use Double for Kotlin equivalent of NestJS number
)