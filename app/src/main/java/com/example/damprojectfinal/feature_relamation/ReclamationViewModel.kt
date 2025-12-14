package com.example.damprojectfinal.feature_relamation

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.damprojectfinal.core.api.ReclamationRetrofitClient
import com.example.damprojectfinal.core.api.TokenManager
import com.example.damprojectfinal.core.api.UserApiService
import com.example.damprojectfinal.core.dto.auth.OrderResponse
import com.example.damprojectfinal.core.dto.auth.UserInfoResponse
import com.example.damprojectfinal.core.dto.reclamation.Reclamation
import com.example.damprojectfinal.core.repository.ReclamationRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ReclamationUiState(
    val isLoading: Boolean = false,
    val userInfo: UserInfoResponse? = null,
    val orders: List<OrderResponse> = emptyList(),
    val selectedOrder: String = "",
    val complaintType: String = "",
    val description: String = "",
    val photos: List<Uri> = emptyList(),
    val agreeToTerms: Boolean = false,
    val error: String? = null,
    val submitSuccess: Boolean = false
)

class ReclamationViewModel(
    private val userApiService: UserApiService,
    private val tokenManager: TokenManager,
    private val repository: ReclamationRepository
) : ViewModel() {

    var uiState by mutableStateOf(ReclamationUiState())
        private set

    private val _reclamations = MutableStateFlow<List<Reclamation>>(emptyList())
    val reclamations = _reclamations.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _selectedReclamation = MutableStateFlow<Reclamation?>(null)
    val selectedReclamation = _selectedReclamation.asStateFlow()
    
    private val _orders = MutableStateFlow<List<OrderResponse>>(emptyList())
    val orders = _orders.asStateFlow()

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            try {
                val userId = tokenManager.getUserIdAsync()
                if (userId.isNullOrEmpty()) {
                    uiState = uiState.copy(error = "Utilisateur non connecté")
                    return@launch
                }

                val userInfo = userApiService.getUserInfo(userId)
                val orders = userApiService.getUserOrders(userId)
                _orders.value = orders
                uiState = uiState.copy(userInfo = userInfo, orders = orders)
            } catch (e: Exception) {
                uiState = uiState.copy(error = "Erreur: ${e.message}")
            }
        }
    }

    fun updateSelectedOrder(order: String) {
        uiState = uiState.copy(selectedOrder = order)
    }

    fun updateComplaintType(type: String) {
        uiState = uiState.copy(complaintType = type)
    }

    fun updateDescription(description: String) {
        if (description.length <= 500) {
            uiState = uiState.copy(description = description)
        }
    }

    fun addPhotos(newPhotos: List<Uri>) {
        val updatedPhotos = (uiState.photos + newPhotos).distinct().take(4)
        uiState = uiState.copy(photos = updatedPhotos)
    }

    fun removePhoto(uri: Uri) {
        uiState = uiState.copy(photos = uiState.photos.filterNot { it == uri })
    }

    fun toggleAgreeToTerms() {
        uiState = uiState.copy(agreeToTerms = !uiState.agreeToTerms)
    }

    fun isFormValid(): Boolean {
        return uiState.selectedOrder.isNotBlank() &&
                uiState.complaintType.isNotBlank() &&
                uiState.description.isNotBlank() &&
                uiState.agreeToTerms
    }

    fun loadReclamations() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                val token = tokenManager.getAccessTokenAsync()
                if (token.isNullOrEmpty()) {
                    _errorMessage.value = "Token manquant"
                    return@launch
                }

                val api = ReclamationRetrofitClient.createClient(token)
                val result = api.getMyReclamations()
                _reclamations.value = result

                Log.d("ReclamationVM", "✅ ${result.size} réclamations chargées")
            } catch (e: Exception) {
                Log.e("ReclamationVM", "❌ Erreur: ${e.message}", e)
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadReclamationById(id: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val token = tokenManager.getAccessTokenAsync() ?: return@launch

                val api = ReclamationRetrofitClient.createClient(token)
                val reclamation = api.getReclamationById(id)
                _selectedReclamation.value = reclamation

            } catch (e: Exception) {
                Log.e("ReclamationVM", "❌ Erreur: ${e.message}", e)
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * ✅ Crée une réclamation avec conversion Base64
     */
    fun createReclamation(
        commandeConcernee: String,
        complaintType: String,
        description: String,
        photoUris: List<Uri>,
        onSuccess: (Reclamation) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                Log.e("ReclamationVM", "════════════════════════════════")
                Log.e("ReclamationVM", "🚀 Submit depuis ViewModel")
                Log.e("ReclamationVM", "   Photos: ${photoUris.size}")

                val response = repository.createReclamation(
                    commandeConcernee = commandeConcernee,
                    complaintType = complaintType,
                    description = description,
                    photoUris = photoUris
                )

                Log.e("ReclamationVM", "✅ Créée: ${response.id}")
                Log.e("ReclamationVM", "════════════════════════════════")

                loadReclamations()
                onSuccess(response)

            } catch (e: Exception) {
                Log.e("ReclamationVM", "❌ Erreur: ${e.message}", e)
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun submitReclamation(onSuccess: () -> Unit = {}) {
        if (!isFormValid()) {
            uiState = uiState.copy(error = "Veuillez remplir tous les champs")
            return
        }

        Log.e("ReclamationVM", "========== SUBMIT ==========")
        Log.e("ReclamationVM", "Commande: ${uiState.selectedOrder}")
        Log.e("ReclamationVM", "Type: ${uiState.complaintType}")
        Log.e("ReclamationVM", "Photos: ${uiState.photos.size}")

        createReclamation(
            commandeConcernee = uiState.selectedOrder.trim(),
            complaintType = uiState.complaintType,
            description = uiState.description.trim(),
            photoUris = uiState.photos
        ) {
            uiState = uiState.copy(submitSuccess = true)
            onSuccess()
        }
    }

    fun resetState() {
        uiState = ReclamationUiState()
        loadUserData()
    }
}