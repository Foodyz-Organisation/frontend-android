package com.example.foodyz_dam.ui.theme.screens.reclamation


//import com.example.foodyz_dam.network.ReclamationApi
import com.example.foodyz_dam.ui.theme.screens.reclamation.CreateReclamationRequest
import com.example.foodyz_dam.ui.theme.screens.reclamation.Reclamation

class ReclamationRepository(private val api: ReclamationApi) {

    suspend fun getAllReclamations(): List<Reclamation> {
        return api.getAllReclamations()
    }

    suspend fun createReclamation(request: CreateReclamationRequest): Reclamation {
        return api.createReclamation(request)
    }
}
