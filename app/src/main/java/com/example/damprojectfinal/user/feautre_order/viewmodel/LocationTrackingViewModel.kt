package com.example.damprojectfinal.user.feautre_order.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.damprojectfinal.core.api.AppConfig
import com.example.damprojectfinal.core.api.OrderTrackingSocketManager
import com.example.damprojectfinal.core.api.TokenManager
import com.example.damprojectfinal.core.location.LocationTracker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LocationTrackingState(
    val isConnected: Boolean = false,
    val isSharing: Boolean = false,
    val currentLocation: LocationData? = null,
    val restaurantLocation: RestaurantLocationData? = null,
    val distance: Double? = null, // Distance in kilometers
    val distanceFormatted: String? = null, // Formatted string like "2.5 km" or "150 m"
    val error: String? = null
)

data class LocationData(
    val lat: Double,
    val lng: Double,
    val accuracy: Float?
)

data class RestaurantLocationData(
    val lat: Double,
    val lng: Double,
    val name: String?,
    val address: String?
)

class LocationTrackingViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "LocationTrackingVM"
    
    private val _state = MutableStateFlow(LocationTrackingState())
    val state: StateFlow<LocationTrackingState> = _state.asStateFlow()
    
    private var socketManager: OrderTrackingSocketManager? = null
    private val locationTracker = LocationTracker(application.applicationContext)
    private val tokenManager = TokenManager(application.applicationContext)
    
    private var currentOrderId: String? = null
    private var currentUserId: String? = null
    
    init {
        setupSocketCallbacks()
    }
    
    private fun setupSocketCallbacks() {
        // Will be set when connecting
    }
    
    /**
     * Connect to order tracking WebSocket and join order room
     */
    fun connectToOrder(orderId: String, userId: String, userType: String = "user") {
        viewModelScope.launch {
            try {
                currentOrderId = orderId
                currentUserId = userId
                
                val token = tokenManager.getAccessTokenBlocking()
                if (token == null) {
                    _state.value = _state.value.copy(error = "No authentication token")
                    Log.e(TAG, "❌ No token available")
                    return@launch
                }
                
                val baseUrl = AppConfig.SOCKET_BASE_URL
                socketManager = OrderTrackingSocketManager(
                    baseUrl = baseUrl,
                    authToken = token,
                    socketPath = AppConfig.SOCKET_PATH
                )
                
                // Setup callbacks
                socketManager?.setOnConnectionChange { isConnected ->
                    Log.d(TAG, "📡 Connection state changed: $isConnected")
                    _state.value = _state.value.copy(isConnected = isConnected)
                    if (isConnected) {
                        Log.d(TAG, "✅ Connected to order tracking WebSocket")
                        Log.d(TAG, "🔗 Joining order room: $orderId as $userType")
                        socketManager?.joinOrder(orderId, userType)
                    } else {
                        Log.w(TAG, "❌ Disconnected from order tracking")
                    }
                }
                
                socketManager?.setOnRestaurantLocation { restaurantLoc ->
                    Log.d(TAG, "🏪 Restaurant location received:")
                    Log.d(TAG, "  📍 Lat/Lng: ${restaurantLoc.lat}, ${restaurantLoc.lon}")
                    Log.d(TAG, "  🏷️ Name: ${restaurantLoc.name}")
                    Log.d(TAG, "  📬 Address: ${restaurantLoc.address}")
                    
                    _state.value = _state.value.copy(
                        restaurantLocation = RestaurantLocationData(
                            lat = restaurantLoc.lat,
                            lng = restaurantLoc.lon,
                            name = restaurantLoc.name,
                            address = restaurantLoc.address
                        )
                    )
                }

                socketManager?.setOnLocationUpdate { update ->
                    _state.value = _state.value.copy(
                        currentLocation = LocationData(
                            lat = update.lat,
                            lng = update.lng,
                            accuracy = update.accuracy?.toFloat()
                        ),
                        distance = update.distance,
                        distanceFormatted = update.distanceFormatted,
                        // Update restaurant location if provided in update
                        restaurantLocation = update.restaurantLocation?.let { restLoc ->
                            RestaurantLocationData(
                                lat = restLoc.lat,
                                lng = restLoc.lon,
                                name = restLoc.name,
                                address = restLoc.address
                            )
                        } ?: _state.value.restaurantLocation
                    )
                    Log.d(TAG, "📍 Location update received: ${update.lat}, ${update.lng}, distance: ${update.distanceFormatted}")
                }
                
                socketManager?.setOnSharingStarted { userId ->
                    Log.d(TAG, "✅ Sharing started for user: $userId")
                }
                
                socketManager?.setOnSharingStopped { userId ->
                    Log.d(TAG, "⏹️ Sharing stopped for user: $userId")
                    if (userId == currentUserId) {
                        _state.value = _state.value.copy(isSharing = false)
                    }
                }
                
                socketManager?.setOnError { error ->
                    _state.value = _state.value.copy(error = error)
                    Log.e(TAG, "❌ Socket error: $error")
                }
                
                // Connect
                socketManager?.connect()
                
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = "Failed to connect: ${e.message}")
                Log.e(TAG, "❌ Failed to connect to order tracking", e)
            }
        }
    }
    
    /**
     * Start sharing location (request permission and start tracking)
     */
    fun startSharingLocation() {
        viewModelScope.launch {
            val orderId = currentOrderId
            val userId = currentUserId
            
            if (orderId == null || userId == null) {
                _state.value = _state.value.copy(error = "Order ID or User ID not set")
                return@launch
            }
            
            if (!locationTracker.hasLocationPermission()) {
                _state.value = _state.value.copy(error = "Location permission not granted")
                return@launch
            }
            
            if (!locationTracker.isLocationEnabled()) {
                _state.value = _state.value.copy(error = "Location services are disabled")
                return@launch
            }
            
            // Notify server that sharing has started
            Log.d(TAG, "📤 Notifying server: Starting location sharing")
            Log.d(TAG, "  Order ID: $orderId")
            Log.d(TAG, "  User ID: $userId")
            socketManager?.startSharing(orderId, userId)
            _state.value = _state.value.copy(isSharing = true, error = null)
            
            // Start continuous location tracking
            locationTracker.startTracking(
                minTime = 5000, // Update every 5 seconds
                minDistance = 10f, // Or 10 meters
                onLocationUpdate = { locationData ->
                    // Send location update via WebSocket
                    socketManager?.sendLocationUpdate(
                        orderId = orderId,
                        userId = userId,
                        lat = locationData.latitude,
                        lng = locationData.longitude,
                        accuracy = locationData.accuracy?.toDouble()
                    )
                    
                    // Update local state
                    Log.d(TAG, "📍 GPS Update: ${locationData.latitude}, ${locationData.longitude} (±${locationData.accuracy}m)")
                    _state.value = _state.value.copy(
                        currentLocation = LocationData(
                            lat = locationData.latitude,
                            lng = locationData.longitude,
                            accuracy = locationData.accuracy
                        )
                    )
                },
                onError = { error ->
                    _state.value = _state.value.copy(error = error)
                    Log.e(TAG, "❌ Location tracking error: $error")
                }
            )
            
            Log.d(TAG, "✅ Started sharing location for order: $orderId")
        }
    }
    
    /**
     * Stop sharing location
     */
    fun stopSharingLocation() {
        viewModelScope.launch {
            val orderId = currentOrderId
            val userId = currentUserId
            
            if (orderId != null && userId != null) {
                socketManager?.stopSharing(orderId, userId)
            }
            
            locationTracker.stopTracking()
            _state.value = _state.value.copy(isSharing = false)
            Log.d(TAG, "⏹️ Stopped sharing location")
        }
    }
    
    /**
     * Disconnect from WebSocket
     */
    fun disconnect() {
        stopSharingLocation()
        socketManager?.disconnect()
        socketManager = null
        currentOrderId = null
        currentUserId = null
        _state.value = LocationTrackingState()
        Log.d(TAG, "🔌 Disconnected from order tracking")
    }
    
    override fun onCleared() {
        super.onCleared()
        disconnect()
    }
}

