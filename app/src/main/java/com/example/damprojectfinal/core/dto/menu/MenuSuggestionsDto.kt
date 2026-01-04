package com.example.damprojectfinal.core.dto.menu

import com.google.gson.annotations.SerializedName

/**
 * Response DTO for AI-generated menu item suggestions
 * Endpoint: GET /menu-items/:id/suggestions
 */
data class MenuSuggestionsDto(
    @SerializedName("bestCombination")
    val bestCombination: SuggestionCombination,
    
    @SerializedName("popularChoice")
    val popularChoice: SuggestionCombination,
    
    @SerializedName("reasoning")
    val reasoning: String
)

/**
 * Represents a single suggestion combination with ingredients, options, and description
 */
data class SuggestionCombination(
    @SerializedName("ingredients")
    val ingredients: List<String>,
    
    @SerializedName("options")
    val options: List<String>,
    
    @SerializedName("description")
    val description: String
)
