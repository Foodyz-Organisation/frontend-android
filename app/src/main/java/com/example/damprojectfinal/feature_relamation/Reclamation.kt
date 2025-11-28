package com.example.foodyz_dam.ui.theme.screens.reclamation

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.*

/**
 * ✅ Modèle de données pour une réclamation (CORRIGÉ)
 */
data class Reclamation(
    @SerializedName("_id")
    val id: String,

    @SerializedName("commandeConcernee")
    val orderNumber: String?,

    @SerializedName("complaintType")
    val complaintType: String?,

    @SerializedName("description")
    val description: String?,

    @SerializedName("photos")
    val photos: List<String>? = null,

    // ✅ Statut brut du backend
    @SerializedName("statut")
    private val statutRaw: String?,

    @SerializedName("nomClient")
    val nomClient: String?,

    @SerializedName("emailClient")
    val emailClient: String?,

    @SerializedName("userId")
    val userId: String?,

    @SerializedName("restaurantEmail")
    val restaurantEmail: String?,

    @SerializedName("restaurantId")
    val restaurantId: String?,

    @SerializedName("responseMessage")
    val responseMessage: String? = null,

    @SerializedName("respondedBy")
    val respondedBy: String? = null,

    @SerializedName("respondedAt")
    val respondedAt: String? = null,

    @SerializedName("createdAt")
    val createdAt: String?,

    @SerializedName("updatedAt")
    val updatedAt: String?
) {
    /**
     * ✅ Statut converti en enum
     */
    val status: ReclamationStatus
        get() = when (statutRaw?.lowercase()) {
            "resolue", "resolved" -> ReclamationStatus.RESOLVED
            "en_cours", "in_progress" -> ReclamationStatus.IN_PROGRESS
            "rejetee", "rejected" -> ReclamationStatus.REJECTED
            else -> ReclamationStatus.PENDING
        }

    /**
     * ✅ Date parsée (format MongoDB ISO 8601)
     */
    val date: Date?
        get() = try {
            createdAt?.let {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }.parse(it)
            }
        } catch (e: Exception) {
            null
        }

    /**
     * ✅ Alias pour compatibilité
     */
    val response: String?
        get() = responseMessage
}

/**
 * ✅ Statut d'une réclamation
 */
enum class ReclamationStatus {
    PENDING,      // en_attente
    IN_PROGRESS,  // en_cours
    RESOLVED,     // resolue
    REJECTED      // rejetee
}

/**
 * ✅ Request pour répondre à une réclamation
 */
data class RespondReclamationRequest(
    val responseMessage: String,
    val newStatus: String? = "resolue"
)