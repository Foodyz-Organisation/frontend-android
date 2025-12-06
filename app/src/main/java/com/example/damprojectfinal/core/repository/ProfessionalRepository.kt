package com.example.damprojectfinal.core.repository

import com.example.damprojectfinal.core.api.ProfessionalApiService
import com.example.damprojectfinal.core.dto.auth.ProfessionalDto

class ProfessionalRepository(private val apiService: ProfessionalApiService) {

    suspend fun searchByName(name: String): List<ProfessionalDto> =
        runCatching {
            val response = apiService.searchByName(name)
            println("DEBUG: searchByName response JSON = $response")
            response
        }.getOrElse {
            println("ERROR in searchByName:")
            it.printStackTrace()
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
