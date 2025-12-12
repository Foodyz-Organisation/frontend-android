package com.example.damprojectfinal.core.dto.posts

import com.google.gson.annotations.SerializedName

/**
 * FoodType enum for posts
 * These are the exact string values that must be sent to the API
 */
enum class FoodType(val value: String) {
    @SerializedName("Spicy")
    SPICY("Spicy"),

    @SerializedName("Healthy")
    HEALTHY("Healthy"),

    @SerializedName("Mashwi")
    MASHWI("Mashwi"),

    @SerializedName("Couscous")
    COUSCOUS("Couscous"),

    @SerializedName("Street food")
    STREET_FOOD("Street food"),

    @SerializedName("Fast food")
    FAST_FOOD("Fast food"),

    @SerializedName("Seafood")
    SEAFOOD("Seafood"),

    @SerializedName("Fried")
    FRIED("Fried"),

    @SerializedName("Desserts")
    DESSERTS("Desserts"),

    @SerializedName("Vegetarian-Friendly")
    VEGETARIAN_FRIENDLY("Vegetarian-Friendly"),

    @SerializedName("Meat")
    MEAT("Meat");

    companion object {
        /**
         * Get all food type values as a list of strings
         */
        fun getAllValues(): List<String> = values().map { it.value }

        /**
         * Find FoodType by string value (case-sensitive)
         */
        fun fromValue(value: String?): FoodType? {
            return values().find { it.value == value }
        }
    }
}

