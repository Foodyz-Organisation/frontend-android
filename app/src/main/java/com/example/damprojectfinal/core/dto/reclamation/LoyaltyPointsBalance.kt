package com.example.damprojectfinal.core.dto.reclamation


import com.google.gson.annotations.SerializedName

/**
 * DTO pour le solde des points de fidélité
 */
data class LoyaltyPointsBalance(
    @SerializedName("userId")
    val userId: String,

    @SerializedName("loyaltyPoints")
    val loyaltyPoints: Int,

    @SerializedName("validReclamations")
    val validReclamations: Int,

    @SerializedName("invalidReclamations")
    val invalidReclamations: Int,

    @SerializedName("reliabilityScore")
    val reliabilityScore: Int
)
