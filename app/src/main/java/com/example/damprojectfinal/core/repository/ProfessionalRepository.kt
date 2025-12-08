package com.example.damprojectfinal.core.repository

import com.example.damprojectfinal.core.api.ProfessionalApiService
import com.example.damprojectfinal.core.dto.auth.ProfessionalDto

class ProfessionalRepository(private val apiService: ProfessionalApiService) {

    suspend fun searchByName(name: String): List<ProfessionalDto> =
        runCatching {
            println("DEBUG: 🔎 ProfessionalRepository.searchByName('$name') called")
            val response = apiService.searchByName(name)
            println("DEBUG: ✅ searchByName response count = ${response.size}")
            response.forEachIndexed { index, prof ->
                println("DEBUG:   [$index] ${prof.fullName} - ${prof.email}")
            }
            response
        }.getOrElse { exception ->
            println("ERROR in searchByName: ${exception.javaClass.simpleName}")
            println("ERROR message: ${exception.message}")
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
