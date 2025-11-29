package com.example.damprojectfinal.core.repository

import com.example.damprojectfinal.core.api.ProfessionalApiService
import com.example.damprojectfinal.core.dto.auth.ProfessionalDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class ProfessionalRepository(private val apiService: ProfessionalApiService) {

    // Fetch professional by ID
    suspend fun getById(id: String): ProfessionalDto? {
        return try {
            withContext(Dispatchers.IO) {
                apiService.getById(id)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Fetch professional by Email
    suspend fun getByEmail(email: String): ProfessionalDto? {
        return try {
            withContext(Dispatchers.IO) {
                apiService.getByEmail(email)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Search professionals by Name
    suspend fun searchByName(name: String): List<ProfessionalDto> {
        return try {
            withContext(Dispatchers.IO) {
                apiService.searchByName(name)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // Update professional
    suspend fun updateProfessional(id: String, updates: Map<String, Any?>): ProfessionalDto? {
        return try {
            withContext(Dispatchers.IO) {
                apiService.update(id, updates)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Toggle active status
    suspend fun toggleActive(id: String): ProfessionalDto? {
        return try {
            withContext(Dispatchers.IO) {
                apiService.toggleActive(id)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
