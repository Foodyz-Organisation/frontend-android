package com.example.foodyz_dam.ui.theme.screens.reclamation

import android.util.Log
import com.example.damprojectfinal.core.api.TokenManager

class ReclamationRepository(
    private val tokenManager: TokenManager
) {
    private val TAG = "ReclamationRepository"

    suspend fun getAllReclamations(): List<Reclamation> {
        val token = tokenManager.getAccessToken() ?: throw Exception("Token manquant")
        val api = ReclamationRetrofitClient.createClient(token)
        return api.getAllReclamations()
    }

    suspend fun createReclamation(request: CreateReclamationRequest): Reclamation {
        val token = tokenManager.getAccessToken()

        Log.d(TAG, "========== DEBUT CREATION RECLAMATION ==========")
        Log.d(TAG, "Token: ${token?.take(30)}...")
        Log.d(TAG, "Request: $request")

        if (token.isNullOrEmpty()) {
            throw Exception("❌ Token manquant")
        }

        val api = ReclamationRetrofitClient.createClient(token)

        return try {
            val result = api.createReclamation(request)
            Log.d(TAG, "✅ Succès: $result")
            result
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erreur: ${e.message}", e)
            throw e
        }
    }
}