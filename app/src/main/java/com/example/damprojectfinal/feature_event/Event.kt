package com.example.foodyz_dam.ui.theme.screens.events

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type
import java.util.UUID

enum class EventStatus {
    @SerializedName("à venir")
    A_VENIR,
    @SerializedName("en cours")
    EN_COURS,
    @SerializedName("terminé")
    TERMINE;
    
    companion object {
        fun fromString(value: String?): EventStatus {
            return when (value?.lowercase()) {
                "à venir", "avenir", "a venir", "a_venir" -> A_VENIR
                "en cours", "encours", "en_cours" -> EN_COURS
                "terminé", "termine" -> TERMINE
                else -> A_VENIR // Valeur par défaut
            }
        }
    }
}

// Adaptateur personnalisé pour gérer les erreurs de désérialisation
class EventStatusDeserializer : JsonDeserializer<EventStatus> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): EventStatus {
        return try {
            val value = json?.asString
            EventStatus.fromString(value)
        } catch (e: Exception) {
            EventStatus.A_VENIR // Valeur par défaut en cas d'erreur
        }
    }
}

data class Event(
// ✅ Changez 'id' en '_id' et enlevez la valeur par défaut
    @SerializedName("_id")
    val _id: String? = null, // L'ObjectId MongoDB du backend

    // ✅ Gardez 'id' optionnel pour la création seulement
    val id: String? = null, // UUID temporaire (optionnel)
    val nom: String = "",
    val description: String = "",
    val date_debut: String = "",
    val date_fin: String = "",
    val image: String? = null,
    val lieu: String = "",
    val categorie: String = "",
    @JsonAdapter(EventStatusDeserializer::class)
    val statut: EventStatus = EventStatus.A_VENIR
)
