package com.example.damprojectfinal.core.dto.professionalUser

import com.google.gson.annotations.SerializedName

data class ProfessionalUserAccount(
    val _id: String,
    val email: String,
    val role: String, // Will be "professional"
    val isActive: Boolean,
    val professionalData: ProfessionalData, // Nested data class for professional details
    val followerCount: Int,
    val followingCount: Int,
    // Add other fields from your backend response as needed (e.g., createdAt, updatedAt)
)

data class ProfessionalData(
    val fullName: String,
    val licenseNumber: String,
    val documents: List<Document> // Assuming Document is another data class for file details
)

// You might need to define a Document data class if you plan to use it
data class Document(
    val filename: String,
    val path: String
)
