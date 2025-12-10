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
        android.util.Log.d("CartRepository", "📦 ========== addItemToCart() CALLED ==========")
        
        val token = tokenManager.getAccessTokenBlocking() ?: run {
            android.util.Log.e("CartRepository", "❌ No token available")
            return null
        }

        android.util.Log.d("CartRepository", "📝 Request details:")
        android.util.Log.d("CartRepository", "  - menuItemId: ${request.menuItemId}")
        android.util.Log.d("CartRepository", "  - name: ${request.name}")
        android.util.Log.d("CartRepository", "  - quantity: ${request.quantity}")
        android.util.Log.d("CartRepository", "  - calculatedPrice: ${request.calculatedPrice}")
        android.util.Log.d("CartRepository", "  - ingredients: ${request.chosenIngredients.size}")
        request.chosenIngredients.forEachIndexed { index, ing ->
            android.util.Log.d("CartRepository", "    [$index] ${ing.name} (default=${ing.isDefault}, type=${ing.intensityType}, value=${ing.intensityValue})")
        }
        android.util.Log.d("CartRepository", "  - options: ${request.chosenOptions.size}")
        request.chosenOptions.forEachIndexed { index, opt ->
            android.util.Log.d("CartRepository", "    [$index] ${opt.name} (+${opt.price} TND)")
        }
        android.util.Log.d("CartRepository", "🔑 Token: ${if (token.isNotEmpty()) "Present (${token.length} chars)" else "Empty"}")
        android.util.Log.d("CartRepository", "👤 UserId: $userId")

        android.util.Log.d("CartRepository", "📡 Making API call...")
        val response = api.addItemToCart(request, "Bearer $token", userId)
        
        android.util.Log.d("CartRepository", "📥 Response received:")
        android.util.Log.d("CartRepository", "  - Code: ${response.code()}")
        android.util.Log.d("CartRepository", "  - IsSuccessful: ${response.isSuccessful}")
        
        if (response.isSuccessful) {
            val cart = response.body()
            android.util.Log.d("CartRepository", "✅ Success! Cart items: ${cart?.items?.size ?: 0}")
            cart?.items?.forEachIndexed { index, item ->
                android.util.Log.d("CartRepository", "  Item $index: ${item.name} (qty=${item.quantity}, price=${item.calculatedPrice})")
            }
            return cart
        } else {
            val errorBody = response.errorBody()?.string()
            android.util.Log.e("CartRepository", "❌ API Error:")
            android.util.Log.e("CartRepository", "  - Code: ${response.code()}")
            android.util.Log.e("CartRepository", "  - Message: ${response.message()}")
            android.util.Log.e("CartRepository", "  - Error Body: $errorBody")
            return null
        }
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
