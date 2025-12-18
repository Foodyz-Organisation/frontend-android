package com.example.damprojectfinal.core.dto.reclamation


import com.google.gson.annotations.SerializedName

/**
 * DTO pour le solde des points de fidélité
 *
 * Doit refléter la réponse de l'API /reclamation/user/loyalty
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
    val reliabilityScore: Int,

    // Historique des transactions de points (peut être omis ou null côté backend)
    @SerializedName("history")
    val history: List<LoyaltyHistoryEntry>? = null,

    // Récompenses disponibles (peut être omis ou null côté backend)
    @SerializedName("availableRewards")
    val availableRewards: List<LoyaltyRewardEntry>? = null
)

/**
 * Entrée d'historique renvoyée par le backend.
 */
data class LoyaltyHistoryEntry(
    @SerializedName("points")
    val points: Int,

    @SerializedName("reason")
    val reason: String,

    @SerializedName("date")
    val date: String,

    @SerializedName("reclamationId")
    val reclamationId: String
)

/**
 * Récompense renvoyée par le backend.
 */
data class LoyaltyRewardEntry(
    @SerializedName("name")
    val name: String,

    @SerializedName("pointsCost")
    val pointsCost: Int,

    @SerializedName("available")
    val available: Boolean
)
