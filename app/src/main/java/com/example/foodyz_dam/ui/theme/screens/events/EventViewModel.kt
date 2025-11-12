package com.example.foodyz_dam.ui.theme.screens.events

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class EventViewModel : ViewModel() {
    private val TAG = "EventViewModel"
    
    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        // Charger les événements de manière sécurisée
        try {
            loadEvents()
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de l'initialisation", e)
            _error.value = "Erreur d'initialisation: ${e.message}"
            _isLoading.value = false
        }
    }

    private fun getErrorMessage(e: Exception): String {
        return when (e) {
            is ConnectException -> {
                "Impossible de se connecter au serveur. Vérifiez que:\n" +
                "• Le backend est démarré sur le port 3000\n" +
                "• Vous utilisez la bonne URL (émulateur: 10.0.2.2, appareil physique: IP locale)\n" +
                "• L'appareil et le serveur sont sur le même réseau"
            }
            is UnknownHostException -> {
                "Serveur introuvable. Vérifiez l'URL de connexion dans RetrofitClient.kt"
            }
            is SocketTimeoutException -> {
                "Timeout: Le serveur ne répond pas. Vérifiez votre connexion réseau."
            }
            is JsonSyntaxException -> {
                "Erreur de format des données reçues du serveur. Vérifiez que le backend envoie des données valides."
            }
            is IllegalStateException -> {
                "Erreur lors du traitement des données: ${e.message}"
            }
            else -> {
                "Erreur: ${e.localizedMessage ?: e.message ?: "Erreur inconnue"}\nType: ${e.javaClass.simpleName}"
            }
        }
    }

    fun loadEvents() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                Log.d(TAG, "Chargement des événements...")
                
                // Protection contre les crashes lors de l'accès à l'API
                try {
                    val response = EventRetrofitClient.api.getEvents()
                    _events.value = response
                    Log.d(TAG, "Événements chargés: ${response.size}")
                } catch (apiException: Exception) {
                    // Erreur spécifique de l'API
                    throw apiException
                }
            } catch (e: Exception) {
                val errorMsg = getErrorMessage(e)
                _error.value = errorMsg
                _events.value = emptyList() // S'assurer que la liste est vide en cas d'erreur
                Log.e(TAG, "Erreur lors du chargement des événements", e)
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addEvent(event: Event) {
        viewModelScope.launch {
            _error.value = null
            try {
                Log.d(TAG, "Création d'un nouvel événement...")
                val newEvent = EventRetrofitClient.api.createEvent(event)
                _events.value = _events.value + newEvent
                Log.d(TAG, "Événement créé avec succès")
            } catch (e: Exception) {
                val errorMsg = getErrorMessage(e)
                _error.value = "Impossible de créer l'événement: $errorMsg"
                Log.e(TAG, "Erreur lors de la création de l'événement", e)
            }
        }
    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            _error.value = null
            try {
                Log.d(TAG, "Suppression de l'événement: $eventId")
                EventRetrofitClient.api.deleteEvent(eventId)
                // ✅ Filtrez par _id maintenant
                _events.value = _events.value.filter { it._id != eventId }
                Log.d(TAG, "Événement supprimé avec succès")
            } catch (e: Exception) {
                val errorMsg = getErrorMessage(e)
                _error.value = "Impossible de supprimer: $errorMsg"
                Log.e(TAG, "Erreur lors de la suppression de l'événement", e)
            }
        }
    }
    fun updateEvent(
        event: Event,
        nom: String,
        description: String,
        dateDebut: String,
        dateFin: String,
        image: String?,
        lieu: String,
        categorie: String,
        statut: EventStatus
    ) {
        val id = event._id
        if (id.isNullOrEmpty()) {
            _error.value = "Impossible de mettre à jour : l'événement n'a pas d'_id valide"
            Log.e(TAG, "updateEvent échoué : _id manquant pour ${event.nom}")
            return
        }

        viewModelScope.launch {
            _error.value = null
            try {
                Log.d(TAG, "Mise à jour de l'événement avec _id=$id")

                val updatedEvent = event.copy(
                    nom = nom,
                    description = description,
                    date_debut = dateDebut,
                    date_fin = dateFin,
                    image = image,
                    lieu = lieu,
                    categorie = categorie,
                    statut = statut
                )

                val response = EventRetrofitClient.api.updateEvent(id, updatedEvent)

                // Mettre à jour la liste locale
                _events.value = _events.value.map { if (it._id == id) response else it }
                Log.d(TAG, "Événement mis à jour avec succès : ${response.nom}")
            } catch (e: Exception) {
                val errorMsg = getErrorMessage(e)
                _error.value = "Impossible de modifier l'événement: $errorMsg"
                Log.e(TAG, "Erreur lors de la mise à jour de l'événement avec _id=$id", e)
            }
        }
    }

}