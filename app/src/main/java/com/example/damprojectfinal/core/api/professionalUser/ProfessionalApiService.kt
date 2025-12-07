package com.example.damprojectfinal.core.api.professionalUser

import com.example.damprojectfinal.core.dto.professionalUser.ProfessionalUserAccount // Assuming you'll create this DTO
import retrofit2.http.*
import retrofit2.http.DELETE
import retrofit2.http.PATCH

interface ProfessionalApiService {

    @GET("professionals/{id}") // Adjust endpoint path if your Nest.js controller uses a different prefix
    suspend fun getProfessionalAccount(@Path("id") professionalId: String): ProfessionalUserAccount

    // Follow/Unfollow endpoints
    @PATCH("professionals/{professionalId}/follow")
    suspend fun followProfessional(@Path("professionalId") professionalId: String): ProfessionalUserAccount

    @DELETE("professionals/{professionalId}/follow")
    suspend fun unfollowProfessional(@Path("professionalId") professionalId: String): ProfessionalUserAccount
}
