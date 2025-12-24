package com.example.damprojectfinal.core.utils

import com.example.damprojectfinal.core.api.BaseUrlProvider
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit

object RetrofitInstance {
    // Use centralized BaseUrlProvider for automatic emulator/device detection
    private val BASE_URL = BaseUrlProvider.BASE_URL_WITH_SLASH

    private val json = Json {
        ignoreUnknownKeys = true
    }

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }
}
