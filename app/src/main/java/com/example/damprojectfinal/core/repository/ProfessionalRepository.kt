package com.example.damprojectfinal.core.repository

import android.util.Log
import com.example.damprojectfinal.core.api.ProfessionalApiService
import com.example.damprojectfinal.core.dto.auth.ProfessionalDto

class ProfessionalRepository(private val apiService: ProfessionalApiService) {

    private val TAG = "ProfessionalRepository"

    suspend fun searchByName(name: String): List<ProfessionalDto> =
        runCatching {
            Log.d(TAG, "🔎 ProfessionalRepository.searchByName('$name') called")
            val response = apiService.searchByName(name)
            Log.d(TAG, "✅ searchByName response count = ${response.size}")
            response.forEachIndexed { index, prof ->
                Log.d(TAG, "  [$index] ${prof.fullName} - ${prof.email}")
            }
            response
        }.getOrElse { exception ->
            Log.e(TAG, "❌ ERROR in searchByName: ${exception.javaClass.simpleName}")
            Log.e(TAG, "❌ ERROR message: ${exception.message}")
            exception.printStackTrace()
            emptyList()
        }

    suspend fun getById(id: String): ProfessionalDto? =
        runCatching {
            val response = apiService.getById(id)
            println("DEBUG: getById response JSON = $response")
            response
        }.getOrElse {
            println("ERROR in getById:")
            it.printStackTrace()
            null
        }

    suspend fun getByEmail(email: String): ProfessionalDto? =
        runCatching {
            val response = apiService.getByEmail(email)
            println("DEBUG: getByEmail response JSON = $response")
            response
        }.getOrElse {
            println("ERROR in getByEmail:")
            it.printStackTrace()
            null
        }
}
