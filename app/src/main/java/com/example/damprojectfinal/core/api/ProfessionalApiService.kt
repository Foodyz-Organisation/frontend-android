package com.example.damprojectfinal.core.api

import com.example.damprojectfinal.core.dto.auth.ProfessionalDto
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class ProfessionalApiService(private val client: HttpClient) {

    private val BASE_URL = "http://localhost:3000/professionals"

    // Fetch by ID
    suspend fun getById(id: String): ProfessionalDto {
        return client.get("$BASE_URL/$id") {
            contentType(ContentType.Application.Json)
        }.body()
    }

    // Fetch by Email
    suspend fun getByEmail(email: String): ProfessionalDto {
        return client.get("$BASE_URL/email/$email") {
            contentType(ContentType.Application.Json)
        }.body()
    }

    // Search by Name
    suspend fun searchByName(name: String): List<ProfessionalDto> {
        return client.get("$BASE_URL/name/$name") {
            contentType(ContentType.Application.Json)
        }.body()
    }

    // Optional: Update professional
    suspend fun update(id: String, dto: Map<String, Any?>): ProfessionalDto {
        return client.patch("$BASE_URL/$id") {
            contentType(ContentType.Application.Json)
            setBody(dto)
        }.body()
    }

    // Optional: Toggle active
    suspend fun toggleActive(id: String): ProfessionalDto {
        return client.patch("$BASE_URL/$id/toggle") {
            contentType(ContentType.Application.Json)
        }.body()
    }
}
