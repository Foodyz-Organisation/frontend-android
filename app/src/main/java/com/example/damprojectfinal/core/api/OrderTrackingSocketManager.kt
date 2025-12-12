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
    private var onSharingStarted: ((String) -> Unit)? = null
    private var onSharingStopped: ((String) -> Unit)? = null
    private var onUserJoined: ((String, String) -> Unit)? = null // userType, clientId
    private var onConnectionChange: ((Boolean) -> Unit)? = null
    private var onError: ((String) -> Unit)? = null

    data class LocationUpdate(
        val userId: String,
        val lat: Double,
        val lng: Double,
        val accuracy: Double?,
        val timestamp: Long
    )

    fun connect() {
        try {
            val options = IO.Options.builder()
                .setAuth(mapOf("token" to authToken))
                .setPath(socketPath)
                .setReconnection(true)
                .setReconnectionDelay(1000)
                .setReconnectionDelayMax(5000)
                .setReconnectionAttempts(Integer.MAX_VALUE)
                .build()

            // Connect to the 'order-tracking' namespace
            socket = IO.socket("$baseUrl/order-tracking", options)

            socket?.on(Socket.EVENT_CONNECT) {
                Log.d(TAG, "✅ Connected to order tracking server")
                onConnectionChange?.invoke(true)
            }

            socket?.on(Socket.EVENT_DISCONNECT) {
                Log.d(TAG, "❌ Disconnected from order tracking server")
                onConnectionChange?.invoke(false)
            }

            // Listen for location updates
            socket?.on("location-update") { args ->
                try {
                    val jsonObj = args[0] as? JSONObject
                    if (jsonObj != null) {
                        val update = LocationUpdate(
                            userId = jsonObj.getString("userId"),
                            lat = jsonObj.getDouble("lat"),
                            lng = jsonObj.getDouble("lng"),
                            accuracy = if (jsonObj.has("accuracy") && !jsonObj.isNull("accuracy")) {
                                jsonObj.getDouble("accuracy")
                            } else null,
                            timestamp = jsonObj.getLong("timestamp")
                        )
                        onLocationUpdate?.invoke(update)
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
                    onUserJoined?.invoke(userType, clientId)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing user-joined", e)
                }
            }

            socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                val error = args[0]?.toString() ?: "Unknown error"
                Log.e(TAG, "Connection error: $error")
                onError?.invoke("Connection error: $error")
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
            val payload = JSONObject().apply {
                put("orderId", orderId)
                put("userType", userType) // "user" or "pro"
            }
            socket?.emit("join-order", payload)
            Log.d(TAG, "Joined order room: $orderId as $userType")
        } catch (e: Exception) {
            Log.e(TAG, "Error joining order", e)
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

    fun setOnSharingStarted(listener: (String) -> Unit) {
        onSharingStarted = listener
    }

    fun setOnSharingStopped(listener: (String) -> Unit) {
        onSharingStopped = listener
    }

    fun setOnUserJoined(listener: (String, String) -> Unit) {
        onUserJoined = listener
    }

    fun setOnConnectionChange(listener: (Boolean) -> Unit) {
        onConnectionChange = listener
    }

    fun setOnError(listener: (String) -> Unit) {
        onError = listener
    }
}

