package com.example.damprojectfinal.core.api

import com.example.damprojectfinal.core.dto.order.CreateOrderRequest
import com.example.damprojectfinal.core.dto.order.OrderResponse
import com.example.damprojectfinal.core.dto.order.UpdateOrderStatusRequest
import com.example.damprojectfinal.core.dto.order.ConfirmPaymentRequest
import com.example.damprojectfinal.core.dto.order.ConfirmPaymentResponse
import com.example.damprojectfinal.core.dto.order.TimeEstimationRequest
import com.example.damprojectfinal.core.dto.order.TimeEstimationResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface OrderApiService {

    // CREATE ORDER
    @POST("orders")
    suspend fun createOrder(
        @Body body: CreateOrderRequest,
        @Header("Authorization") token: String
    ): Response<OrderResponse>

    // CREATE ORDER WITH PAYMENT (returns raw ResponseBody for CARD payments)
    @POST("orders")
    suspend fun createOrderRaw(
        @Body body: CreateOrderRequest,
        @Header("Authorization") token: String
    ): Response<ResponseBody>

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

    // DELETE SINGLE ORDER
    @DELETE("orders/{orderId}")
    suspend fun deleteOrder(
        @Path("orderId") orderId: String,
        @Header("Authorization") token: String
    ): Response<Unit>

    // DELETE ALL ORDERS FOR USER
    @DELETE("orders/user/{userId}")
    suspend fun deleteAllOrdersByUser(
        @Path("userId") userId: String,
        @Header("Authorization") token: String
    ): Response<Unit>

    // DELETE ALL ORDERS FOR PROFESSIONAL
    @DELETE("orders/professional/{professionalId}")
    suspend fun deleteAllOrdersByProfessional(
        @Path("professionalId") professionalId: String,
        @Header("Authorization") token: String
    ): Response<Unit>

    // CONFIRM CARD PAYMENT
    @POST("orders/payment/confirm")
    suspend fun confirmPayment(
        @Body body: ConfirmPaymentRequest,
        @Header("Authorization") token: String
    ): Response<ConfirmPaymentResponse>

    // ESTIMATE ORDER PREPARATION TIME (Gemini AI)
    @POST("orders/estimate-time")
    suspend fun estimateTime(
        @Body request: TimeEstimationRequest
    ): Response<TimeEstimationResponse>

}
