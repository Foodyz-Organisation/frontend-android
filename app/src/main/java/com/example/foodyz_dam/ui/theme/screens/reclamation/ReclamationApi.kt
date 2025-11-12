package com.example.foodyz_dam.ui.theme.screens.reclamation

import android.util.Log
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

interface ReclamationApi {
    @GET("reclamation")
    suspend fun getAllReclamations(): List<Reclamation>

    @POST("reclamation")
    suspend fun createReclamation(@Body request: CreateReclamationRequest): Reclamation
}

object ReclamationRetrofitClient {
    private const val TAG = "ReclamationClient"
    private const val BASE_URL_EMULATOR = "http://10.0.2.2:3000/"
    private const val BASE_URL_PHYSICAL = "http://192.168.1.100:3000/"
    private const val USE_EMULATOR = true
    private val BASE_URL = if (USE_EMULATOR) BASE_URL_EMULATOR else BASE_URL_PHYSICAL

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // Changed from 'api' to 'reclamationApi' to match your navigation usage
    val reclamationApi: ReclamationApi by lazy {
        try {
            Log.d(TAG, "Initialisation de Retrofit sur $BASE_URL")
        } catch (_: Exception) {}
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ReclamationApi::class.java)
    }
}