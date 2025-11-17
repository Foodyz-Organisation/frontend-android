package com.example.foodyz_dam.ui.theme.screens.reclamation

data class CreateReclamationRequest(
    val description: String,
    val commandeConcernee: String,
    val complaintType: String,
    val photos: List<String> = emptyList()
)