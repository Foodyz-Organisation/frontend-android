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
    @SerializedName("notes") val notes: String? = null
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
    @SerializedName("calculatedPrice") val calculatedPrice: Double
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
    @SerializedName("calculatedPrice") val calculatedPrice: Double
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
