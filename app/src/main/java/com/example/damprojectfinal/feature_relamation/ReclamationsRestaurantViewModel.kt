package com.example.damprojectfinal.feature_relamation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log
import com.example.damprojectfinal.core.dto.reclamation.Reclamation
import com.example.damprojectfinal.core.dto.reclamation.RespondReclamationRequest
import com.example.damprojectfinal.core.repository.ReclamationRepository

class ReclamationsRestaurantViewModel(
    private val repository: ReclamationRepository
) : ViewModel() {

    private val _reclamations = MutableStateFlow<List<Reclamation>>(emptyList())
    val reclamations = _reclamations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _selected = MutableStateFlow<Reclamation?>(null)
    val selected = _selected.asStateFlow()

    /**
     * ✅ NOUVELLE MÉTHODE: Charge les réclamations de MON restaurant
     * Utilise le token JWT automatiquement (pas besoin de restaurantId)
     */
    fun loadMyRestaurantReclamations() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                Log.d("ReclamVM", "🔍 Chargement de MES réclamations restaurant")
                val list = repository.getMyRestaurantReclamations()
                _reclamations.value = list
                Log.d("ReclamVM", "✅ ${list.size} réclamations chargées")
            } catch (e: Exception) {
                Log.e("ReclamVM", "❌ Erreur loadMyRestaurantReclamations", e)
                _error.value = e.message ?: "Erreur inconnue"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * ✅ Charge les réclamations pour un restaurant spécifique par ID
     */
    fun loadReclamationsForRestaurant(restaurantId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                Log.d("ReclamVM", "🔍 Chargement réclamations pour restaurant: $restaurantId")
                val list = repository.getReclamationsByRestaurant(restaurantId)
                _reclamations.value = list
                Log.d("ReclamVM", "✅ ${list.size} réclamations chargées")
            } catch (e: Exception) {
                Log.e("ReclamVM", "❌ Erreur loadReclamationsForRestaurant", e)
                _error.value = e.message ?: "Erreur inconnue"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * ✅ Sélectionne une réclamation
     */
    fun selectReclamation(rec: Reclamation) {
        _selected.value = rec
        Log.d("ReclamVM", "✅ Réclamation sélectionnée: ${rec.id}")
    }

    /**
     * ✅ Désélectionne la réclamation
     */
    fun clearSelected() {
        _selected.value = null
    }

    /**
     * ✅ Répond à une réclamation
     */
    fun respond(
        reclamationId: String,
        responseMessage: String,
        onSuccess: (Reclamation) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                Log.d("ReclamVM", "📝 Envoi de la réponse pour: $reclamationId")
                val req = RespondReclamationRequest(
                    responseMessage = responseMessage,
                    newStatus = "resolue"
                )
                val updated = repository.respondToReclamation(reclamationId, req)

                // Mettre à jour la liste
                _reclamations.value = _reclamations.value.map {
                    if (it.id == updated.id) updated else it
                }

                _selected.value = updated
                Log.d("ReclamVM", "✅ Réponse envoyée avec succès")
                onSuccess(updated)
            } catch (e: Exception) {
                Log.e("ReclamVM", "❌ Erreur respond", e)
                _error.value = e.message ?: "Erreur lors de l'envoi de la réponse"
            } finally {
                _isLoading.value = false
            }
        }
    }
}