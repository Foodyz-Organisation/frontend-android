package com.example.damprojectfinal.core.dto.reclamation

data class CreateReclamationRequest(
    val description: String,
    val commandeConcernee: String,
    val complaintType: String,
    val photos: List<String> = emptyList()
)