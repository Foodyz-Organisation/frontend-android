package com.example.damprojectfinal.core.`object`

import android.content.Context
import com.example.damprojectfinal.core.api.ProfessionalApiService
import com.example.damprojectfinal.core.api.TokenManager
import io.ktor.client.*
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object KtorClient {
    
    private var appContext: Context? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    val client: HttpClient by lazy {
        HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true  // ignores extra backend fields
                    isLenient = true
                })
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 30000
                connectTimeoutMillis = 30000
                socketTimeoutMillis = 30000
            }
        }
    }

    val professionalApiService: ProfessionalApiService by lazy {
        val tokenManager = appContext?.let { TokenManager(it) }
        ProfessionalApiService(client, tokenManager)
    }

}
