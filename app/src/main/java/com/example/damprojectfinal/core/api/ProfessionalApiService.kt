package com.example.damprojectfinal.core.api

import android.util.Log
import com.example.damprojectfinal.core.dto.auth.ProfessionalDto
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import android.content.Context

class ProfessionalApiService(
    private val client: HttpClient,
    private val tokenManager: TokenManager? = null
) {

    private val TAG = "ProfessionalApiService"
    private val BASE_URL = "${BaseUrlProvider.BASE_URL}/professionals"

    private suspend fun HttpRequestBuilder.addAuthHeader() {
        tokenManager?.let {
            val token = it.getAccessTokenAsync()
            if (!token.isNullOrEmpty()) {
                header("Authorization", "Bearer $token")
                Log.d(TAG, "🔐 Added Authorization header")
            } else {
                Log.w(TAG, "⚠️ No token available for authentication")
            }
        } ?: Log.w(TAG, "⚠️ TokenManager is null - no authentication")
    }

    suspend fun getById(id: String): ProfessionalDto =
        client.get("$BASE_URL/$id") {
            contentType(ContentType.Application.Json)
            addAuthHeader()
        }.body()

    suspend fun getByEmail(email: String): ProfessionalDto =
        client.get("$BASE_URL/email/${email.encodeURLPath()}") {
            contentType(ContentType.Application.Json)
            addAuthHeader()
        }.body()

    suspend fun searchByName(name: String): List<ProfessionalDto> {
        val encodedName = name.encodeURLPath() // encode ' and spaces
        val fullUrl = "$BASE_URL/name/$encodedName"
        Log.d(TAG, "🌐 Ktor API calling: $fullUrl")
        Log.d(TAG, "🔍 Search query: '$name' (encoded: '$encodedName')")
        
        return try {
            val response: List<ProfessionalDto> = client.get {
                url {
                    takeFrom(BASE_URL)
                    encodedPath += "/name/$encodedName"
                }
                contentType(ContentType.Application.Json)
                addAuthHeader()
            }.body()
            Log.d(TAG, "✅ Ktor response received with ${response.size} professionals")
            if (response.isEmpty()) {
                Log.w(TAG, "⚠️ No professionals found for query: '$name'")
            } else {
                response.forEachIndexed { index, prof ->
                    Log.d(TAG, "  [$index] ${prof.fullName} (${prof.email})")
                }
            }
            response
        } catch (e: Exception) {
            Log.e(TAG, "❌ Ktor API call failed: ${e.javaClass.simpleName} - ${e.message}", e)
            Log.e(TAG, "❌ URL was: $fullUrl")
            e.printStackTrace()
            throw e
        }
    }
}
