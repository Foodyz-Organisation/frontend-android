package com.example.damprojectfinal.core.api

import android.util.Log
import com.example.damprojectfinal.feature_event.Event
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

// ==========================
// ✅ Interface Retrofit (API)
// ==========================
interface EventApi {
    @GET("events")
    suspend fun getEvents(): List<Event>

    @GET("events/{id}")
    suspend fun getEventById(@Path("id") id: String): Event

    @POST("events")
    suspend fun createEvent(@Body event: Event): Event

    @PUT("events/{id}")
    suspend fun updateEvent(
        @Path("id") id: String,
        @Body event: Event
    ): Event

    @DELETE("events/{id}")
    suspend fun deleteEvent(@Path("id") id: String)
}

// ===============================
// ✅ Configuration Retrofit Client
// ===============================
object EventRetrofitClient {
    private const val TAG = "RetrofitClient"

    // Pour l’émulateur Android Studio
    private const val BASE_URL_EMULATOR = "http://192.168.1.10:3000/"

    // Pour un appareil physique (remplace par ton IP locale)
    private const val BASE_URL_PHYSICAL = "http://192.168.1.103000/"

    // ⚙️ Choisis ton mode ici :
    private const val USE_EMULATOR = true

    private val BASE_URL = if (USE_EMULATOR) BASE_URL_EMULATOR else BASE_URL_PHYSICAL

    // Intercepteur pour logs HTTP
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Configuration du client HTTP
    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // Initialisation de Retrofit
    val api: EventApi by lazy {
        try {
            Log.d(TAG, "Configuration Retrofit :")
            Log.d(TAG, "  Mode: ${if (USE_EMULATOR) "Émulateur" else "Appareil physique"}")
            Log.d(TAG, "  URL de base: $BASE_URL")
        } catch (_: Exception) {}

        try {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(EventApi::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de l'initialisation de Retrofit", e)
            throw e
        }
    }
}
