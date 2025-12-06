package com.example.damprojectfinal.core.dto.professionalUser

data class ProfessionalProfile(
    val id: String,
    val name: String,
    val imageUrl: String? = null, // URL for the profile picture
    val rating: Double,
    val reviewCount: Int,
    val priceRange: String, // e.g., "$$", "$$$"
    val cuisine: String, // e.g., "Italian, Pizza, Pasta"
    val deliveryTime: String, // e.g., "30-45 min"
    val takeawayTime: String, // e.g., "Ready in 15 min"
    val dineInAvailable: Boolean,
    val address: String,
    val phoneNumber: String,
    val openingHours: String // e.g., "10:00 AM - 11:00 PM"
)

// You might also want a placeholder for loading profile data, e.g., a ViewModel state
data class ProfessionalProfileState(
    val profile: ProfessionalProfile? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
