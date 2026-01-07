package com.example.damprojectfinal.core.retro

import android.content.Context
import com.example.damprojectfinal.core.api.AuthInterceptor
import com.example.damprojectfinal.core.api.BaseUrlProvider
import com.example.damprojectfinal.core.api.CartApiService
import com.example.damprojectfinal.core.api.ChatApiService
import com.example.damprojectfinal.core.api.FollowApiService
import com.example.damprojectfinal.core.api.MenuItemApi
import com.example.damprojectfinal.core.api.OrderApiService
import com.example.damprojectfinal.core.api.TokenManager
import com.example.damprojectfinal.core.api.normalUser.UserApiService
import com.example.damprojectfinal.core.api.posts.PostsApiService
import com.example.damprojectfinal.core.api.posts.ReelsApiService
import com.example.damprojectfinal.core.api.professionalUser.ProfessionalApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // Use centralized BaseUrlProvider instead of hardcoded URL
    private val BASE_URL = BaseUrlProvider.BASE_URL_WITH_SLASH

    lateinit var appContext: Context // Declare appContext here

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        setLevel(HttpLoggingInterceptor.Level.BODY)
    }

    private val okHttpClient: OkHttpClient by lazy { // Change to 'lazy' to depend on appContext
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(AuthInterceptor(TokenManager(appContext))) // Pass TokenManager instance, using appContext
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

    val menuItemApi: MenuItemApi by lazy {
        retrofit.create(MenuItemApi::class.java)
    }

    val cartApi: CartApiService by lazy {
        retrofit.create(CartApiService::class.java)
    }

    val orderApi: OrderApiService by lazy {
        retrofit.create(OrderApiService::class.java)
    }

    val chatApiService: ChatApiService by lazy {
        retrofit.create(ChatApiService::class.java)
    }

    val followApiService: FollowApiService by lazy {
        retrofit.create(FollowApiService::class.java)
    }

}