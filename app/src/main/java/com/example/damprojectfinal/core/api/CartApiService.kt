package com.example.damprojectfinal.core.api

import com.example.damprojectfinal.core.dto.cart.AddToCartRequest
import com.example.damprojectfinal.core.dto.cart.CartResponse
import com.example.damprojectfinal.core.dto.cart.UpdateQuantityRequest
import retrofit2.Response
import retrofit2.http.*

interface CartApiService {

    // ---------------------------------------
    // GET: User Cart
    // ---------------------------------------
    @GET("cart")
    suspend fun getUserCart(
        @Header("Authorization") token: String,
        @Query("userId") userId: String  // Pass userId as query
    ): Response<CartResponse>

    // ---------------------------------------
    // POST: Add Item to Cart
    // ---------------------------------------
    @POST("cart/add")
    suspend fun addItemToCart(
        @Body request: AddToCartRequest,
        @Header("Authorization") token: String,
        @Query("userId") userId: String  // ✅ Add userId here
    ): Response<CartResponse>

    // ---------------------------------------
    // PATCH: Update Item Quantity
    // ---------------------------------------
    @PATCH("cart/update/{itemIndex}")
    suspend fun updateItemQuantity(
        @Path("itemIndex") itemIndex: Int,
        @Body request: UpdateQuantityRequest,
        @Header("Authorization") token: String,
        @Query("userId") userId: String  // Pass userId as query
    ): Response<CartResponse>

    // ---------------------------------------
    // DELETE: Remove Item from Cart
    // ---------------------------------------
    @DELETE("cart/remove/{itemIndex}")
    suspend fun removeItem(
        @Path("itemIndex") itemIndex: Int,
        @Header("Authorization") token: String,
        @Query("userId") userId: String  // Pass userId as query
    ): Response<CartResponse>

    // ---------------------------------------
    // DELETE: Clear Cart
    // ---------------------------------------
    @DELETE("cart/clear")
    suspend fun clearCart(
        @Header("Authorization") token: String,
        @Query("userId") userId: String  // Pass userId as query
    ): Response<CartResponse>
}
