package com.example.damprojectfinal.core.api

import com.example.damprojectfinal.core.dto.auth.UserInfoResponse
import com.example.damprojectfinal.core.dto.auth.OrderResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class UserApiService(private val tokenManager: TokenManager) {

    private val BASE_URL = "http://192.168.1.7:3000"

    private val client = HttpClient(Android) {
        // Conversion JSON automatique
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }

        // Timeout des requêtes
        install(HttpTimeout) {
            requestTimeoutMillis = 15000
        }
    }

    // Helper pour ajouter le token à chaque requête
    private fun addAuthHeader(builder: io.ktor.client.request.HttpRequestBuilder) {
        val token = tokenManager.getAccessToken()
        if (!token.isNullOrEmpty()) {
            builder.header(HttpHeaders.Authorization, "Bearer $token")
        }
    }

    /**
     * Récupère les informations de l'utilisateur connecté
     */
    suspend fun getUserInfo(userId: String): UserInfoResponse {
        return client.get("$BASE_URL/users/$userId") {
            addAuthHeader(this)
        }.body()
    }

    /**
     * Récupère les commandes de l'utilisateur
     */
    suspend fun getUserOrders(userId: String): List<OrderResponse> {
        return client.get("$BASE_URL/users/$userId/orders") {
            addAuthHeader(this)
        }.body()
    }

}
