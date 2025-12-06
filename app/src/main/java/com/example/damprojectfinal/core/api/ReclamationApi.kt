package com.example.damprojectfinal.core.api

import android.util.Log
import com.example.damprojectfinal.core.dto.reclamation.CreateReclamationRequest
import com.example.damprojectfinal.core.dto.reclamation.Reclamation
import com.example.damprojectfinal.core.dto.reclamation.RespondReclamationRequest
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.Interceptor
import java.util.concurrent.TimeUnit

interface ReclamationApi {
    @GET("reclamation")
    suspend fun getAllReclamations(): List<Reclamation>

    @GET("reclamation/my-reclamations")
    suspend fun getMyReclamations(): List<Reclamation>

    @POST("reclamation")
    suspend fun createReclamation(@Body request: CreateReclamationRequest): Reclamation

    @GET("reclamation/{id}")
    suspend fun getReclamationById(@Path("id") id: String): Reclamation

    /**
     * ✅ NOUVELLE ROUTE: Récupère les réclamations de MON restaurant
     * Endpoint: GET /reclamation/restaurant/my-reclamations
     */
    @GET("reclamation/restaurant/my-reclamations")
    suspend fun getMyRestaurantReclamations(): List<Reclamation>

    /**
     * ✅ Récupère les réclamations pour un restaurant spécifique par ID
     * Endpoint: GET /reclamation/restaurant/:restaurantId
     */
    @GET("reclamation/restaurant/{restaurantId}")
    suspend fun getReclamationsByRestaurant(
        @Path("restaurantId") restaurantId: String
    ): List<Reclamation>

    /**
     * ✅ Répond à une réclamation
     * Endpoint: PUT /reclamation/:id/respond
     */
    @PUT("reclamation/{id}/respond")
    suspend fun respondToReclamation(
        @Path("id") id: String,
        @Body request: RespondReclamationRequest
    ): Reclamation
}

object ReclamationRetrofitClient {
    private const val TAG = "ReclamationClient"
    private const val BASE_URL = "http://192.168.1.10:3000/"

    fun createClient(token: String): ReclamationApi {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val authInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            val authenticatedRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()

            Log.d(TAG, "🔐 Requête avec token: Bearer ${token.take(20)}...")
            chain.proceed(authenticatedRequest)
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ReclamationApi::class.java)
    }
}