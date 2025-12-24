package com.example.damprojectfinal.core.dto.posts

import com.google.gson.annotations.SerializedName

/**
 * FoodType enum for posts
 * Updated to match backend Category enum - all values are UPPERCASE
 * These are the exact string values that must be sent to the API
 */
enum class FoodType(val value: String, val emoji: String, val displayName: String) {
    // Core Categories
    BURGER("BURGER", "🍔", "Burger"),
    PIZZA("PIZZA", "🍕", "Pizza"),
    PASTA("PASTA", "🍝", "Pasta"),
    MEXICAN("MEXICAN", "🌮", "Mexican"),
    SUSHI("SUSHI", "🍣", "Sushi"),
    ASIAN("ASIAN", "🍜", "Asian"),
    INDIAN("INDIAN", "🍛", "Indian"),
    MIDEAST("MIDEAST", "🥙", "Middle East"),
    SEAFOOD("SEAFOOD", "🦞", "Seafood"),
    CHICKEN("CHICKEN", "🍗", "Chicken"),
    SANDWICHES("SANDWICHES", "🥪", "Sandwiches"),
    SOUPS("SOUPS", "🍲", "Soups"),

    // Dietary and Flavor
    SALAD("SALAD", "🥗", "Salad"),
    VEGETARIAN("VEGETARIAN", "🌱", "Vegetarian"),
    VEGAN("VEGAN", "🌿", "Vegan"),
    HEALTHY("HEALTHY", "🥑", "Healthy"),
    GLUTEN_FREE("GLUTEN_FREE", "🌾", "Gluten Free"),
    SPICY("SPICY", "🌶️", "Spicy"),

    // Item Type and Occasion
    BREAKFAST("BREAKFAST", "🥐", "Breakfast"),
    DESSERT("DESSERT", "🍰", "Dessert"),
    DRINKS("DRINKS", "🥤", "Drinks"),
    KIDS_MENU("KIDS_MENU", "🍟", "Kids Menu"),
    FAMILY_MEAL("FAMILY_MEAL", "👨‍👩‍👧‍👦", "Family Meal");

    companion object {
        /**
         * Get all food type values as a list of strings
         */
        fun getAllValues(): List<String> = values().map { it.value }

        /**
         * Find FoodType by string value (case-insensitive, handles both old and new values)
         */
        fun fromValue(value: String?): FoodType? {
            if (value == null) return null
            
            // First try exact match (new uppercase values)
            values().find { it.value == value }?.let { return it }
            
            // Try case-insensitive match
            values().find { it.value.equals(value, ignoreCase = true) }?.let { return it }
            
            // Try matching by display name (for old values)
            values().find { it.displayName.equals(value, ignoreCase = true) }?.let { return it }
            
            // Legacy mapping for old values (backward compatibility)
            return when (value) {
                "Spicy" -> SPICY
                "Healthy" -> HEALTHY
                "Mashwi" -> MIDEAST
                "Couscous" -> MIDEAST
                "Street food" -> SANDWICHES
                "Fast food" -> BURGER
                "Seafood" -> SEAFOOD
                "Fried" -> CHICKEN
                "Desserts" -> DESSERT
                "Vegetarian-Friendly" -> VEGETARIAN
                "Meat" -> CHICKEN
                else -> null
            }
        }

        /**
         * Get FoodType for display in UI (for CategoryIconsRow)
         * Returns a subset of popular food types for the home screen
         */
        fun getPopularFoodTypes(): List<FoodType> {
            return listOf(
                BREAKFAST,
                HEALTHY,
                DESSERT,
                BURGER,
                PIZZA,
                SEAFOOD,
                SALAD,
                CHICKEN,
                PASTA,
                SUSHI
            )
        }
    }
}

