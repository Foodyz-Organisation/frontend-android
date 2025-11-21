package com.example.damprojectfinal.core.dto.menu

// Import the existing DTO for the values in the map
import com.example.damprojectfinal.core.dto.menu.MenuItemResponseDto

/**
 * Type alias for the grouped menu list response from the API.
 * The keys are the Category names (strings) and the values are lists of menu items.
 */
typealias GroupedMenuResponse = Map<String, List<MenuItemResponseDto>>