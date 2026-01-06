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
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                currentOrderId = orderId
                currentUserId = userId
                
                val token = tokenManager.getAccessTokenAsync()
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
                    
                    val newRestLoc = RestaurantLocationData(
                        lat = restaurantLoc.lat,
                        lng = restaurantLoc.lon,
                        name = restaurantLoc.name,
                        address = restaurantLoc.address
                    )
                    
                    // Recalculate distance if we already have a current location
                    var distanceKm: Double? = _state.value.distance
                    var distanceStr: String? = _state.value.distanceFormatted
                    val currentLoc = _state.value.currentLocation
                    
                    if (currentLoc != null) {
                        distanceKm = calculateDistance(
                            currentLoc.lat, currentLoc.lng,
                            newRestLoc.lat, newRestLoc.lng
                        )
                        distanceStr = if (distanceKm < 1.0) {
                            "${(distanceKm * 1000).toInt()} m"
                        } else {
                            String.format("%.1f km", distanceKm)
                        }
                    }

                    _state.value = _state.value.copy(
                        restaurantLocation = newRestLoc,
                        distance = distanceKm,
                        distanceFormatted = distanceStr
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
     * Start local tracking (UI only, no socket sharing)
     * Used for Navigation Mode to show user's position
     */
    fun startLocalTracking() {
        viewModelScope.launch {
             // If already sharing, we are already tracking, so do nothing OR ensure we update UI
             if (_state.value.isSharing) return@launch
             
             if (!locationTracker.hasLocationPermission() || !locationTracker.isLocationEnabled()) {
                 Log.e(TAG, "❌ Cannot start local tracking: Missing permissions or GPS disabled")
                 return@launch
             }

             Log.d(TAG, "📍 Starting local GPS tracking (UI only)")
             try {
                locationTracker.startTracking(
                    minTime = 2000, // Faster updates for navigation (2s)
                    minDistance = 5f, // 5 meters
                    onLocationUpdate = { locationData ->
                        processLocationUpdate(locationData)
                    },
                    onError = { error ->
                        Log.e(TAG, "❌ Local tracking error: $error")
                    }
                )
             } catch (e: Exception) {
                 Log.e(TAG, "❌ Exception starting local tracker: ${e.message}")
             }
        }
    }

    /**
     * Start sharing location (request permission and start tracking)
     */
    fun startSharingLocation() {
        viewModelScope.launch {
            if (_state.value.isSharing) {
                Log.w(TAG, "⚠️ Already sharing location, ignoring start request")
                return@launch
            }

            val orderId = currentOrderId
            val userId = currentUserId
            
            if (orderId == null || userId == null) {
                _state.value = _state.value.copy(error = "Order ID or User ID not set")
                Log.e(TAG, "❌ Cannot start sharing: OrderId=$orderId, UserId=$userId")
                return@launch
            }
            
            // socketManager might be null if not connected yet
            if (socketManager == null || socketManager?.isConnected() == false) {
                 Log.w(TAG, "⚠️ Socket not connected yet, waiting...")
                 // Optional: Wait or just fail? For now, we fail and let retry happen if needed
                 // But actually, we should probably just return as we cannot share without socket
                 _state.value = _state.value.copy(error = "Socket not connected")
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
            try {
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
                        
                        // Update local state and distance
                        processLocationUpdate(locationData)
                    },
                    onError = { error ->
                        _state.value = _state.value.copy(error = error)
                        Log.e(TAG, "❌ Location tracking error: $error")
                    }
                )
                Log.d(TAG, "✅ Started sharing location for order: $orderId")
            } catch (e: Exception) {
                 Log.e(TAG, "❌ Exception starting tracker: ${e.message}")
                 _state.value = _state.value.copy(error = "Tracker error: ${e.message}")
            }
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
        locationTracker.stopTracking() // Ensure local tracking also stops
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

    /**
     * Calculate distance between two points using Haversine formula (in km)
     */
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0 // kilometers
        
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        
        return earthRadius * c
    }

    /**
     * Process location update: calculate distance and update state
     * Includes simple accuracy filter to stabilize position
     */
    private fun processLocationUpdate(locationData: com.example.damprojectfinal.core.location.LocationTracker.LocationData) {
        // Filter out low accuracy updates, BUT always allow if we don't have a location yet
        if (locationData.accuracy != null && locationData.accuracy > 200) {
             if (_state.value.currentLocation != null) {
                 Log.d(TAG, "⚠️ Ignoring low accuracy location: ${locationData.accuracy}m")
                 return
             }
        }

        // Calculate distance if restaurant location is available
        val currentRestLoc = _state.value.restaurantLocation
        var distanceKm: Double? = null
        var distanceStr: String? = null
        
        if (currentRestLoc != null) {
            distanceKm = calculateDistance(
                locationData.latitude, locationData.longitude,
                currentRestLoc.lat, currentRestLoc.lng
            )
            // Format distance
            distanceStr = if (distanceKm < 1.0) {
                "${(distanceKm * 1000).toInt()} m"
            } else {
                String.format("%.1f km", distanceKm)
            }
        }

        // Update state
        _state.value = _state.value.copy(
            currentLocation = LocationData(
                lat = locationData.latitude,
                lng = locationData.longitude,
                accuracy = locationData.accuracy
            ),
            distance = distanceKm,
            distanceFormatted = distanceStr
        )
    }
}

