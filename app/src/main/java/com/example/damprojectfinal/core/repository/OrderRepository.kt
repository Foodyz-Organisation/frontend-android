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
            println("DEBUG: createOrder() - No token available")
            return null
        }

        println("DEBUG: createOrder() - request = $request")

        val response = api.createOrder(request, "Bearer $token")

        println("DEBUG: createOrder() - response code = ${response.code()}")
        println("DEBUG: createOrder() - body = ${response.body()}")
        println("DEBUG: createOrder() - error = ${response.errorBody()?.string()}")

        return if (response.isSuccessful) response.body() else null
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
}
