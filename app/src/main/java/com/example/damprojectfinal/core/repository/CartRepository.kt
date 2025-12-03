package com.example.damprojectfinal.core.repository

import com.example.damprojectfinal.core.api.CartApiService
import com.example.damprojectfinal.core.api.TokenManager
import com.example.damprojectfinal.core.dto.cart.AddToCartRequest
import com.example.damprojectfinal.core.dto.cart.CartResponse
import com.example.damprojectfinal.core.dto.cart.UpdateQuantityRequest

class CartRepository(
    private val api: CartApiService,
    private val tokenManager: TokenManager
) {

    // -----------------------------
    // GET User Cart
    // -----------------------------
    suspend fun getUserCart(userId: String): CartResponse? {
        val token = tokenManager.getAccessTokenBlocking() ?: run {
            println("DEBUG: getUserCart() - No token available")
            return null
        }

        val response = api.getUserCart("Bearer $token", userId)
        println("DEBUG: getUserCart() - response code=${response.code()}, body=${response.body()}")
        return if (response.isSuccessful) response.body() else null
    }

    // -----------------------------
    // ADD Item to Cart
    // -----------------------------
    suspend fun addItemToCart(request: AddToCartRequest, userId: String): CartResponse? {
        val token = tokenManager.getAccessTokenBlocking() ?: run {
            println("DEBUG: addItemToCart() - No token available")
            return null
        }

        println("DEBUG: addItemToCart() - request = $request")
        println("DEBUG: addItemToCart() - token = $token, userId = $userId")

        val response = api.addItemToCart(request, "Bearer $token", userId)
        println("DEBUG: addItemToCart() - response code = ${response.code()}")
        println("DEBUG: addItemToCart() - response body = ${response.body()}")
        println("DEBUG: addItemToCart() - response errorBody = ${response.errorBody()?.string()}")

        return if (response.isSuccessful) response.body() else null
    }

    // -----------------------------
    // UPDATE Quantity
    // -----------------------------
    suspend fun updateItemQuantity(itemIndex: Int, quantity: Int, userId: String): CartResponse? {
        val token = tokenManager.getAccessTokenBlocking() ?: return null
        val request = UpdateQuantityRequest(quantity)
        val response = api.updateItemQuantity(itemIndex, request, "Bearer $token", userId)
        return if (response.isSuccessful) response.body() else null
    }

    // -----------------------------
    // REMOVE Item
    // -----------------------------
    suspend fun removeItem(itemIndex: Int, userId: String): CartResponse? {
        val token = tokenManager.getAccessTokenBlocking() ?: return null
        val response = api.removeItem(itemIndex, "Bearer $token", userId)
        return if (response.isSuccessful) response.body() else null
    }

    // -----------------------------
    // CLEAR Cart
    // -----------------------------
    suspend fun clearCart(userId: String): CartResponse? {
        val token = tokenManager.getAccessTokenBlocking() ?: return null
        val response = api.clearCart("Bearer $token", userId)
        return if (response.isSuccessful) response.body() else null
    }
}
