package com.example.foodyz_dam.ui.theme.screens.reclamation


data class CreateReclamationRequest(
    val nomClient: String,
    val emailClient: String,
    val description: String,
    val commandeConcernee: String,
    val complaintType: String,
    val image: String? = null
)
