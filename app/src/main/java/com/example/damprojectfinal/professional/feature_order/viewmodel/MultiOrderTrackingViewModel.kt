package com.example.damprojectfinal.professional.feature_order.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.damprojectfinal.core.api.AppConfig
import com.example.damprojectfinal.core.api.OrderTrackingSocketManager
import com.example.damprojectfinal.core.api.TokenManager
import com.example.damprojectfinal.core.dto.order.OrderResponse
import com.example.damprojectfinal.core.repository.OrderRepository
import com.example.damprojectfinal.core.retro.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Data class for a user sharing location
 */
data class SharingUser(
    val userId: String,
    val orderId: String,
    val userName: String,
    val lat: Double,
    val lng: Double,
    val accuracy: Double?,
    val distance: Double?,
    val distanceFormatted: String?,
    val timestamp: Long
)

/**
 * State for multi-order tracking
 */
data class MultiOrderTrackingState(
    val isConnected: Boolean = false,
    val activeUsers: Map<String, SharingUser> = emptyMap(), // userId -> SharingUser
    val restaurantLocation: RestaurantLocationData? = null,
    val error: String? = null
)

data class RestaurantLocationData(
    val lat: Double,
    val lng: Double,
    val name: String?,
    val address: String?
)

/**
 * ViewModel for tracking multiple users' locations simultaneously
 * Each user has one order, so userId -> orderId mapping is one-to-one
 */
class MultiOrderTrackingViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "MultiOrderTrackingVM"
    
    private val _state = MutableStateFlow(MultiOrderTrackingState())
    val state: StateFlow<MultiOrderTrackingState> = _state.asStateFlow()
    
    private var socketManager: OrderTrackingSocketManager? = null
    private val tokenManager = TokenManager(application.applicationContext)
    private val orderRepository = OrderRepository(RetrofitClient.orderApi, tokenManager)
    
    // Map userId -> orderId (one-to-one since each user has one order)
    private val userIdToOrderIdMap = mutableMapOf<String, String>()
    // Map orderId -> userId (reverse lookup)
    private val orderIdToUserIdMap = mutableMapOf<String, String>()
    // Map orderId -> userName
    private val orderIdToUserNameMap = mutableMapOf<String, String>()
    
    /**
     * Connect and join all order rooms for a professional
     */
    fun connectToAllOrders(professionalId: String) {
        viewModelScope.launch {
            try {
                // Disconnect existing connection if any
                if (socketManager != null) {
                    Log.d(TAG, "⚠️ Disconnecting existing socket before reconnecting")
                    disconnect()
                }
                
                // First, load all orders for this professional
                val orders = orderRepository.getOrdersByProfessional(professionalId)
                
                if (orders == null || orders.isEmpty()) {
                    _state.value = _state.value.copy(
                        error = "No orders found",
                        isConnected = false
                    )
                    Log.w(TAG, "⚠️ No orders found for professional: $professionalId")
                    return@launch
                }
                
                // Build userId -> orderId mapping (one-to-one)
                userIdToOrderIdMap.clear()
                orderIdToUserIdMap.clear()
                orderIdToUserNameMap.clear()
                
                orders.forEach { order ->
                    val userId = order.getUserId()
                    val orderId = order._id
                    val userName = order.getUserName()
                    
                    if (userId.isNotEmpty() && orderId.isNotEmpty()) {
                        userIdToOrderIdMap[userId] = orderId
                        orderIdToUserIdMap[orderId] = userId
                        orderIdToUserNameMap[orderId] = userName
                        Log.d(TAG, "📋 Mapped: userId=$userId -> orderId=$orderId, userName=$userName")
                    }
                }
                
                Log.d(TAG, "📊 Total orders mapped: ${userIdToOrderIdMap.size}")
                Log.d(TAG, "📋 Order IDs to join: ${orderIdToUserIdMap.keys.take(5)}")
                
                // Connect to socket
                val token = tokenManager.getAccessTokenAsync()
                if (token == null) {
                    _state.value = _state.value.copy(error = "No authentication token")
                    return@launch
                }
                
                val baseUrl = AppConfig.SOCKET_BASE_URL
                socketManager = OrderTrackingSocketManager(
                    baseUrl = baseUrl,
                    authToken = token,
                    socketPath = AppConfig.SOCKET_PATH
                )
                
                // Setup callbacks
                setupSocketCallbacks()
                
                // Connect
                socketManager?.connect()
                
                // Wait for connection, then join all order rooms
                // We'll join rooms when connected (handled in callback)
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error connecting to orders: ${e.message}", e)
                _state.value = _state.value.copy(error = "Failed to connect: ${e.message}")
            }
        }
    }
    
    private fun setupSocketCallbacks() {
        socketManager?.setOnConnectionChange { isConnected ->
            _state.value = _state.value.copy(isConnected = isConnected)
            
            if (isConnected) {
                Log.d(TAG, "✅ Connected to socket, joining ${userIdToOrderIdMap.size} order rooms...")
                Log.d(TAG, "📋 Available order IDs: ${orderIdToUserIdMap.keys}")
                
                // Join all order rooms
                userIdToOrderIdMap.values.forEach { orderId ->
                    socketManager?.joinOrder(orderId, "pro")
                    Log.d(TAG, "🔗 Emitted join-order for room: $orderId")
                }
                
                Log.d(TAG, "✅ Finished emitting join-order for all ${userIdToOrderIdMap.size} rooms")
            } else {
                Log.w(TAG, "❌ Socket disconnected")
            }
        }
        
        socketManager?.setOnRestaurantLocation { restaurantLoc ->
            _state.value = _state.value.copy(
                restaurantLocation = RestaurantLocationData(
                    lat = restaurantLoc.lat,
                    lng = restaurantLoc.lon,
                    name = restaurantLoc.name,
                    address = restaurantLoc.address
                )
            )
            Log.d(TAG, "🏪 Restaurant location: ${restaurantLoc.lat}, ${restaurantLoc.lon}")
        }
        
        socketManager?.setOnLocationUpdate { update ->
            val userId = update.userId
            Log.d(TAG, "📍 Received location-update event for userId: $userId")
            
            // Get orderId from mapping
            val orderId = userIdToOrderIdMap[userId]
            val userName = orderId?.let { orderIdToUserNameMap[it] } ?: "Customer"
            
            if (orderId != null) {
                val sharingUser = SharingUser(
                    userId = userId,
                    orderId = orderId,
                    userName = userName,
                    lat = update.lat,
                    lng = update.lng,
                    accuracy = update.accuracy,
                    distance = update.distance,
                    distanceFormatted = update.distanceFormatted,
                    timestamp = update.timestamp
                )
                
                // Update active users map
                val updatedUsers = _state.value.activeUsers.toMutableMap()
                updatedUsers[userId] = sharingUser
                
                _state.value = _state.value.copy(activeUsers = updatedUsers)
                
                Log.d(TAG, "✅ Location update processed: $userName (userId=$userId, orderId=$orderId) at ${update.lat}, ${update.lng}")
                Log.d(TAG, "📊 Total active users now: ${updatedUsers.size}")
            } else {
                Log.w(TAG, "⚠️ Received location update for unknown userId: $userId")
                Log.w(TAG, "📋 Available userIds in mapping: ${userIdToOrderIdMap.keys.take(5)}")
            }
        }
        
        socketManager?.setOnSharingStarted { userId ->
            Log.d(TAG, "✅ Sharing started event received for userId: $userId")
            val orderId = userIdToOrderIdMap[userId]
            val userName = orderId?.let { orderIdToUserNameMap[it] } ?: "Customer"
            Log.d(TAG, "  → User: $userName, Order: $orderId")
            // User will appear when we receive first location-update
        }
        
        socketManager?.setOnSharingStopped { userId ->
            Log.d(TAG, "⏹️ Sharing stopped for userId: $userId")
            
            // Remove user from active users
            val updatedUsers = _state.value.activeUsers.toMutableMap()
            updatedUsers.remove(userId)
            
            _state.value = _state.value.copy(activeUsers = updatedUsers)
        }
        
        socketManager?.setOnUserJoined { userType, clientId, userId, orderId ->
            Log.d(TAG, "👤 User joined event received: userType=$userType, clientId=$clientId, userId=$userId, orderId=$orderId")
            
            // If userType is "user", this is a customer joining
            // We need to identify which order they belong to
            val actualUserId = userId ?: clientId // Use userId if available, otherwise try clientId
            
            if (userType == "user" && actualUserId.isNotEmpty()) {
                // Find the orderId for this userId
                val userOrderId = orderId ?: userIdToOrderIdMap[actualUserId]
                
                if (userOrderId != null) {
                    val userName = orderIdToUserNameMap[userOrderId] ?: "Customer"
                    Log.d(TAG, "✅ User joined order room: $userName (userId=$actualUserId, orderId=$userOrderId)")
                    
                    // Note: User won't appear in activeUsers until they start sharing location
                    // But we can log that they've joined for debugging
                } else {
                    // Try to find userId in our mapping by checking if clientId matches any userId
                    val foundUserId = userIdToOrderIdMap.keys.find { it == clientId || it == actualUserId }
                    if (foundUserId != null) {
                        val foundOrderId = userIdToOrderIdMap[foundUserId]
                        val userName = foundOrderId?.let { orderIdToUserNameMap[it] } ?: "Customer"
                        Log.d(TAG, "✅ User joined (found via mapping): $userName (userId=$foundUserId, orderId=$foundOrderId)")
                    } else {
                        Log.w(TAG, "⚠️ User joined but couldn't identify: clientId=$clientId, userId=$userId")
                        Log.d(TAG, "📋 Available userIds in mapping: ${userIdToOrderIdMap.keys.take(5)}")
                    }
                }
            } else if (userType == "pro") {
                Log.d(TAG, "✅ Professional joined order room")
            }
        }
        
        socketManager?.setOnError { error ->
            // Filter out common reconnection errors that are handled automatically
            val isReconnectionError = error.contains("xhr poll error") || 
                                     error.contains("websocket error") ||
                                     error.contains("timeout")
            
            if (!isReconnectionError) {
                Log.e(TAG, "❌ Socket error: $error")
                _state.value = _state.value.copy(error = error)
            } else {
                Log.w(TAG, "⚠️ Reconnection error (will retry): $error")
            }
        }
    }
    
    /**
     * Refresh orders and join any new order rooms
     */
    fun refreshOrders(professionalId: String) {
        viewModelScope.launch {
            try {
                val orders = orderRepository.getOrdersByProfessional(professionalId)
                
                if (orders == null || orders.isEmpty()) {
                    Log.w(TAG, "⚠️ No orders found when refreshing")
                    return@launch
                }
                
                val newOrderIds = mutableSetOf<String>()
                
                orders.forEach { order ->
                    val userId = order.getUserId()
                    val orderId = order._id
                    val userName = order.getUserName()
                    
                    if (userId.isNotEmpty() && orderId.isNotEmpty()) {
                        // Check if this is a new order
                        if (!userIdToOrderIdMap.containsKey(userId)) {
                            newOrderIds.add(orderId)
                            Log.d(TAG, "🆕 Found new order: orderId=$orderId, userId=$userId")
                        }
                        
                        // Update mappings
                        userIdToOrderIdMap[userId] = orderId
                        orderIdToUserIdMap[orderId] = userId
                        orderIdToUserNameMap[orderId] = userName
                    }
                }
                
                // Join new order rooms if connected
                if (newOrderIds.isNotEmpty() && socketManager?.isConnected() == true) {
                    Log.d(TAG, "🔗 Joining ${newOrderIds.size} new order rooms...")
                    newOrderIds.forEach { orderId ->
                        socketManager?.joinOrder(orderId, "pro")
                        Log.d(TAG, "🔗 Joined new order room: $orderId")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error refreshing orders: ${e.message}", e)
            }
        }
    }
    
    /**
     * Disconnect from socket
     */
    fun disconnect() {
        socketManager?.disconnect()
        socketManager = null
        _state.value = MultiOrderTrackingState()
        Log.d(TAG, "🔌 Disconnected from socket")
    }
    
    override fun onCleared() {
        super.onCleared()
        disconnect()
    }
}

