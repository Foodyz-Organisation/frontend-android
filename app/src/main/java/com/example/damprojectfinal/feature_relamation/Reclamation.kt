package com.example.foodyz_dam.ui.theme.screens.reclamation


import java.util.*

/**
 * Modèle de données pour une réclamation
 */
data class Reclamation(
    val id: String,
    val orderNumber: String?,
    val complaintType: String?,
    val description: String?,
    val photos: List<String>?,
    val status: ReclamationStatus,
    val date: Date?,
    val response: String? = null
)

/**
 * Statut d'une réclamation
 */
enum class ReclamationStatus {
    PENDING,    // En attente
    RESOLVED,   // Résolue
    REJECTED    // Rejetée
}