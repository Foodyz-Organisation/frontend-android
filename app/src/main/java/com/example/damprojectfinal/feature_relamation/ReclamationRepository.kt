package com.example.foodyz_dam.ui.theme.screens.reclamation

import android.util.Log
import com.example.damprojectfinal.core.api.TokenManager

class ReclamationRepository(
    private val tokenManager: TokenManager
) {
    private val TAG = "ReclamationRepository"

    /**
     * ✅ Récupère toutes les réclamations
     */
    suspend fun getAllReclamations(): List<Reclamation> {
        val token = tokenManager.getAccessToken()
            ?: throw Exception("Token manquant")
        val api = ReclamationRetrofitClient.createClient(token)
        return api.getAllReclamations()
    }

    /**
     * ✅ Crée une nouvelle réclamation
     */
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

    /**
     * ✅ NOUVELLE MÉTHODE: Récupère les réclamations de MON restaurant
     * Utilise le token JWT pour identifier le restaurant
     * Appelle: GET /reclamation/restaurant/my-reclamations
     */
    suspend fun getMyRestaurantReclamations(): List<Reclamation> {
        Log.d(TAG, "========== MES RECLAMATIONS RESTAURANT ==========")

        val token = tokenManager.getAccessToken()

        if (token.isNullOrEmpty()) {
            Log.e(TAG, "❌ Token manquant")
            throw Exception("❌ Token manquant")
        }

        Log.d(TAG, "Token: ${token.take(30)}...")

        val api = ReclamationRetrofitClient.createClient(token)

        return try {
            val result = api.getMyRestaurantReclamations()
            Log.d(TAG, "✅ Réclamations trouvées: ${result.size}")
            result.forEachIndexed { index, rec ->
                Log.d(TAG, "  ${index + 1}. ${rec.nomClient}: ${rec.description}")
            }
            result
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erreur getMyRestaurantReclamations: ${e.message}", e)
            throw e
        }
    }

    /**
     * ✅ Récupère les réclamations pour un restaurant spécifique par ID
     * Appelle: GET /reclamation/restaurant/:restaurantId
     */
    suspend fun getReclamationsByRestaurant(restaurantId: String): List<Reclamation> {
        Log.d(TAG, "========== RECLAMATIONS RESTAURANT PAR ID ==========")
        Log.d(TAG, "Restaurant ID: $restaurantId")

        val token = tokenManager.getAccessToken()

        if (token.isNullOrEmpty()) {
            Log.e(TAG, "❌ Token manquant")
            throw Exception("❌ Token manquant")
        }

        Log.d(TAG, "Token: ${token.take(30)}...")

        val api = ReclamationRetrofitClient.createClient(token)

        return try {
            val result = api.getReclamationsByRestaurant(restaurantId)
            Log.d(TAG, "✅ Réclamations trouvées: ${result.size}")
            result.forEachIndexed { index, rec ->
                Log.d(TAG, "  ${index + 1}. ${rec.nomClient}: ${rec.description}")
            }
            result
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erreur getReclamationsByRestaurant: ${e.message}", e)
            throw e
        }
    }

    /**
     * ✅ Répond à une réclamation
     */
    suspend fun respondToReclamation(
        id: String,
        request: RespondReclamationRequest
    ): Reclamation {
        val token = tokenManager.getAccessToken()
            ?: throw Exception("Token manquant")
        val api = ReclamationRetrofitClient.createClient(token)

        Log.d(TAG, "📝 Réponse à la réclamation $id: ${request.responseMessage}")

        return try {
            val result = api.respondToReclamation(id, request)
            Log.d(TAG, "✅ Réponse envoyée avec succès")
            result
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erreur respondToReclamation: ${e.message}", e)
            throw e
        }
    }
}