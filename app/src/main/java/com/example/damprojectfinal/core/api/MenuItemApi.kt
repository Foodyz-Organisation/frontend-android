package com.example.damprojectfinal.core.api

import com.example.damprojectfinal.core.dto.menu.MenuItemResponseDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface MenuItemApi {

    @Multipart
    @POST("menu-items")
    suspend fun createMenuItem(
        // 1. The JSON Payload Part (The DTO)
        // Key MUST be "createMenuItemDto" to match NestJS @Body() body.createMenuItemDto
        @Part("createMenuItemDto") menuItemDto: RequestBody,

        // 2. The File Part (The Image)
        // Key MUST be "image" to match NestJS FileInterceptor('image')
        @Part image: MultipartBody.Part,

        // Authorization header
        @Header("Authorization") token: String
    ): Response<MenuItemResponseDto> // Use 'suspend' for Kotlin Coroutines




}