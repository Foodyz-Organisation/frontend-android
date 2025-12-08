package com.example.damprojectfinal.feature_deals

import android.util.Log
import com.example.damprojectfinal.core.api.RetrofitInstance
import com.example.damprojectfinal.core.dto.deals.CreateDealDto
import com.example.damprojectfinal.core.dto.deals.Deal
import com.example.damprojectfinal.core.dto.deals.UpdateDealDto

// ✅ LE SEUL ET UNIQUE DealsRepository
class DealsRepository {
    private val api = RetrofitInstance.dealsApi

    companion object {
        private const val TAG = "DealsRepository"
    }

    suspend fun getAllDeals(): Result<List<Deal>> {
        return try {
            Log.d(TAG, "📡 ========================================")
            Log.d(TAG, "📡 Appel API getAllDeals()")
            Log.d(TAG, "📡 URL: http://192.168.137.208:3000/api/deals")

            val response = api.getAllDeals()

            Log.d(TAG, "📥 Réponse reçue:")
            Log.d(TAG, "   - Code: ${response.code()}")
            Log.d(TAG, "   - Success: ${response.isSuccessful}")
            Log.d(TAG, "   - Body: ${response.body()}")
            Log.d(TAG, "   - Headers: ${response.headers()}")

            if (response.isSuccessful && response.body() != null) {
                val deals = response.body()!!
                Log.d(TAG, "✅ ${deals.size} deals récupérés")
                deals.forEachIndexed { index, deal ->
                    Log.d(TAG, "   [$index] ID: ${deal._id}, Restaurant: ${deal.restaurantName}")
                }
                Log.d(TAG, "📡 ========================================")
                Result.success(deals)
            } else {
                val error = "Erreur HTTP: ${response.code()} - ${response.message()}"
                Log.e(TAG, "❌ $error")
                Log.e(TAG, "❌ Body error: ${response.errorBody()?.string()}")
                Log.d(TAG, "📡 ========================================")
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ ========================================")
            Log.e(TAG, "❌ EXCEPTION dans getAllDeals()")
            Log.e(TAG, "❌ Message: ${e.message}")
            Log.e(TAG, "❌ Type: ${e.javaClass.simpleName}")
            Log.e(TAG, "❌ Stack trace:", e)
            Log.d(TAG, "📡 ========================================")
            Result.failure(Exception("Erreur de connexion: ${e.message}"))
        }
    }

    suspend fun getDealById(id: String): Result<Deal> {
        return try {
            Log.d(TAG, "📡 Appel API getDealById($id)")
            val response = api.getDealById(id)

            if (response.isSuccessful && response.body() != null) {
                val deal = response.body()!!
                Log.d(TAG, "✅ Deal récupéré: ${deal.restaurantName}")
                Result.success(deal)
            } else {
                Log.e(TAG, "❌ Deal non trouvé: $id")
                Result.failure(Exception("Deal non trouvé"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception getDealById: ${e.message}", e)
            Result.failure(Exception("Erreur de connexion: ${e.message}"))
        }
    }

    suspend fun createDeal(createDealDto: CreateDealDto): Result<Deal> {
        return try {
            Log.d(TAG, "📡 Création deal: ${createDealDto.restaurantName}")
            val response = api.createDeal(createDealDto)

            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "✅ Deal créé avec succès")
                Result.success(response.body()!!)
            } else {
                val error = "Erreur création: ${response.code()} - ${response.message()}"
                Log.e(TAG, "❌ $error")
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception createDeal: ${e.message}", e)
            Result.failure(Exception("Erreur de connexion: ${e.message}"))
        }
    }

    suspend fun updateDeal(id: String, updateDealDto: UpdateDealDto): Result<Deal> {
        return try {
            Log.d(TAG, "📡 Mise à jour deal: $id")
            val response = api.updateDeal(id, updateDealDto)

            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "✅ Deal mis à jour")
                Result.success(response.body()!!)
            } else {
                val error = "Erreur MAJ: ${response.code()} - ${response.message()}"
                Log.e(TAG, "❌ $error")
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception updateDeal: ${e.message}", e)
            Result.failure(Exception("Erreur de connexion: ${e.message}"))
        }
    }

    suspend fun deleteDeal(id: String): Result<Deal> {
        return try {
            Log.d(TAG, "📡 Suppression deal: $id")
            val response = api.deleteDeal(id)

            if (response.isSuccessful) {
                Log.d(TAG, "✅ Deal supprimé")
                Result.success(response.body()!!)
            } else {
                val error = "Erreur suppression: ${response.code()} - ${response.message()}"
                Log.e(TAG, "❌ $error")
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception deleteDeal: ${e.message}", e)
            Result.failure(Exception("Erreur de connexion: ${e.message}"))
        }
    }
}