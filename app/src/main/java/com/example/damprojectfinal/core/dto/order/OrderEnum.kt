package com.example.damprojectfinal.core.dto.order

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.google.gson.annotations.SerializedName

// -----------------------------
// Order Status
// -----------------------------
@Serializable
enum class OrderStatus {
    @SerialName("pending")
    @SerializedName("pending")
    PENDING,

    @SerialName("confirmed")
    @SerializedName("confirmed")
    CONFIRMED,

    @SerialName("completed")
    @SerializedName("completed")
    COMPLETED,

    @SerialName("cancelled")
    @SerializedName("cancelled")
    CANCELLED,

    @SerialName("refused")
    @SerializedName("refused")
    REFUSED
}

// -----------------------------
// Order Type
// -----------------------------
@Serializable
enum class OrderType {
    @SerialName("eat-in")
    @SerializedName("eat-in")
    EAT_IN,

    @SerialName("takeaway")
    @SerializedName("takeaway")
    TAKEAWAY,

    @SerialName("delivery")
    @SerializedName("delivery")
    DELIVERY
}
