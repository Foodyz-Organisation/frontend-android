package com.example.damprojectfinal.core.api.professionalUser

import com.example.damprojectfinal.core.dto.professionalUser.ProfessionalUserAccount // Assuming you'll create this DTO
import retrofit2.http.GET
import retrofit2.http.Path

interface ProfessionalApiService {

    @GET("professionals/{id}") // Adjust endpoint path if your Nest.js controller uses a different prefix
    suspend fun getProfessionalAccount(@Path("id") professionalId: String): ProfessionalUserAccount

    // Add other professional-related endpoints here as needed
}
