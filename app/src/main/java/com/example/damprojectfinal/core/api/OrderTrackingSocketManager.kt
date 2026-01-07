package com.example.damprojectfinal.core.api

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject

/**
 * WebSocket Manager for Order Tracking (separate from Chat)
 * Connects to the 'order-tracking' namespace
 */
class OrderTrackingSocketManager(
    private val baseUrl: String,   // ex: "http://10.0.2.2:3000"
    private val authToken: String, // JWT
    private val socketPath: String = "/socket.io"
) {
    private var socket: Socket? = null
    private val TAG = "OrderTrackingSocket"

    // Callbacks
    private var onLocationUpdate: ((LocationUpdate) -> Unit)? = null
    private var onRestaurantLocation: ((RestaurantLocation) -> Unit)? = null
    private var onSharingStarted: ((String) -> Unit)? = null
    private var onSharingStopped: ((String) -> Unit)? = null
    private var onUserJoined: ((String, String, String?, String?) -> Unit)? = null // userType, clientId, userId?, orderId?
    private var onConnectionChange: ((Boolean) -> Unit)? = null
    private var onError: ((String) -> Unit)? = null

    data class LocationUpdate(
        val userId: String,
        val lat: Double,
        val lng: Double,
        val accuracy: Double?,
        val timestamp: Long,
        val distance: Double? = null, // Distance in kilometers
        val distanceFormatted: String? = null, // Formatted string like "2.5 km" or "150 m"
        val restaurantLocation: RestaurantLocation? = null
    )

    data class RestaurantLocation(
        val lat: Double,
        val lon: Double,
        val name: String?,
        val address: String?
    )

    fun connect() {
        try {
            // Disconnect existing connection if any
            if (socket != null && socket?.connected() == true) {
                Log.d(TAG, "⚠️ Disconnecting existing socket before reconnecting")
                socket?.disconnect()
                socket = null
            }
            
            val fullUrl = "$baseUrl/order-tracking"
            Log.d(TAG, "🔌 Connecting to order tracking socket...")
            Log.d(TAG, "  Base URL: $baseUrl")
            Log.d(TAG, "  Full URL: $fullUrl")
            Log.d(TAG, "  Socket Path: $socketPath")
            Log.d(TAG, "  Has Auth Token: ${authToken.isNotEmpty()}")
            
            val options = IO.Options.builder()
                .setAuth(mapOf("token" to authToken))
                .setPath(socketPath)
                .setReconnection(true)
                .setReconnectionDelay(1000)
                .setReconnectionDelayMax(10000) // Increased max delay
                .setReconnectionAttempts(Integer.MAX_VALUE)
                .setTimeout(20000) // 20 second timeout
                .setTransports(arrayOf("websocket", "polling")) // Prefer websocket, fallback to polling
                .build()

            // Connect to the 'order-tracking' namespace
            socket = IO.socket(fullUrl, options)
            Log.d(TAG, "📡 Socket instance created, calling connect()...")

            socket?.on(Socket.EVENT_CONNECT) {
                Log.d(TAG, "✅ ✅ ✅ CONNECTED to order tracking server")
                Log.d(TAG, "  Socket ID: ${socket?.id()}")
                onConnectionChange?.invoke(true)
            }

            socket?.on(Socket.EVENT_DISCONNECT) { args ->
                val reason = args.getOrNull(0)?.toString() ?: "unknown"
                Log.d(TAG, "❌ ❌ ❌ DISCONNECTED from order tracking server")
                Log.d(TAG, "  Reason: $reason")
                onConnectionChange?.invoke(false)
            }

            // Listen for restaurant location (sent when joining order room)
            socket?.on("restaurant-location") { args ->
                try {
                    val jsonObj = args[0] as? JSONObject
                    if (jsonObj != null) {
                        val restaurantLocation = RestaurantLocation(
                            lat = jsonObj.getDouble("lat"),
                            lon = jsonObj.getDouble("lon"),
                            name = if (jsonObj.has("name") && !jsonObj.isNull("name")) {
                                jsonObj.getString("name")
                            } else null,
                            address = if (jsonObj.has("address") && !jsonObj.isNull("address")) {
                                jsonObj.getString("address")
                            } else null
                        )
                        Log.d(TAG, "📍 Restaurant location received: ${restaurantLocation.lat}, ${restaurantLocation.lon}")
                        onRestaurantLocation?.invoke(restaurantLocation)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing restaurant location", e)
                    onError?.invoke("Failed to parse restaurant location: ${e.message}")
                }
            }

            // Listen for location updates
            socket?.on("location-update") { args ->
                try {
                    val rawData = args[0]?.toString() ?: "null"
                    Log.d(TAG, "📥 RECEIVED location-update event: $rawData")
                    
                    val jsonObj = args[0] as? JSONObject
                    if (jsonObj != null) {
                        // Parse restaurant location if present
                        val restaurantLocationObj = if (jsonObj.has("restaurantLocation") && !jsonObj.isNull("restaurantLocation")) {
                            val restLocObj = jsonObj.getJSONObject("restaurantLocation")
                            RestaurantLocation(
                                lat = restLocObj.getDouble("lat"),
                                lon = restLocObj.getDouble("lon"),
                                name = if (restLocObj.has("name") && !restLocObj.isNull("name")) {
                                    restLocObj.getString("name")
                                } else null,
                                address = null
                            )
                        } else null

                        val update = LocationUpdate(
                            userId = if (jsonObj.has("userId")) jsonObj.getString("userId") else "",
                            lat = if (jsonObj.has("lat")) jsonObj.getDouble("lat") else 0.0,
                            lng = if (jsonObj.has("lng")) jsonObj.getDouble("lng") else 0.0,
                            accuracy = if (jsonObj.has("accuracy") && !jsonObj.isNull("accuracy")) {
                                jsonObj.getDouble("accuracy")
                            } else null,
                            timestamp = if (jsonObj.has("timestamp")) jsonObj.getLong("timestamp") else System.currentTimeMillis(),
                            distance = if (jsonObj.has("distance") && !jsonObj.isNull("distance")) {
                                jsonObj.getDouble("distance")
                            } else null,
                            distanceFormatted = if (jsonObj.has("distanceFormatted") && !jsonObj.isNull("distanceFormatted")) {
                                jsonObj.getString("distanceFormatted")
                            } else null,
                            restaurantLocation = restaurantLocationObj
                        )
                        onLocationUpdate?.invoke(update)
                    } else {
                        Log.e(TAG, "❌ Received location-update but payload is not JSONObject: ${args[0]?.javaClass?.name}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing location update", e)
                    onError?.invoke("Failed to parse location update: ${e.message}")
                }
            }

            // Listen for sharing started
            socket?.on("sharing-started") { args ->
                try {
                    val jsonObj = args[0] as? JSONObject
                    val userId = jsonObj?.getString("userId") ?: ""
                    onSharingStarted?.invoke(userId)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing sharing-started", e)
                }
            }

            // Listen for sharing stopped
            socket?.on("sharing-stopped") { args ->
                try {
                    val jsonObj = args[0] as? JSONObject
                    val userId = jsonObj?.getString("userId") ?: ""
                    onSharingStopped?.invoke(userId)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing sharing-stopped", e)
                }
            }

            // Listen for user joined
            socket?.on("user-joined") { args ->
                try {
                    val jsonObj = args[0] as? JSONObject
                    val userType = jsonObj?.getString("userType") ?: ""
                    val clientId = jsonObj?.getString("clientId") ?: ""
                    // Also try to extract userId and orderId if present
                    val userId = if (jsonObj?.has("userId") == true) jsonObj.getString("userId") else null
                    val orderId = if (jsonObj?.has("orderId") == true) jsonObj.getString("orderId") else null
                    onUserJoined?.invoke(userType, clientId, userId, orderId)
                    Log.d(TAG, "User joined: userType=$userType, clientId=$clientId, userId=$userId, orderId=$orderId")
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing user-joined", e)
                }
            }

            socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                val error = args[0]?.toString() ?: "Unknown error"
                Log.e(TAG, "Connection error: $error")
                // Don't treat reconnection attempts as errors - they're handled automatically
                // The socket will automatically reconnect based on the options we set
                if (!error.contains("xhr poll error") && !error.contains("websocket error")) {
                    // Only show non-transport errors as fatal
                    if (socket?.connected() == false) {
                        onError?.invoke("Connection error: $error")
                    }
                } else {
                    Log.d(TAG, "🔄 Transport error (reconnection will be automatic): $error")
                }
            }

            socket?.connect()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create socket connection", e)
            onError?.invoke("Socket initialization failed: ${e.message}")
        }
    }

    fun disconnect() {
        socket?.disconnect()
        socket = null
        Log.d(TAG, "Disconnected from order tracking")
    }

    fun isConnected(): Boolean {
        return socket?.connected() == true
    }

    /**
     * Join an order room
     */
    fun joinOrder(orderId: String, userType: String) {
        try {
            if (socket == null || socket?.connected() != true) {
                Log.w(TAG, "⚠️ Cannot join order room: Socket not connected")
                return
            }
            
            val payload = JSONObject().apply {
                put("orderId", orderId)
                put("userType", userType) // "user" or "pro"
            }
            socket?.emit("join-order", payload)
            Log.d(TAG, "📤 Emitted join-order: orderId=$orderId, userType=$userType")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error joining order", e)
            onError?.invoke("Failed to join order: ${e.message}")
        }
    }

    /**
     * Start sharing location
     */
    fun startSharing(orderId: String, userId: String) {
        try {
            val payload = JSONObject().apply {
                put("orderId", orderId)
                put("userId", userId)
            }
            socket?.emit("start-sharing", payload)
            Log.d(TAG, "Started sharing location for order: $orderId")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting sharing", e)
            onError?.invoke("Failed to start sharing: ${e.message}")
        }
    }

    /**
     * Stop sharing location
     */
    fun stopSharing(orderId: String, userId: String) {
        try {
            val payload = JSONObject().apply {
                put("orderId", orderId)
                put("userId", userId)
            }
            socket?.emit("stop-sharing", payload)
            Log.d(TAG, "Stopped sharing location for order: $orderId")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping sharing", e)
            onError?.invoke("Failed to stop sharing: ${e.message}")
        }
    }

    /**
     * Send location update
     */
    fun sendLocationUpdate(
        orderId: String,
        userId: String,
        lat: Double,
        lng: Double,
        accuracy: Double? = null
    ) {
        try {
            val payload = JSONObject().apply {
                put("orderId", orderId)
                put("userId", userId)
                put("lat", lat)
                put("lng", lng)
                if (accuracy != null) {
                    put("accuracy", accuracy)
                }
            }
            socket?.emit("location-update", payload)
            Log.d(TAG, "Sent location update: lat=$lat, lng=$lng")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending location update", e)
            onError?.invoke("Failed to send location: ${e.message}")
        }
    }

    // Callback setters
    fun setOnLocationUpdate(listener: (LocationUpdate) -> Unit) {
        onLocationUpdate = listener
    }

    fun setOnRestaurantLocation(listener: (RestaurantLocation) -> Unit) {
        onRestaurantLocation = listener
    }

    fun setOnSharingStarted(listener: (String) -> Unit) {
        onSharingStarted = listener
    }

    fun setOnSharingStopped(listener: (String) -> Unit) {
        onSharingStopped = listener
    }

    fun setOnUserJoined(listener: (String, String, String?, String?) -> Unit) {
        onUserJoined = listener
    }

    fun setOnConnectionChange(listener: (Boolean) -> Unit) {
        onConnectionChange = listener
    }

    fun setOnError(listener: (String) -> Unit) {
        onError = listener
    }
}

