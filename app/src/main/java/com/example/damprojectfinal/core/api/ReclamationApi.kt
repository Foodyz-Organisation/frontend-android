package com.example.foodyz_dam.ui.theme.screens.reclamation

import android.util.Log
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
}

object ReclamationRetrofitClient {
    private const val TAG = "ReclamationClient"
    private const val BASE_URL = "http://10.0.2.2:3000/"

    // ✅ Fonction pour créer le client avec token
    fun createClient(token: String): ReclamationApi {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // ✅ Intercepteur pour ajouter le token JWT
        val authInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            val authenticatedRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()

            Log.d(TAG, "Requête avec token: Bearer ${token.take(20)}...")
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