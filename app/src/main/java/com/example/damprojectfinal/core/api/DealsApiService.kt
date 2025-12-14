package com.example.damprojectfinal.core.api

import com.example.damprojectfinal.core.dto.deals.CreateDealDto
import com.example.damprojectfinal.core.dto.deals.Deal
import com.example.damprojectfinal.core.dto.deals.UpdateDealDto
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

// Interface Retrofit pour l'API Deals
interface DealsApiService {

    @GET("deals")
    suspend fun getAllDeals(): Response<List<Deal>>

    @GET("deals/{id}")
    suspend fun getDealById(@Path("id") id: String): Response<Deal>

    @POST("deals")
    suspend fun createDeal(@Body createDealDto: CreateDealDto): Response<Deal>

    @PATCH("deals/{id}")
    suspend fun updateDeal(
        @Path("id") id: String,
        @Body updateDealDto: UpdateDealDto
    ): Response<Deal>

    @DELETE("deals/{id}")
    suspend fun deleteDeal(@Path("id") id: String): Response<Deal>
}

// Singleton pour créer l'instance Retrofit
object RetrofitInstance {
    private const val BASE_URL = "http://172.18.5.27:3000/"

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val dealsApi: DealsApiService by lazy {
        retrofit.create(DealsApiService::class.java)
    }
}

// Repository pour gérer les appels API
class DealsRepository {
    private val api = RetrofitInstance.dealsApi

    suspend fun getAllDeals(): Result<List<Deal>> {
        return try {
            val response = api.getAllDeals()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Erreur: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDealById(id: String): Result<Deal> {
        return try {
            val response = api.getDealById(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Deal non trouvé"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createDeal(createDealDto: CreateDealDto): Result<Deal> {
        return try {
            val response = api.createDeal(createDealDto)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Erreur lors de la création"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateDeal(id: String, updateDealDto: UpdateDealDto): Result<Deal> {
        return try {
            val response = api.updateDeal(id, updateDealDto)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Erreur lors de la mise à jour"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteDeal(id: String): Result<Deal> {
        return try {
            val response = api.deleteDeal(id)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Erreur lors de la suppression"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}