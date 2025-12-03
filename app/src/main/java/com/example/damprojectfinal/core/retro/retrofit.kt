package com.example.damprojectfinal.core.retro

import com.example.damprojectfinal.core.api.CartApiService
import com.example.damprojectfinal.core.api.OrderApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.damprojectfinal.core.api.MenuItemApi

object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:3000/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
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
}
