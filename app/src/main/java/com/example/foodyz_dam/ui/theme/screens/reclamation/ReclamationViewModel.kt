package com.example.foodyz_dam.ui.theme.screens.reclamation


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
//import com.example.foodyz_dam.repository.ReclamationRepository
import com.example.foodyz_dam.ui.theme.screens.reclamation.CreateReclamationRequest
import com.example.foodyz_dam.ui.theme.screens.reclamation.Reclamation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReclamationViewModel(private val repository: ReclamationRepository) : ViewModel() {

    private val _reclamations = MutableStateFlow<List<Reclamation>>(emptyList())
    val reclamations: StateFlow<List<Reclamation>> get() = _reclamations

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> get() = _errorMessage

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
                Log.d("ReclamationViewModel", "Envoi de la requête: $request")
                val response = repository.createReclamation(request)
                Log.d("ReclamationViewModel", "Réponse reçue: $response")
                loadReclamations() // Recharger la liste
                onSuccess(response)
            } catch (e: Exception) {
                Log.e("ReclamationViewModel", "Erreur lors de la création: ${e.message}", e)
                _errorMessage.value = e.message
            }
        }
    }

}
// Dans ReclamationViewModel.kt, ajoutez à la fin:
class ReclamationViewModelFactory(
    private val repository: ReclamationRepository
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReclamationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReclamationViewModel(repository) as T
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
