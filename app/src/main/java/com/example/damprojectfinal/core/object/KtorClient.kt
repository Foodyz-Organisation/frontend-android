package com.example.damprojectfinal.core.`object`

import com.example.damprojectfinal.core.api.ProfessionalApiService
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object KtorClient {

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
        ProfessionalApiService(client)
    }

}
