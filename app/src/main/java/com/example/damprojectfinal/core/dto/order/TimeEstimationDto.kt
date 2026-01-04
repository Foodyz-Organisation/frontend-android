package com.example.damprojectfinal.core.dto.order

import com.google.gson.annotations.SerializedName

/**
 * Request DTO for estimating order preparation time
 * Used before order placement to show users expected wait time
 */
data class TimeEstimationRequest(
    @SerializedName("professionalId") 
    val professionalId: String,
    
    @SerializedName("items") 
    val items: List<EstimateItem>
) {
    data class EstimateItem(
        @SerializedName("menuItemId") 
        val menuItemId: String,
        
        @SerializedName("quantity") 
        val quantity: Int
    )
}

/**
 * Response DTO containing AI-calculated time estimation
 * Includes base time, queue position, and AI explanation
 */
data class TimeEstimationResponse(
    @SerializedName("estimatedMinutes") 
    val estimatedMinutes: Int,
    
    @SerializedName("baseMinutes") 
    val baseMinutes: Int,
    
    @SerializedName("queuePosition") 
    val queuePosition: Int,
    
    @SerializedName("currentQueueSize") 
    val currentQueueSize: Int,
    
    @SerializedName("explanation") 
    val explanation: String
)
