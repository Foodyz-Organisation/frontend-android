package com.example.damprojectfinal.core.api.professionalUser

import com.example.damprojectfinal.core.dto.professionalUser.ProfessionalUserAccount
import com.example.damprojectfinal.core.dto.professionalUser.UpdateProfessionalRequest
import retrofit2.http.*

interface ProfessionalApiService {

    @GET("professionals/{id}")
    suspend fun getProfessionalAccount(@Path("id") professionalId: String): ProfessionalUserAccount

    @PATCH("professionals/{id}")
    suspend fun updateProfessional(
        @Path("id") professionalId: String,
        @Body request: UpdateProfessionalRequest
    ): ProfessionalUserAccount

    // Follow/Unfollow endpoints
    @PATCH("professionals/{professionalId}/follow")
    suspend fun followProfessional(@Path("professionalId") professionalId: String): ProfessionalUserAccount

    @DELETE("professionals/{professionalId}/follow")
    suspend fun unfollowProfessional(@Path("professionalId") professionalId: String): ProfessionalUserAccount
}
