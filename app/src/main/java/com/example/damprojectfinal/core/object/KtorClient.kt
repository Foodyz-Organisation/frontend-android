package com.example.damprojectfinal.core.`object`

import android.content.Context
import com.example.damprojectfinal.core.api.ProfessionalApiService
import com.example.damprojectfinal.core.api.TokenManager
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object KtorClient {
    
    private var appContext: Context? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    val client: HttpClient by lazy {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true  // ignores extra backend fields
                    isLenient = true
                })
            }
        }
    }

    val professionalApiService: ProfessionalApiService by lazy {
        val tokenManager = appContext?.let { TokenManager(it) }
        ProfessionalApiService(client, tokenManager)
    }

}
