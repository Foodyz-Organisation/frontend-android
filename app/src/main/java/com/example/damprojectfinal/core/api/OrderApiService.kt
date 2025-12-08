package com.example.damprojectfinal.core.api

import com.example.damprojectfinal.core.dto.order.CreateOrderRequest
import com.example.damprojectfinal.core.dto.order.OrderResponse
import com.example.damprojectfinal.core.dto.order.UpdateOrderStatusRequest
import retrofit2.Response
import retrofit2.http.*

interface OrderApiService {

    // CREATE ORDER
    @POST("orders")
    suspend fun createOrder(
        @Body body: CreateOrderRequest,
        @Header("Authorization") token: String
    ): Response<OrderResponse>

    // GET ORDERS BY USER
    @GET("orders/user/{userId}")
    suspend fun getOrdersByUser(
        @Path("userId") userId: String,
        @Header("Authorization") token: String
    ): Response<List<OrderResponse>>

    // GET ORDERS BY PROFESSIONAL
    @GET("orders/professional/{professionalId}")
    suspend fun getOrdersByProfessional(
        @Path("professionalId") professionalId: String,
        @Header("Authorization") token: String
    ): Response<List<OrderResponse>>

    // GET PENDING ORDERS
    @GET("orders/professional/{professionalId}/pending")
    suspend fun getPendingOrders(
        @Path("professionalId") professionalId: String,
        @Header("Authorization") token: String
    ): Response<List<OrderResponse>>

    // GET SINGLE ORDER BY ID
    @GET("orders/{orderId}")
    suspend fun getOrderById(
        @Path("orderId") orderId: String,
        @Header("Authorization") token: String
    ): Response<OrderResponse>

    // UPDATE ORDER STATUS
    @PATCH("orders/{orderId}/status")
    suspend fun updateOrderStatus(
        @Path("orderId") orderId: String,
        @Body body: UpdateOrderStatusRequest,
        @Header("Authorization") token: String
    ): Response<OrderResponse>

}
