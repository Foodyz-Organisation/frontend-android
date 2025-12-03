package com.example.damprojectfinal.core.api

import com.example.damprojectfinal.core.dto.auth.ProfessionalDto
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class ProfessionalApiService(private val client: HttpClient) {

    private val BASE_URL = "http://10.0.2.2:3000/professionals"

    suspend fun getById(id: String): ProfessionalDto =
        client.get("$BASE_URL/$id") {
            contentType(ContentType.Application.Json)
        }.body()

    suspend fun getByEmail(email: String): ProfessionalDto =
        client.get("$BASE_URL/email/${email.encodeURLPath()}") {
            contentType(ContentType.Application.Json)
        }.body()

    suspend fun searchByName(name: String): List<ProfessionalDto> {
        val encodedName = name.encodeURLPath() // encode ' and spaces
        return client.get {
            url {
                takeFrom(BASE_URL)
                encodedPath += "/name/$encodedName"
            }
            contentType(ContentType.Application.Json)
        }.body()
    }
}
