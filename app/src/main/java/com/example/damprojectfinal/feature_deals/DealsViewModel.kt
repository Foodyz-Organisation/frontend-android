package com.example.damprojectfinal.feature_deals

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.damprojectfinal.core.dto.deals.CreateDealDto
import com.example.damprojectfinal.core.dto.deals.Deal
import com.example.damprojectfinal.core.dto.deals.UpdateDealDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// État UI
sealed class DealsUiState {
    object Loading : DealsUiState()
    data class Success(val deals: List<Deal>) : DealsUiState()
    data class Error(val message: String) : DealsUiState()
}

sealed class DealDetailUiState {
    object Loading : DealDetailUiState()
    data class Success(val deal: Deal) : DealDetailUiState()
    data class Error(val message: String) : DealDetailUiState()
}

class DealsViewModel(
    private val repository: DealsRepository = DealsRepository()
) : ViewModel() {

    companion object {
        private const val TAG = "DealsViewModel"
    }

    private val _dealsState = MutableStateFlow<DealsUiState>(DealsUiState.Loading)
    val dealsState: StateFlow<DealsUiState> = _dealsState.asStateFlow()

    private val _dealDetailState = MutableStateFlow<DealDetailUiState>(DealDetailUiState.Loading)
    val dealDetailState: StateFlow<DealDetailUiState> = _dealDetailState.asStateFlow()

    private val _operationResult = MutableStateFlow<Result<String>?>(null)
    val operationResult: StateFlow<Result<String>?> = _operationResult.asStateFlow()

    init {
        Log.d(TAG, "🎬 DealsViewModel initialisé - hashCode: ${this.hashCode()}")
        Log.d(TAG, "🔄 Chargement initial des deals...")
        loadDeals()
    }

    fun loadDeals() {
        Log.d(TAG, "📋 loadDeals() appelée")
        viewModelScope.launch {
            try {
                Log.d(TAG, "⏳ État: Loading")
                _dealsState.value = DealsUiState.Loading

                Log.d(TAG, "🌐 Appel API repository.getAllDeals()...")
                val result = repository.getAllDeals()

                result.onSuccess { deals ->
                    Log.d(TAG, "✅ API Success - ${deals.size} deals reçus")

                    // ✅ TEMPORAIRE : Afficher TOUS les deals sans filtrage
                    Log.d(TAG, "✅ ${deals.size} deals chargés (sans filtrage)")
                    _dealsState.value = DealsUiState.Success(deals)
                }

                result.onFailure { error ->
                    Log.e(TAG, "❌ API Error: ${error.message}")
                    Log.e(TAG, "❌ Stack trace:", error)
                    _dealsState.value = DealsUiState.Error(
                        error.message ?: "Erreur lors du chargement"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "💥 Exception dans loadDeals(): ${e.message}")
                Log.e(TAG, "💥 Stack trace:", e)
                _dealsState.value = DealsUiState.Error(e.message ?: "Erreur inattendue")
            }
        }
    }

    fun loadDealById(id: String) {
        Log.d(TAG, "🔍 loadDealById($id)")
        viewModelScope.launch {
            try {
                _dealDetailState.value = DealDetailUiState.Loading

                val result = repository.getDealById(id)

                result.onSuccess { deal ->
                    Log.d(TAG, "✅ Deal chargé: ${deal.restaurantName}")
                    _dealDetailState.value = DealDetailUiState.Success(deal)
                }

                result.onFailure { error ->
                    Log.e(TAG, "❌ Erreur chargement deal: ${error.message}")
                    _dealDetailState.value = DealDetailUiState.Error(
                        error.message ?: "Deal introuvable"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "💥 Exception loadDealById: ${e.message}", e)
                _dealDetailState.value = DealDetailUiState.Error(e.message ?: "Erreur")
            }
        }
    }

    fun createDeal(createDealDto: CreateDealDto) {
        Log.d(TAG, "➕ createDeal: ${createDealDto.restaurantName}")
        viewModelScope.launch {
            try {
                Log.d(TAG, "📤 Envoi données:")
                Log.d(TAG, "   - Restaurant: ${createDealDto.restaurantName}")
                Log.d(TAG, "   - Category: ${createDealDto.category}")
                Log.d(TAG, "   - StartDate: ${createDealDto.startDate}")
                Log.d(TAG, "   - EndDate: ${createDealDto.endDate}")

                val result = repository.createDeal(createDealDto)

                result.onSuccess { deal ->
                    Log.d(TAG, "✅ Deal créé avec ID: ${deal._id}")
                    _operationResult.value = Result.success("Deal créé avec succès")

                    // ✅ IMPORTANT: Recharger la liste après création
                    Log.d(TAG, "🔄 Rechargement de la liste...")
                    loadDeals()
                }

                result.onFailure { error ->
                    Log.e(TAG, "❌ Erreur création: ${error.message}")
                    _operationResult.value = Result.failure(error)
                }
            } catch (e: Exception) {
                Log.e(TAG, "💥 Exception createDeal: ${e.message}", e)
                _operationResult.value = Result.failure(e)
            }
        }
    }

    fun updateDeal(id: String, updateDealDto: UpdateDealDto) {
        Log.d(TAG, "✏️ updateDeal: $id")
        viewModelScope.launch {
            try {
                Log.d(TAG, "📤 Mise à jour:")
                Log.d(TAG, "   - ID: $id")
                Log.d(TAG, "   - Restaurant: ${updateDealDto.restaurantName}")
                Log.d(TAG, "   - isActive: ${updateDealDto.isActive}")

                val result = repository.updateDeal(id, updateDealDto)

                result.onSuccess { deal ->
                    Log.d(TAG, "✅ Deal mis à jour: ${deal._id}")
                    _operationResult.value = Result.success("Deal mis à jour")

                    // ✅ Recharger la liste après modification
                    Log.d(TAG, "🔄 Rechargement de la liste...")
                    loadDeals()
                }

                result.onFailure { error ->
                    Log.e(TAG, "❌ Erreur MAJ: ${error.message}")
                    _operationResult.value = Result.failure(error)
                }
            } catch (e: Exception) {
                Log.e(TAG, "💥 Exception updateDeal: ${e.message}", e)
                _operationResult.value = Result.failure(e)
            }
        }
    }

    fun deleteDeal(id: String) {
        Log.d(TAG, "🗑️ deleteDeal: $id")
        viewModelScope.launch {
            try {
                val result = repository.deleteDeal(id)

                result.onSuccess {
                    Log.d(TAG, "✅ Deal supprimé: $id")
                    _operationResult.value = Result.success("Deal supprimé")

                    // ✅ Recharger la liste après suppression
                    Log.d(TAG, "🔄 Rechargement de la liste...")
                    loadDeals()
                }

                result.onFailure { error ->
                    Log.e(TAG, "❌ Erreur suppression: ${error.message}")
                    _operationResult.value = Result.failure(error)
                }
            } catch (e: Exception) {
                Log.e(TAG, "💥 Exception deleteDeal: ${e.message}", e)
                _operationResult.value = Result.failure(e)
            }
        }
    }

    fun clearOperationResult() {
        Log.d(TAG, "🧹 Nettoyage operationResult")
        _operationResult.value = null
    }

    private fun isValidDeal(deal: Deal): Boolean {
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")
            val currentDate = Date()
            val startDate = dateFormat.parse(deal.startDate)
            val endDate = dateFormat.parse(deal.endDate)

            val isActive = deal.isActive
            val isAfterStart = startDate != null && currentDate.after(startDate)
            val isBeforeEnd = endDate != null && currentDate.before(endDate)

            val isValid = isActive && startDate != null && endDate != null && isAfterStart && isBeforeEnd

            if (!isValid) {
                Log.d(TAG, "    ⚠️ Deal invalide: isActive=$isActive, afterStart=$isAfterStart, beforeEnd=$isBeforeEnd")
            }

            isValid
        } catch (e: Exception) {
            Log.e(TAG, "    ❌ Erreur validation deal: ${e.message}")
            false
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "🔚 DealsViewModel détruit - hashCode: ${this.hashCode()}")
    }
}