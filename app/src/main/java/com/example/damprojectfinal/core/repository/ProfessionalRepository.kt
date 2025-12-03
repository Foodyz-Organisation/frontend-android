package com.example.damprojectfinal.core.repository

import com.example.damprojectfinal.core.api.ProfessionalApiService
import com.example.damprojectfinal.core.dto.auth.ProfessionalDto

class ProfessionalRepository(private val apiService: ProfessionalApiService) {

    suspend fun searchByName(name: String): List<ProfessionalDto> =
        runCatching { apiService.searchByName(name) }
            .getOrElse {
                it.printStackTrace()
                emptyList()
            }

    suspend fun getById(id: String): ProfessionalDto? =
        runCatching { apiService.getById(id) }
            .getOrElse {
                it.printStackTrace()
                null
            }

    suspend fun getByEmail(email: String): ProfessionalDto? =
        runCatching { apiService.getByEmail(email) }
            .getOrElse {
                it.printStackTrace()
                null
            }
}
