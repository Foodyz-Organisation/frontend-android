package com.example.damprojectfinal.core.dto.order

import com.google.gson.annotations.SerializedName

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
    @SerializedName("isDefault") val isDefault: Boolean
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
// RESPONSE: ORDER
// ---------------------------------------------------------
data class OrderResponse(
    @SerializedName("_id") val _id: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("professionalId") val professionalId: String,
    @SerializedName("orderType") val orderType: OrderType,
    @SerializedName("scheduledTime") val scheduledTime: String?,
    @SerializedName("items") val items: List<OrderItemResponse>,
    @SerializedName("totalPrice") val totalPrice: Double,
    @SerializedName("status") val status: OrderStatus,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String
)

data class OrderItemResponse(
    @SerializedName("menuItemId") val menuItemId: String,
    @SerializedName("name") val name: String,  // ⭐ ADDED for consistency
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("chosenIngredients") val chosenIngredients: List<ChosenIngredientResponse>?,
    @SerializedName("chosenOptions") val chosenOptions: List<ChosenOptionResponse>?,
    @SerializedName("calculatedPrice") val calculatedPrice: Double
)

data class ChosenIngredientResponse(
    @SerializedName("name") val name: String,
    @SerializedName("isDefault") val isDefault: Boolean
)

data class ChosenOptionResponse(
    @SerializedName("name") val name: String,
    @SerializedName("price") val price: Double
)
