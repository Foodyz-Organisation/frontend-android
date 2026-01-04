package com.example.damprojectfinal.core.dto.order

import com.google.gson.annotations.SerializedName
import com.google.gson.JsonElement

// ---------------------------------------------------------
// REQUEST: CREATE ORDER
// ---------------------------------------------------------
data class CreateOrderRequest(
    @SerializedName("userId") val userId: String,
    @SerializedName("professionalId") val professionalId: String,
    @SerializedName("orderType") val orderType: OrderType,
    @SerializedName("scheduledTime") val scheduledTime: String? = null,
    @SerializedName("items") val items: List<OrderItemRequest>,
    @SerializedName("totalPrice") val totalPrice: Double,
    @SerializedName("deliveryAddress") val deliveryAddress: String? = null,
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("comment") val comment: String? = null, // Special requests/comments
    @SerializedName("paymentMethod") val paymentMethod: String // "CASH" or "CARD"
)

// ---------------------------------------------------------
// REQUEST: ORDER ITEM
// ---------------------------------------------------------
data class OrderItemRequest(
    @SerializedName("menuItemId") val menuItemId: String,
    @SerializedName("name") val name: String,  // ⭐ ADDED - Required by backend
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("chosenIngredients") val chosenIngredients: List<ChosenIngredientRequest>? = null,
    @SerializedName("chosenOptions") val chosenOptions: List<ChosenOptionRequest>? = null,
    @SerializedName("calculatedPrice") val calculatedPrice: Double,
    
    // 🎯 NEW: Deal-related fields
    @SerializedName("originalPrice") val originalPrice: Double? = null, // Original price before discount
    @SerializedName("discountPercentage") val discountPercentage: Int? = null, // Discount percentage (0-100)
    @SerializedName("dealId") val dealId: String? = null // Deal ID if applicable
)

data class ChosenIngredientRequest(
    @SerializedName("name") val name: String,
    @SerializedName("isDefault") val isDefault: Boolean,
    @SerializedName("intensityType") val intensityType: com.example.damprojectfinal.core.dto.menu.IntensityType? = null,
    @SerializedName("intensityColor") val intensityColor: String? = null,
    @SerializedName("intensityValue") val intensityValue: Double? = null  // Changed to Double for JSON parsing consistency
)

data class ChosenOptionRequest(
    @SerializedName("name") val name: String,
    @SerializedName("price") val price: Double
)

// ---------------------------------------------------------
// REQUEST: UPDATE ORDER STATUS
// ---------------------------------------------------------
data class UpdateOrderStatusRequest(
    @SerializedName("status") val status: OrderStatus
)

// ---------------------------------------------------------
// OPTIONAL: UPDATE ORDER DATA
// ---------------------------------------------------------
data class UpdateOrderRequest(
    @SerializedName("items") val items: List<OrderItemRequest>? = null,
    @SerializedName("totalPrice") val totalPrice: Double? = null,
    @SerializedName("orderType") val orderType: OrderType? = null,
    @SerializedName("scheduledTime") val scheduledTime: String? = null
)

// ---------------------------------------------------------
// USER INFO (for populated userId)
// ---------------------------------------------------------
data class UserInfo(
    @SerializedName("_id") val _id: String,
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String?
)

