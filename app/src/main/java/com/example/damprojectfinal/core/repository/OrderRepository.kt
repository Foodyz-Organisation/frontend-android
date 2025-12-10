package com.example.damprojectfinal.core.repository

import com.example.damprojectfinal.core.api.OrderApiService
import com.example.damprojectfinal.core.api.TokenManager
import com.example.damprojectfinal.core.dto.order.CreateOrderRequest
import com.example.damprojectfinal.core.dto.order.OrderResponse
import com.example.damprojectfinal.core.dto.order.UpdateOrderStatusRequest

class OrderRepository(
    private val api: OrderApiService,
    private val tokenManager: TokenManager
) {

    // -----------------------------------------------------
    // CREATE ORDER
    // -----------------------------------------------------
    suspend fun createOrder(request: CreateOrderRequest): OrderResponse? {
        val token = tokenManager.getAccessTokenBlocking() ?: run {
            android.util.Log.e("OrderRepository", "❌ createOrder() - No token available")
            return null
        }

        android.util.Log.d("OrderRepository", "📤 Creating order with ${request.items.size} items")
        request.items.forEachIndexed { itemIndex, item ->
            android.util.Log.d("OrderRepository", "  Item $itemIndex: ${item.name} (qty=${item.quantity})")
            android.util.Log.d("OrderRepository", "    Ingredients: ${item.chosenIngredients?.size ?: 0}")
            item.chosenIngredients?.forEach { ingredient ->
                android.util.Log.d("OrderRepository", "      - ${ingredient.name}: intensityValue=${ingredient.intensityValue}, type=${ingredient.intensityType}")
            }
        }

        val response = api.createOrder(request, "Bearer $token")

        android.util.Log.d("OrderRepository", "📥 Response code: ${response.code()}")
        
        if (response.isSuccessful) {
            val orderResponse = response.body()
            if (orderResponse != null) {
                android.util.Log.d("OrderRepository", "✅ Order created: ${orderResponse._id}")
                android.util.Log.d("OrderRepository", "  Items in response: ${orderResponse.items.size}")
                orderResponse.items.forEachIndexed { itemIndex, item ->
                    android.util.Log.d("OrderRepository", "    Item $itemIndex: ${item.name}")
                    android.util.Log.d("OrderRepository", "      Ingredients: ${item.chosenIngredients?.size ?: 0}")
                    item.chosenIngredients?.forEach { ingredient ->
                        android.util.Log.d("OrderRepository", "        - ${ingredient.name}: intensityValue=${ingredient.intensityValue}, type=${ingredient.intensityType}")
                    }
                }
            } else {
                android.util.Log.e("OrderRepository", "❌ Response body is null")
            }
            return orderResponse
        } else {
            val errorBody = response.errorBody()?.string()
            android.util.Log.e("OrderRepository", "❌ Order creation failed: ${response.code()}")
            android.util.Log.e("OrderRepository", "  Error body: $errorBody")
            return null
        }
    }


    // -----------------------------------------------------
    // GET ORDERS BY USER
    // -----------------------------------------------------
    suspend fun getOrdersByUser(userId: String): List<OrderResponse>? {
        val token = tokenManager.getAccessTokenBlocking() ?: run {
            println("DEBUG: getOrdersByUser() - No token available")
            return null
        }

        println("DEBUG: getOrdersByUser() - userId = $userId")

        val response = api.getOrdersByUser(userId, "Bearer $token")

        println("DEBUG: getOrdersByUser() - response code = ${response.code()}")
        println("DEBUG: getOrdersByUser() - response body = ${response.body()}")

        return if (response.isSuccessful) response.body() else null
    }


    // -----------------------------------------------------
    // GET ORDERS BY PROFESSIONAL
    // -----------------------------------------------------
    suspend fun getOrdersByProfessional(professionalId: String): List<OrderResponse>? {
        val token = tokenManager.getAccessTokenBlocking() ?: run {
            println("DEBUG: getOrdersByProfessional() - No token available")
            return null
        }

        println("DEBUG: getOrdersByProfessional() - professionalId = $professionalId")

        val response = api.getOrdersByProfessional(professionalId, "Bearer $token")

        println("DEBUG: getOrdersByProfessional() - response code = ${response.code()}")
        println("DEBUG: getOrdersByProfessional() - response body = ${response.body()}")

        return if (response.isSuccessful) response.body() else null
    }


    // -----------------------------------------------------
    // UPDATE ORDER STATUS
    // -----------------------------------------------------
    suspend fun updateOrderStatus(orderId: String, request: UpdateOrderStatusRequest): OrderResponse? {
        val token = tokenManager.getAccessTokenBlocking() ?: run {
            println("DEBUG: updateOrderStatus() - No token available")
            return null
        }

        println("DEBUG: updateOrderStatus() - orderId = $orderId")
        println("DEBUG: updateOrderStatus() - request = $request")

        val response = api.updateOrderStatus(orderId, request, "Bearer $token")

        println("DEBUG: updateOrderStatus() - response code = ${response.code()}")
        println("DEBUG: updateOrderStatus() - response body = ${response.body()}")
        println("DEBUG: updateOrderStatus() - error = ${response.errorBody()?.string()}")

        return if (response.isSuccessful) response.body() else null
    }


    // -----------------------------------------------------
    // GET PENDING ORDERS
    // -----------------------------------------------------
    suspend fun getPendingOrders(professionalId: String): List<OrderResponse>? {
        val token = tokenManager.getAccessTokenBlocking() ?: run {
            println("DEBUG: getPendingOrders() - No token available")
            return null
        }

        println("DEBUG: getPendingOrders() - professionalId = $professionalId")

        val response = api.getPendingOrders(professionalId, "Bearer $token")

        println("DEBUG: getPendingOrders() - response code = ${response.code()}")
        println("DEBUG: getPendingOrders() - response body = ${response.body()}")

        return if (response.isSuccessful) response.body() else null
    }


    // -----------------------------------------------------
    // GET ORDER BY ID
    // -----------------------------------------------------
    suspend fun getOrderById(orderId: String): OrderResponse? {
        val token = tokenManager.getAccessTokenBlocking() ?: run {
            println("DEBUG: getOrderById() - No token available")
            return null
        }

        println("DEBUG: getOrderById() - orderId = $orderId")

        val response = api.getOrderById(orderId, "Bearer $token")

        println("DEBUG: getOrderById() - response code = ${response.code()}")
        println("DEBUG: getOrderById() - response body = ${response.body()}")

        return if (response.isSuccessful) response.body() else null
    }

    // -----------------------------------------------------
    // DELETE SINGLE ORDER
    // -----------------------------------------------------
    suspend fun deleteOrder(orderId: String): Boolean {
        val token = tokenManager.getAccessTokenBlocking() ?: run {
            android.util.Log.e("OrderRepository", "❌ deleteOrder() - No token available")
            return false
        }

        android.util.Log.d("OrderRepository", "🗑️ Deleting order: $orderId")

        val response = api.deleteOrder(orderId, "Bearer $token")

        android.util.Log.d("OrderRepository", "📥 Delete response code: ${response.code()}")

        if (response.isSuccessful) {
            android.util.Log.d("OrderRepository", "✅ Order deleted successfully")
            return true
        } else {
            val errorBody = response.errorBody()?.string()
            android.util.Log.e("OrderRepository", "❌ Delete failed: ${response.code()}")
            android.util.Log.e("OrderRepository", "  Error body: $errorBody")
            return false
        }
    }

    // -----------------------------------------------------
    // DELETE ALL ORDERS FOR USER
    // -----------------------------------------------------
    suspend fun deleteAllOrdersByUser(userId: String): Boolean {
        val token = tokenManager.getAccessTokenBlocking() ?: run {
            android.util.Log.e("OrderRepository", "❌ deleteAllOrdersByUser() - No token available")
            return false
        }

        android.util.Log.d("OrderRepository", "🗑️ Deleting all orders for user: $userId")

        val response = api.deleteAllOrdersByUser(userId, "Bearer $token")

        android.util.Log.d("OrderRepository", "📥 Delete all response code: ${response.code()}")

        if (response.isSuccessful) {
            android.util.Log.d("OrderRepository", "✅ All orders deleted successfully")
            return true
        } else {
            val errorBody = response.errorBody()?.string()
            android.util.Log.e("OrderRepository", "❌ Delete all failed: ${response.code()}")
            android.util.Log.e("OrderRepository", "  Error body: $errorBody")
            return false
        }
    }

    // -----------------------------------------------------
    // DELETE ALL ORDERS FOR PROFESSIONAL
    // -----------------------------------------------------
    suspend fun deleteAllOrdersByProfessional(professionalId: String): Boolean {
        val token = tokenManager.getAccessTokenBlocking() ?: run {
            android.util.Log.e("OrderRepository", "❌ deleteAllOrdersByProfessional() - No token available")
            return false
        }

        android.util.Log.d("OrderRepository", "🗑️ Deleting all orders for professional: $professionalId")

        val response = api.deleteAllOrdersByProfessional(professionalId, "Bearer $token")

        android.util.Log.d("OrderRepository", "📥 Delete all response code: ${response.code()}")

        if (response.isSuccessful) {
            android.util.Log.d("OrderRepository", "✅ All orders deleted successfully")
            return true
        } else {
            val errorBody = response.errorBody()?.string()
            android.util.Log.e("OrderRepository", "❌ Delete all failed: ${response.code()}")
            android.util.Log.e("OrderRepository", "  Error body: $errorBody")
            return false
        }
    }
}
