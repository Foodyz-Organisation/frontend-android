// src/main/java/com/example/damprojectfinal/core.api.posts/RetrofitClient.kt
package com.example.damprojectfinal.core.api.posts

// ... (existing imports) ...
import com.example.damprojectfinal.core.api.AuthInterceptor // <-- NEW IMPORT
import com.example.damprojectfinal.core.api.TokenManager // <-- NEW IMPORT
import android.content.Context // <-- NEW IMPORT for Context to initialize TokenManager
import com.example.damprojectfinal.core.api.normalUser.UserApiService
import com.example.damprojectfinal.core.api.professionalUser.ProfessionalApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:3000/"

    lateinit var appContext: Context // Declare appContext here

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        setLevel(HttpLoggingInterceptor.Level.BODY)
    }

    // OkHttp Client configured with the logging interceptor.
    private val okHttpClient: OkHttpClient by lazy { // Change to 'lazy' to depend on appContext
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            // --- NEW: Add AuthInterceptor ---
            .addInterceptor(AuthInterceptor(TokenManager(appContext))) // Pass TokenManager instance, using appContext
            // --- END NEW ---
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    // Retrofit instance, initialized lazily.
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // Attach our configured OkHttp client
            .addConverterFactory(GsonConverterFactory.create()) // Use Gson for JSON serialization/deserialization
            .build()
    }

    // --- API Services ---
    // Lazy-initialized instance of PostsApiService.
    val postsApiService: PostsApiService by lazy {
        retrofit.create(PostsApiService::class.java)
    }


    val reelsApiService: ReelsApiService by lazy {
        retrofit.create(ReelsApiService::class.java)
    }

    val userApiService: UserApiService by lazy {
        retrofit.create(UserApiService::class.java)
    }

    val professionalApiService: ProfessionalApiService by lazy {
        retrofit.create(ProfessionalApiService::class.java)
    }

}