// ---------------------------------------------------------
// RESPONSE: ORDER
// ---------------------------------------------------------
data class OrderResponse(
    @SerializedName("_id") val _id: String,
    @SerializedName("userId") val userId: JsonElement,  // Can be String or UserInfo object
    @SerializedName("professionalId") val professionalId: String,
    @SerializedName("orderType") val orderType: OrderType,
    @SerializedName("scheduledTime") val scheduledTime: String?,
    @SerializedName("items") val items: List<OrderItemResponse>,
    @SerializedName("totalPrice") val totalPrice: Double,
    @SerializedName("status") val status: OrderStatus,
    @SerializedName("paymentMethod") val paymentMethod: String? = null,  // "CASH" or "CARD"
    @SerializedName("paymentId") val paymentId: String? = null,  // Payment ID for CARD payments
    
    // ===== NEW: Time Estimation Fields (Gemini AI) =====
    @SerializedName("comment") val comment: String? = null,
    @SerializedName("basePreparationMinutes") val basePreparationMinutes: Int? = null,
    @SerializedName("estimatedPreparationMinutes") val estimatedPreparationMinutes: Int? = null,
    @SerializedName("queuePosition") val queuePosition: Int? = null,
    
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String
) {
    // Helper to get user ID as string
    fun getUserId(): String {
        return try {
            if (userId.isJsonObject) {
                userId.asJsonObject.get("_id")?.asString ?: ""
            } else {
                userId.asString
            }
        } catch (e: Exception) {
            println("ERROR getUserId: ${e.message}")
            ""
        }
    }
    
    // Helper to get user name
    fun getUserName(): String {
        return try {
            if (userId.isJsonObject) {
                val username = userId.asJsonObject.get("username")?.asString
                println("DEBUG: Extracted username = $username")
                username ?: "Customer"
            } else {
                println("DEBUG: userId is not object, it's: $userId")
                "Customer"
            }
        } catch (e: Exception) {
            println("ERROR getUserName: ${e.message}")
            "Customer"
        }
    }
}

data class OrderItemResponse(
    @SerializedName("menuItemId") val menuItemId: String,
    @SerializedName("name") val name: String,
    @SerializedName("image") val image: String?,  // Add image field
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("chosenIngredients") val chosenIngredients: List<ChosenIngredientResponse>?,
    @SerializedName("chosenOptions") val chosenOptions: List<ChosenOptionResponse>?,
    @SerializedName("calculatedPrice") val calculatedPrice: Double,
    
    // 🎯 NEW: Deal-related fields
    @SerializedName("originalPrice") val originalPrice: Double? = null, // Original price before discount
    @SerializedName("discountPercentage") val discountPercentage: Int? = null, // Discount percentage (0-100)
    @SerializedName("dealId") val dealId: String? = null // Deal ID if applicable
)

data class ChosenIngredientResponse(
    @SerializedName("name") val name: String,
    @SerializedName("isDefault") val isDefault: Boolean,
    @SerializedName("intensityType") val intensityType: com.example.damprojectfinal.core.dto.menu.IntensityType? = null,
    @SerializedName("intensityColor") val intensityColor: String? = null,
    @SerializedName("intensityValue") val intensityValue: Double? = null  // Changed to Double for JSON parsing
)

data class ChosenOptionResponse(
    @SerializedName("name") val name: String,
    @SerializedName("price") val price: Double
)

// ---------------------------------------------------------
// PAYMENT RESPONSE (for CARD payments)
// ---------------------------------------------------------
data class CreateOrderWithPaymentResponse(
    @SerializedName("order") val order: OrderResponse,
    @SerializedName("clientSecret") val clientSecret: String?,
    @SerializedName("paymentIntentId") val paymentIntentId: String?
)

// ---------------------------------------------------------
// PAYMENT CONFIRMATION REQUEST
// ---------------------------------------------------------
// ⭐ UPDATED: Now using Stripe Android SDK to create PaymentMethod client-side
// Android Stripe SDK creates PaymentMethod (pm_xxx), we send ONLY the ID to backend
// Card details NEVER touch our server (PCI-DSS compliant)
data class ConfirmPaymentRequest(
    @SerializedName("paymentIntentId") val paymentIntentId: String,
    @SerializedName("paymentMethodId") val paymentMethodId: String  // Real Stripe PaymentMethod ID (pm_xxx)
)

// ---------------------------------------------------------
// PAYMENT CONFIRMATION RESPONSE
// ---------------------------------------------------------
data class ConfirmPaymentResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("order") val order: OrderResponse
)
