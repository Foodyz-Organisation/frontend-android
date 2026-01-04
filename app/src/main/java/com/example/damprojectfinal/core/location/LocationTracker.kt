package com.example.damprojectfinal.core.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat

/**
 * Location Tracker for getting user's current location
 */
class LocationTracker(private val context: Context) {
    private val TAG = "LocationTracker"
    private var locationManager: LocationManager? = null
    private var locationListener: LocationListener? = null
    private var isTracking = false

    data class LocationData(
        val latitude: Double,
        val longitude: Double,
        val accuracy: Float?
    )

    /**
     * Check if location permissions are granted
     */
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Check if location services are enabled
     */
    fun isLocationEnabled(): Boolean {
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true ||
                locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true
    }

    /**
     * Get current location (one-time)
     */
    fun getCurrentLocation(
        onSuccess: (LocationData) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!hasLocationPermission()) {
            onError("Location permission not granted")
            return
        }

        if (!isLocationEnabled()) {
            onError("Location services are disabled")
            return
        }

        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                locationManager?.removeUpdates(this)
                val locationData = LocationData(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    accuracy = location.accuracy
                )
                onSuccess(locationData)
                Log.d(TAG, "Location obtained: ${locationData.latitude}, ${locationData.longitude}")
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {
                onError("Location provider disabled")
            }
        }

        try {
            // Try GPS first, then network
            val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            var locationObtained = false

            for (provider in providers) {
                val lastKnownLocation = locationManager?.getLastKnownLocation(provider)
                if (lastKnownLocation != null) {
                    val locationData = LocationData(
                        latitude = lastKnownLocation.latitude,
                        longitude = lastKnownLocation.longitude,
                        accuracy = lastKnownLocation.accuracy
                    )
                    onSuccess(locationData)
                    locationObtained = true
                    break
                }
            }

            if (!locationObtained) {
                // Request fresh location updates
                for (provider in providers) {
                    if (locationManager?.isProviderEnabled(provider) == true) {
                        locationManager?.requestLocationUpdates(
                            provider,
                            0L,
                            0f,
                            locationListener!!
                        )
                        break
                    }
                }
            }
        } catch (e: SecurityException) {
            onError("Location permission denied: ${e.message}")
        } catch (e: Exception) {
            onError("Failed to get location: ${e.message}")
        }
    }

    /**
     * Start continuous location tracking
     */
    fun startTracking(
        minTime: Long = 5000, // 5 seconds
        minDistance: Float = 10f, // 10 meters
        onLocationUpdate: (LocationData) -> Unit,
        onError: (String) -> Unit
    ) {
        if (isTracking) {
            Log.w(TAG, "Already tracking location")
            return
        }

        if (!hasLocationPermission()) {
            onError("Location permission not granted")
            return
        }

        if (!isLocationEnabled()) {
            onError("Location services are disabled")
            return
        }

        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val locationData = LocationData(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    accuracy = location.accuracy
                )
                onLocationUpdate(locationData)
                Log.d(TAG, "Location update: ${locationData.latitude}, ${locationData.longitude}")
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {
                onError("Location provider disabled")
            }
        }

        try {
            // 1. Try to get LAST KNOWN LOCATION first for immediate UI feedback
            // We do this safely by checking Network first (faster, less blocking risk usually)
            var initialLocationFound = false
            
            try {
                // Try Network first
                 if (locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true) {
                     val lastNetwork = locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                     if (lastNetwork != null) {
                         val locationData = LocationData(
                            latitude = lastNetwork.latitude,
                            longitude = lastNetwork.longitude,
                            accuracy = lastNetwork.accuracy
                        )
                        onLocationUpdate(locationData)
                        initialLocationFound = true
                        Log.d(TAG, "📍 Initial location (Network Last Known): ${locationData.latitude}, ${locationData.longitude}")
                     }
                 }
                 
                 // Try GPS if Network failed
                 if (!initialLocationFound && locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true) {
                     val lastGps = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                     if (lastGps != null) {
                        val locationData = LocationData(
                            latitude = lastGps.latitude,
                            longitude = lastGps.longitude,
                            accuracy = lastGps.accuracy
                        )
                        onLocationUpdate(locationData)
                        Log.d(TAG, "📍 Initial location (GPS Last Known): ${locationData.latitude}, ${locationData.longitude}")
                     }
                 }
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ Failed to get last known location: ${e.message}")
            }

            // 2. Request CONTINUOUS UPDATES from available providers
            // We prioritize NETWORK for speed/indoors, but also request GPS for accuracy if available
            // We do NOT break after one; we can request from multiple if needed, or just pick the best strategy.
            // Strategy: Try Network first. If available, use it. Also try GPS.
            
            var started = false
            
            // Request Network Updates (Fast, Indoors)
            if (locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true) {
                 try {
                     locationManager?.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        minTime,
                        minDistance,
                        locationListener!!
                    )
                    started = true
                    Log.d(TAG, "✅ Started tracking with NETWORK_PROVIDER")
                 } catch (e: Exception) {
                     Log.e(TAG, "Failed Network provider: ${e.message}")
                 }
            }
            
            // Request GPS Updates (Accurate, Outdoors)
            // Even if Network started, GPS is better for moving vehicles/delivery
            if (locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true) {
                try {
                    locationManager?.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        minTime,
                        minDistance,
                        locationListener!!
                    )
                    started = true
                    Log.d(TAG, "✅ Started tracking with GPS_PROVIDER")
                } catch (e: Exception) {
                     Log.e(TAG, "Failed GPS provider: ${e.message}")
                }
            }
            
            if (started) {
                isTracking = true
            } else {
                onError("No suitable location provider enabled (Network or GPS)")
            }
            
        } catch (e: SecurityException) {
            onError("Location permission denied: ${e.message}")
        } catch (e: Exception) {
            onError("Failed to start tracking: ${e.message}")
        }
    }

    /**
     * Stop location tracking
     */
    fun stopTracking() {
        if (isTracking && locationListener != null) {
            locationManager?.removeUpdates(locationListener!!)
            isTracking = false
            Log.d(TAG, "Stopped tracking location")
        }
    }
}

