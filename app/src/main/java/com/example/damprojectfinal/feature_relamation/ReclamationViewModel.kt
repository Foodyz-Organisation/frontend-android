package com.example.foodyz_dam.ui.theme.screens.reclamation

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.damprojectfinal.core.api.TokenManager
import com.example.damprojectfinal.core.api.UserApiService
import com.example.damprojectfinal.core.dto.auth.OrderResponse
import com.example.damprojectfinal.core.dto.auth.UserInfoResponse
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    private val _reclamations = kotlinx.coroutines.flow.MutableStateFlow<List<Reclamation>>(emptyList())
    val reclamations: kotlinx.coroutines.flow.StateFlow<List<Reclamation>> get() = _reclamations

    private val _errorMessage = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)
    val errorMessage: kotlinx.coroutines.flow.StateFlow<String?> get() = _errorMessage

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            try {
                val userId = tokenManager.getUserId()
                if (userId.isNullOrEmpty()) {
                    uiState = uiState.copy(error = "Utilisateur non connecté")
                    return@launch
                }

                val userInfo = userApiService.getUserInfo(userId)

                if (userInfo.name.isNullOrBlank()) {
                    uiState = uiState.copy(
                        error = "Impossible de récupérer le nom de l'utilisateur"
                    )
                    return@launch
                }

                val orders = userApiService.getUserOrders(userId)
                uiState = uiState.copy(userInfo = userInfo, orders = orders)
            } catch (e: Exception) {
                uiState = uiState.copy(error = "Erreur lors du chargement des données: ${e.message}")
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
        val userName = uiState.userInfo?.name
        return uiState.selectedOrder.isNotBlank() &&
                uiState.complaintType.isNotBlank() &&
                uiState.description.isNotBlank() &&
                uiState.agreeToTerms &&
                !userName.isNullOrBlank()
    }

    fun loadReclamations() {
        viewModelScope.launch {
            try {
                _reclamations.value = repository.getAllReclamations()
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun createReclamation(request: CreateReclamationRequest, onSuccess: (Reclamation) -> Unit) {
        viewModelScope.launch {
            try {
                val response = repository.createReclamation(request)
                loadReclamations()
                onSuccess(response)
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun submitReclamation(onSuccess: () -> Unit = {}) {
        val userName = uiState.userInfo?.name?.trim()
        if (userName.isNullOrBlank()) {
            uiState = uiState.copy(
                error = "Nom d'utilisateur manquant. Veuillez vous reconnecter."
            )
            return
        }

        if (!isFormValid()) {
            uiState = uiState.copy(
                error = "Veuillez remplir tous les champs requis"
            )
            return
        }

        val request = CreateReclamationRequest(
            nomClient = userName,
            commandeConcernee = uiState.selectedOrder.trim(),
            complaintType = uiState.complaintType,
            description = uiState.description.trim(),
            photos = uiState.photos.map { it.toString() }
        )

        createReclamation(request) {
            uiState = uiState.copy(submitSuccess = true)
            onSuccess()
        }
    }


    fun resetState() {
        uiState = ReclamationUiState()
        loadUserData()
    }
}

class ReclamationViewModelFactory(
    private val userApiService: UserApiService,
    private val tokenManager: TokenManager,
    private val repository: ReclamationRepository
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReclamationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReclamationViewModel(userApiService, tokenManager, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

fun parseDate(dateString: String?): Date? {
    return try {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).parse(dateString)
    } catch (e: Exception) {
        null
    }
}
