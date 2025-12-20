package com.example.damprojectfinal.core.repository

import com.example.damprojectfinal.core.api.OrderApiService
import com.example.damprojectfinal.core.api.TokenManager
import com.example.damprojectfinal.core.dto.order.CreateOrderRequest
import com.example.damprojectfinal.core.dto.order.OrderResponse
import com.example.damprojectfinal.core.dto.order.UpdateOrderStatusRequest
import com.example.damprojectfinal.core.dto.order.CreateOrderWithPaymentResponse
import com.example.damprojectfinal.core.dto.order.ConfirmPaymentRequest
import com.example.damprojectfinal.core.dto.order.ConfirmPaymentResponse
import com.google.gson.Gson
import com.google.gson.JsonObject

class OrderRepository(
    private val api: OrderApiService,
    private val tokenManager: TokenManager
) {

    // -----------------------------------------------------
    // CREATE ORDER
    // Returns OrderResponse for CASH, CreateOrderWithPaymentResponse for CARD
    // -----------------------------------------------------
    suspend fun createOrder(request: CreateOrderRequest): OrderResponse? {
        val token = tokenManager.getAccessTokenBlocking() ?: run {
            android.util.Log.e("OrderRepository", "❌ createOrder() - No token available")
            return null
        }

        android.util.Log.d("OrderRepository", "📤 Creating order with ${request.items.size} items, paymentMethod: ${request.paymentMethod}")
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
            // Retrofit already deserializes the response body, so we can use it directly
            val responseBody = response.body()
            if (responseBody != null) {
                android.util.Log.d("OrderRepository", "✅ Order created successfully")
                android.util.Log.d("OrderRepository", "  Order ID: ${responseBody._id}")
                android.util.Log.d("OrderRepository", "  Payment Method: ${responseBody.paymentMethod}")
                android.util.Log.d("OrderRepository", "  Items count: ${responseBody.items.size}")
                
                // For CASH payments, the response is already an OrderResponse
                // For CARD payments, we need to check if it has payment info
                // But since we're using createOrder for CASH, it should be direct OrderResponse
                return responseBody
            } else {
                android.util.Log.e("OrderRepository", "❌ Response body is null")
            }
            return null
        } else {
            val errorBody = response.errorBody()?.string()
            android.util.Log.e("OrderRepository", "❌ Order creation failed: ${response.code()}")
            android.util.Log.e("OrderRepository", "  Error body: $errorBody")
            return null
        }
    }
    
    // -----------------------------------------------------
    // CREATE ORDER WITH PAYMENT (returns payment info for CARD)
    // -----------------------------------------------------
    suspend fun createOrderWithPayment(request: CreateOrderRequest): CreateOrderWithPaymentResponse? {
        val token = tokenManager.getAccessTokenBlocking() ?: run {
            android.util.Log.e("OrderRepository", "❌ createOrderWithPayment() - No token available")
            return null
        }

        // Use raw response endpoint to get the full response body without Retrofit deserializing it
        val response = api.createOrderRaw(request, "Bearer $token")

        android.util.Log.d("OrderRepository", "📥 createOrderWithPayment response code: ${response.code()}")

        if (response.isSuccessful) {
            // Get raw response body as string - this works because we're using ResponseBody directly
            val responseBodyString = try {
                val responseBody = response.body()
                if (responseBody != null) {
                    val bodyString = responseBody.string()
                    android.util.Log.d("OrderRepository", "📏 Raw body length: ${bodyString.length} chars")
                    bodyString
                } else {
                    android.util.Log.e("OrderRepository", "❌ Response body is null")
                    null
                }
            } catch (e: Exception) {
                android.util.Log.e("OrderRepository", "❌ Error reading response body: ${e.message}", e)
                null
            }
            
            if (responseBodyString != null && responseBodyString.isNotBlank()) {
                android.util.Log.d("OrderRepository", "✅ Raw response body received")
                android.util.Log.d("OrderRepository", "📏 Response length: ${responseBodyString.length} chars")
                // Log first 200 chars to verify we got the full response
                android.util.Log.d("OrderRepository", "📋 Response preview: ${responseBodyString.take(200)}...")
                android.util.Log.d("OrderRepository", "✅ Response body received (raw string)")
                android.util.Log.d("OrderRepository", "📋 Full response: $responseBodyString")
                
                try {
                    // Parse as JSON object to check structure
                    val jsonObject = Gson().fromJson(responseBodyString, JsonObject::class.java)
                    
                    android.util.Log.d("OrderRepository", "🔍 Checking response structure...")
                    android.util.Log.d("OrderRepository", "  Has 'order' key: ${jsonObject.has("order")}")
                    android.util.Log.d("OrderRepository", "  Has 'clientSecret' key: ${jsonObject.has("clientSecret")}")
                    android.util.Log.d("OrderRepository", "  Has 'paymentIntentId' key: ${jsonObject.has("paymentIntentId")}")
                    
                    if (jsonObject.has("order") && jsonObject.has("clientSecret")) {
                        // CARD payment response with nested order structure
                        // Backend returns: { "order": {...}, "clientSecret": "...", "paymentIntentId": "..." }
                        android.util.Log.d("OrderRepository", "💳 CARD payment response (nested structure detected)")
                        
                        val paymentResponse = Gson().fromJson(responseBodyString, CreateOrderWithPaymentResponse::class.java)
                        
                        android.util.Log.d("OrderRepository", "✅ Parsed CreateOrderWithPaymentResponse")
                        android.util.Log.d("OrderRepository", "  Order ID: ${paymentResponse.order._id}")
                        android.util.Log.d("OrderRepository", "  Payment Method: ${paymentResponse.order.paymentMethod}")
                        android.util.Log.d("OrderRepository", "  Payment ID (MongoDB): ${paymentResponse.order.paymentId}")
                        android.util.Log.d("OrderRepository", "  PaymentIntentId (Stripe): ${paymentResponse.paymentIntentId}")
                        android.util.Log.d("OrderRepository", "  ClientSecret: ${paymentResponse.clientSecret?.take(20)}...")
                        
                        // CRITICAL: Log the difference between paymentId and paymentIntentId
                        android.util.Log.d("OrderRepository", "🔍 PAYMENT ID ANALYSIS:")
                        android.util.Log.d("OrderRepository", "  - paymentId (MongoDB): ${paymentResponse.order.paymentId} (used to link order to payment)")
                        android.util.Log.d("OrderRepository", "  - paymentIntentId (Stripe): ${paymentResponse.paymentIntentId} (used for Stripe payment confirmation)")
                        android.util.Log.d("OrderRepository", "  ⚠️ Backend confirmPayment expects paymentIntentId (Stripe), NOT paymentId (MongoDB)")
                        
                        return paymentResponse
                    } else {
                        // Try to parse as direct OrderResponse (for CASH or different structure)
                        android.util.Log.d("OrderRepository", "💵 Attempting to parse as direct OrderResponse")
                        val orderResponse = Gson().fromJson(responseBodyString, OrderResponse::class.java)
                        
                        if (orderResponse.paymentMethod == "CARD" && orderResponse.paymentId != null) {
                            // CARD payment - backend returns paymentId directly in OrderResponse
                            android.util.Log.d("OrderRepository", "💳 CARD payment detected from paymentMethod and paymentId")
                            android.util.Log.d("OrderRepository", "  Using paymentId as paymentIntentId: ${orderResponse.paymentId}")
                            return CreateOrderWithPaymentResponse(
                                order = orderResponse,
                                clientSecret = jsonObject.get("clientSecret")?.asString,
                                paymentIntentId = orderResponse.paymentId
                            )
                        } else {
                            // CASH payment - wrap in response format
                            android.util.Log.d("OrderRepository", "💵 CASH payment response - wrapping")
                            return CreateOrderWithPaymentResponse(
                                order = orderResponse,
                                clientSecret = null,
                                paymentIntentId = null
                            )
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("OrderRepository", "❌ Error parsing response: ${e.message}", e)
                    android.util.Log.e("OrderRepository", "  Response string: $responseBodyString")
                    return null
                }
            } else {
                android.util.Log.e("OrderRepository", "❌ Response body is null")
            }
        } else {
            val errorBody = response.errorBody()?.string()
            android.util.Log.e("OrderRepository", "❌ createOrderWithPayment failed: ${response.code()}")
            android.util.Log.e("OrderRepository", "  Error body: $errorBody")
        }
        return null
    }
    
    // -----------------------------------------------------
    // CONFIRM CARD PAYMENT
    // -----------------------------------------------------
    // -----------------------------------------------------
    // CONFIRM CARD PAYMENT WITH CARD DETAILS
    // Backend requires paymentMethodId, but we don't have Stripe SDK
    // Solution: Send a placeholder paymentMethodId and let backend handle it
    // OR: Backend needs to be updated to accept card details and create PaymentMethod server-side
    // -----------------------------------------------------
    suspend fun confirmPaymentWithCardDetails(
        paymentIntentId: String,
        cardNumber: String,
        expMonth: Int,
        expYear: Int,
        cvv: String,
        cardholderName: String
    ): ConfirmPaymentResponse? {
        val token = tokenManager.getAccessTokenBlocking() ?: run {
            android.util.Log.e("OrderRepository", "❌ confirmPaymentWithCardDetails() - No token available")
            return null
        }

        android.util.Log.d("OrderRepository", "💳 ========== PAYMENT CONFIRMATION WITH CARD DETAILS ==========")
        android.util.Log.d("OrderRepository", "📤 Sending payment confirmation to backend")
        android.util.Log.d("OrderRepository", "  Endpoint: POST /orders/payment/confirm")
        android.util.Log.d("OrderRepository", "  PaymentIntentId (Stripe): $paymentIntentId")
        android.util.Log.d("OrderRepository", "  Card Number: ${cardNumber.take(4)}****${cardNumber.takeLast(4)}")
        android.util.Log.d("OrderRepository", "  Expiry: $expMonth/$expYear")
        android.util.Log.d("OrderRepository", "  CVV: ${cvv.length} digits")
        android.util.Log.d("OrderRepository", "  Cardholder: $cardholderName")
        
        // Validate paymentIntentId format
        if (!paymentIntentId.startsWith("pi_")) {
            android.util.Log.w("OrderRepository", "⚠️ WARNING: paymentIntentId doesn't start with 'pi_'")
            android.util.Log.w("OrderRepository", "  This might be a MongoDB paymentId instead of Stripe paymentIntentId")
        }

        // ⚠️ CURRENT BACKEND LIMITATION:
        // Backend validation ONLY accepts: paymentIntentId and paymentMethodId
        // Backend REJECTS: cardNumber, cardholderName, cvv, expMonth, expYear
        //
        // Since Android app doesn't have Stripe SDK to create PaymentMethod client-side,
        // we need backend to accept card details and create PaymentMethod server-side.
        //
        // TEMPORARY WORKAROUND: Send a placeholder paymentMethodId
        // Backend needs to be updated (see backend code requirements below)
        
        // For now, we'll use a test PaymentMethod ID format
        // Backend should recognize this and create PaymentMethod from card details
        // OR backend should be updated to accept card details in the request
        val placeholderPaymentMethodId = "pm_android_${System.currentTimeMillis()}"
        
        android.util.Log.w("OrderRepository", "⚠️ TEMPORARY: Using placeholder paymentMethodId: $placeholderPaymentMethodId")
        android.util.Log.w("OrderRepository", "  Card details collected but NOT sent (backend rejects them)")
        android.util.Log.w("OrderRepository", "")
        android.util.Log.w("OrderRepository", "📋 BACKEND UPDATE REQUIRED:")
        android.util.Log.w("OrderRepository", "  The backend DTO validation needs to accept card details:")
        android.util.Log.w("OrderRepository", "  - cardNumber (string)")
        android.util.Log.w("OrderRepository", "  - expMonth (number)")
        android.util.Log.w("OrderRepository", "  - expYear (number)")
        android.util.Log.w("OrderRepository", "  - cvv (string)")
        android.util.Log.w("OrderRepository", "  - cardholderName (string, optional)")
        android.util.Log.w("OrderRepository", "")
        android.util.Log.w("OrderRepository", "  Then backend should create PaymentMethod server-side:")
        android.util.Log.w("OrderRepository", "  const paymentMethod = await stripe.paymentMethods.create({")
        android.util.Log.w("OrderRepository", "    type: 'card',")
        android.util.Log.w("OrderRepository", "    card: {")
        android.util.Log.w("OrderRepository", "      number: cardNumber,")
        android.util.Log.w("OrderRepository", "      exp_month: expMonth,")
        android.util.Log.w("OrderRepository", "      exp_year: expYear,")
        android.util.Log.w("OrderRepository", "      cvc: cvv")
        android.util.Log.w("OrderRepository", "    }")
        android.util.Log.w("OrderRepository", "  });")

        val request = ConfirmPaymentRequest(
            paymentIntentId = paymentIntentId,
            paymentMethodId = placeholderPaymentMethodId  // Required by current backend validation
        )

        android.util.Log.d("OrderRepository", "📋 Request body:")
        android.util.Log.d("OrderRepository", "  {")
        android.util.Log.d("OrderRepository", "    \"paymentIntentId\": \"$paymentIntentId\",")
        android.util.Log.d("OrderRepository", "    \"paymentMethodId\": \"$placeholderPaymentMethodId\"")
        android.util.Log.d("OrderRepository", "  }")
        android.util.Log.d("OrderRepository", "")
        android.util.Log.d("OrderRepository", "📝 NOTE: Card details are NOT sent (backend rejects them)")
        android.util.Log.d("OrderRepository", "  Backend needs to be updated to accept card details")
        android.util.Log.d("OrderRepository", "  OR create PaymentMethod from card details server-side")
        
        android.util.Log.d("OrderRepository", "📤 Sending HTTP request...")
        val response = api.confirmPayment(request, "Bearer $token")

        android.util.Log.d("OrderRepository", "📥 Payment confirmation response code: ${response.code()}")
        
        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string()
            android.util.Log.e("OrderRepository", "❌ Payment confirmation failed: ${response.code()}")
            android.util.Log.e("OrderRepository", "  Error body: $errorBody")
            android.util.Log.e("OrderRepository", "")
            android.util.Log.e("OrderRepository", "🔴 BACKEND UPDATE REQUIRED:")
            android.util.Log.e("OrderRepository", "  The backend endpoint /orders/payment/confirm needs to:")
            android.util.Log.e("OrderRepository", "  1. Accept card details (cardNumber, expMonth, expYear, cvv, cardholderName)")
            android.util.Log.e("OrderRepository", "  2. Create PaymentMethod from card details using Stripe API:")
            android.util.Log.e("OrderRepository", "     const paymentMethod = await stripe.paymentMethods.create({")
            android.util.Log.e("OrderRepository", "       type: 'card',")
            android.util.Log.e("OrderRepository", "       card: { number, exp_month, exp_year, cvc }")
            android.util.Log.e("OrderRepository", "     });")
            android.util.Log.e("OrderRepository", "  3. Then confirm payment with: paymentIntent.confirm({ payment_method: paymentMethod.id })")
        } else {
            android.util.Log.d("OrderRepository", "✅ Payment confirmation successful!")
        }
        android.util.Log.d("OrderRepository", "================================================")

        if (response.isSuccessful) {
            val confirmResponse = response.body()
            if (confirmResponse != null) {
                android.util.Log.d("OrderRepository", "✅ Payment confirmed successfully")
                return confirmResponse
            } else {
                android.util.Log.e("OrderRepository", "❌ Payment confirmation response body is null")
            }
        } else {
            val errorBody = response.errorBody()?.string()
            android.util.Log.e("OrderRepository", "❌ Payment confirmation failed: ${response.code()}")
            android.util.Log.e("OrderRepository", "  Error body: $errorBody")
        }
        return null
    }
    
    // -----------------------------------------------------
    // CONFIRM CARD PAYMENT (legacy - with PaymentMethod ID)
    // -----------------------------------------------------
    suspend fun confirmPayment(paymentIntentId: String, paymentMethodId: String): ConfirmPaymentResponse? {
        val token = tokenManager.getAccessTokenBlocking() ?: run {
            android.util.Log.e("OrderRepository", "❌ confirmPayment() - No token available")
            return null
        }

        android.util.Log.d("OrderRepository", "💳 ========== PAYMENT CONFIRMATION REQUEST ==========")
        android.util.Log.d("OrderRepository", "📤 Sending payment confirmation to backend")
        android.util.Log.d("OrderRepository", "  Endpoint: POST /orders/payment/confirm")
        android.util.Log.d("OrderRepository", "  PaymentIntentId (Stripe): $paymentIntentId")
        android.util.Log.d("OrderRepository", "  PaymentMethodId: $paymentMethodId")
        android.util.Log.d("OrderRepository", "  ⚠️ Backend expects Stripe paymentIntentId (starts with 'pi_')")
        android.util.Log.d("OrderRepository", "  ⚠️ NOT MongoDB paymentId (24-char hex string)")
        
        // Validate paymentIntentId format
        if (!paymentIntentId.startsWith("pi_")) {
            android.util.Log.w("OrderRepository", "⚠️ WARNING: paymentIntentId doesn't start with 'pi_'")
            android.util.Log.w("OrderRepository", "  This might be a MongoDB paymentId instead of Stripe paymentIntentId")
            android.util.Log.w("OrderRepository", "  Backend will likely return 404 'Payment not found'")
        }
        
        // Validate paymentMethodId format
        if (paymentMethodId != null) {
            if (paymentMethodId.contains("_fake") || paymentMethodId.contains("test_")) {
                android.util.Log.w("OrderRepository", "⚠️ WARNING: PaymentMethodId appears to be fake/test: $paymentMethodId")
                android.util.Log.w("OrderRepository", "  Backend will try to use this with Stripe, which may fail")
                android.util.Log.w("OrderRepository", "  For fake payments, backend should create PaymentMethod from card details")
            } else if (!paymentMethodId.startsWith("pm_")) {
                android.util.Log.w("OrderRepository", "⚠️ WARNING: PaymentMethodId doesn't start with 'pm_': $paymentMethodId")
            }
        }

        val request = ConfirmPaymentRequest(
            paymentIntentId = paymentIntentId,
            paymentMethodId = paymentMethodId
        )

        android.util.Log.d("OrderRepository", "📋 Request body:")
        android.util.Log.d("OrderRepository", "  {")
        android.util.Log.d("OrderRepository", "    \"paymentIntentId\": \"$paymentIntentId\",")
        android.util.Log.d("OrderRepository", "    \"paymentMethodId\": \"$paymentMethodId\"")
        android.util.Log.d("OrderRepository", "  }")
        
        android.util.Log.d("OrderRepository", "📤 Sending HTTP request...")
        val response = api.confirmPayment(request, "Bearer $token")

        android.util.Log.d("OrderRepository", "📥 Payment confirmation response code: ${response.code()}")
        
        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string()
            android.util.Log.e("OrderRepository", "❌ Payment confirmation failed: ${response.code()}")
            android.util.Log.e("OrderRepository", "  Error body: $errorBody")
            android.util.Log.e("OrderRepository", "  PaymentIntentId sent: $paymentIntentId")
            android.util.Log.e("OrderRepository", "  PaymentMethodId sent: $paymentMethodId")
            
            // Detailed error explanation
            if (response.code() == 500 && errorBody?.contains("No such PaymentMethod") == true) {
                android.util.Log.e("OrderRepository", "")
                android.util.Log.e("OrderRepository", "🔴 ERROR EXPLANATION:")
                android.util.Log.e("OrderRepository", "  The backend tried to use a FAKE PaymentMethod ID with Stripe.")
                android.util.Log.e("OrderRepository", "  Stripe rejected it because the PaymentMethod doesn't exist.")
                android.util.Log.e("OrderRepository", "")
                android.util.Log.e("OrderRepository", "  ❌ Problem:")
                android.util.Log.e("OrderRepository", "    - We sent: '$paymentMethodId' (fake ID)")
                android.util.Log.e("OrderRepository", "    - Backend tried to use it with Stripe")
                android.util.Log.e("OrderRepository", "    - Stripe said: 'No such PaymentMethod'")
                android.util.Log.e("OrderRepository", "")
                android.util.Log.e("OrderRepository", "  ✅ Solution:")
                android.util.Log.e("OrderRepository", "    The backend needs to CREATE a PaymentMethod from card details")
                android.util.Log.e("OrderRepository", "    BEFORE trying to confirm the payment with Stripe.")
                android.util.Log.e("OrderRepository", "")
                android.util.Log.e("OrderRepository", "  📋 Backend should:")
                android.util.Log.e("OrderRepository", "    1. Receive card details (number, expiry, CVV) OR")
                android.util.Log.e("OrderRepository", "    2. Create PaymentMethod from card details using Stripe API")
                android.util.Log.e("OrderRepository", "    3. Then attach it to PaymentIntent and confirm")
                android.util.Log.e("OrderRepository", "")
                android.util.Log.e("OrderRepository", "  ⚠️ Current flow (WRONG):")
                android.util.Log.e("OrderRepository", "    Frontend → Fake PaymentMethod ID → Backend → Stripe ❌")
                android.util.Log.e("OrderRepository", "")
                android.util.Log.e("OrderRepository", "  ✅ Correct flow:")
                android.util.Log.e("OrderRepository", "    Frontend → Card Details → Backend → Create PaymentMethod → Stripe ✅")
            } else if (response.code() == 404) {
                android.util.Log.e("OrderRepository", "  ⚠️ If 404 'Payment not found', check if paymentIntentId is correct")
                android.util.Log.e("OrderRepository", "  ⚠️ Backend looks up payment by Stripe paymentIntentId, not MongoDB paymentId")
            }
        } else {
            android.util.Log.d("OrderRepository", "✅ Payment confirmation successful!")
        }
        android.util.Log.d("OrderRepository", "================================================")

        if (response.isSuccessful) {
            val confirmResponse = response.body()
            if (confirmResponse != null) {
                android.util.Log.d("OrderRepository", "✅ Payment confirmed successfully")
                return confirmResponse
            } else {
                android.util.Log.e("OrderRepository", "❌ Payment confirmation response body is null")
            }
        } else {
            val errorBody = response.errorBody()?.string()
            android.util.Log.e("OrderRepository", "❌ Payment confirmation failed: ${response.code()}")
            android.util.Log.e("OrderRepository", "  Error body: $errorBody")
        }
        return null
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
