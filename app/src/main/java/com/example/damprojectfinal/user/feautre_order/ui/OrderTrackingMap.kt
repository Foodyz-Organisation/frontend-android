package com.example.damprojectfinal.user.feautre_order.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.damprojectfinal.ui.theme.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import kotlin.math.*

/**
 * Live tracking map showing restaurant location and user's real-time location
 */
@Composable
fun OrderTrackingMap(
    restaurantLocation: RestaurantLocation?,
    userLocation: UserLocation?,
    modifier: Modifier = Modifier,
    showDistance: Boolean = true
) {
    val context = LocalContext.current
    
    // Initialize OSMDroid
    DisposableEffect(Unit) {
        Configuration.getInstance().load(
            context,
            context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )
        onDispose { }
    }
    
    // Calculate distance if both locations available
    val distance = remember(restaurantLocation, userLocation) {
        if (restaurantLocation != null && userLocation != null) {
            calculateDistance(
                restaurantLocation.lat,
                restaurantLocation.lng,
                userLocation.lat,
                userLocation.lng
            )
        } else null
    }
    
    Box(modifier = modifier) {
        // Map View
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    
                    // Set initial zoom and center
                    val centerPoint = when {
                        userLocation != null -> GeoPoint(userLocation.lat, userLocation.lng)
                        restaurantLocation != null -> GeoPoint(restaurantLocation.lat, restaurantLocation.lng)
                        else -> GeoPoint(36.8065, 10.1815) // Default: Tunis
                    }
                    
                    controller.setZoom(15.0)
                    controller.setCenter(centerPoint)
                    
                    // Add restaurant marker
                    restaurantLocation?.let { location ->
                        val restaurantMarker = Marker(this).apply {
                            position = GeoPoint(location.lat, location.lng)
                            title = location.name ?: "Restaurant"
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            
                            // Custom icon (red pin for restaurant)
                            icon = createMarkerIcon(ctx, Color(0xFFEF4444))
                        }
                        overlays.add(restaurantMarker)
                    }
                    
                    // Add user marker
                    userLocation?.let { location ->
                        val userMarker = Marker(this).apply {
                            position = GeoPoint(location.lat, location.lng)
                            title = "Your Location"
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            
                            // Custom icon (blue pin for user)
                            icon = createMarkerIcon(ctx, Color(0xFF3B82F6))
                        }
                        overlays.add(userMarker)
                    }
                    
                    // Add polyline between restaurant and user if both available
                    if (restaurantLocation != null && userLocation != null) {
                        val polyline = Polyline().apply {
                            addPoint(GeoPoint(restaurantLocation.lat, restaurantLocation.lng))
                            addPoint(GeoPoint(userLocation.lat, userLocation.lng))
                            color = Color(0xFF3B82F6).hashCode()
                            width = 5f
                        }
                        overlays.add(polyline)
                    }
                    
                    // Center map to show both markers
                    if (restaurantLocation != null && userLocation != null) {
                        val bounds = org.osmdroid.util.BoundingBox(
                            maxOf(restaurantLocation.lat, userLocation.lat),
                            maxOf(restaurantLocation.lng, userLocation.lng),
                            minOf(restaurantLocation.lat, userLocation.lat),
                            minOf(restaurantLocation.lng, userLocation.lng)
                        )
                        zoomToBoundingBox(bounds, false, 50)
                    }
                }
            },
            update = { mapView ->
                // Update user marker position when location changes
                userLocation?.let { location ->
                    // Remove old user marker
                    mapView.overlays.removeAll { it is Marker && it.title == "Your Location" }
                    
                    // Remove old polyline
                    mapView.overlays.removeAll { it is Polyline }
                    
                    // Add updated user marker
                    val userMarker = Marker(mapView).apply {
                        position = GeoPoint(location.lat, location.lng)
                        title = "Your Location"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        icon = createMarkerIcon(context, Color(0xFF3B82F6))
                    }
                    mapView.overlays.add(userMarker)
                    
                    // Update polyline
                    restaurantLocation?.let { restLoc ->
                        val polyline = Polyline().apply {
                            addPoint(GeoPoint(restLoc.lat, restLoc.lng))
                            addPoint(GeoPoint(location.lat, location.lng))
                            color = Color(0xFF3B82F6).hashCode()
                            width = 5f
                        }
                        mapView.overlays.add(polyline)
                    }
                    
                    // Center map to show both markers
                    restaurantLocation?.let { restLoc ->
                        val bounds = org.osmdroid.util.BoundingBox(
                            maxOf(restLoc.lat, location.lat),
                            maxOf(restLoc.lng, location.lng),
                            minOf(restLoc.lat, location.lat),
                            minOf(restLoc.lng, location.lng)
                        )
                        mapView.zoomToBoundingBox(bounds, false, 50)
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // Distance indicator (top right)
        if (showDistance && distance != null) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Distance",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (distance < 1000) {
                            "${distance.toInt()} m"
                        } else {
                            String.format("%.2f km", distance / 1000)
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppPrimaryRed
                    )
                }
            }
        }
        
        // Status indicator (top left)
        if (userLocation == null) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = AppPrimaryRed
                    )
                    Text(
                        text = "Waiting for location...",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

/**
 * Create a custom marker icon (colored circle)
 */
private fun createMarkerIcon(
    context: Context,
    composeColor: Color
): Drawable {
    val size = 80
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    
    // Convert Compose Color to Android Color Int
    val androidColor = android.graphics.Color.argb(
        (composeColor.alpha * 255).toInt(),
        (composeColor.red * 255).toInt(),
        (composeColor.green * 255).toInt(),
        (composeColor.blue * 255).toInt()
    )
    
    // Draw circle background
    val paint = android.graphics.Paint().apply {
        isAntiAlias = true
        color = androidColor
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 4, paint)
    
    // Draw white border
    val borderPaint = android.graphics.Paint().apply {
        isAntiAlias = true
        style = android.graphics.Paint.Style.STROKE
        strokeWidth = 4f
        color = android.graphics.Color.WHITE
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 4, borderPaint)
    
    return BitmapDrawable(context.resources, bitmap)
}

/**
 * Calculate distance between two points in meters (Haversine formula)
 */
private fun calculateDistance(
    lat1: Double,
    lng1: Double,
    lat2: Double,
    lng2: Double
): Double {
    val earthRadius = 6371000.0 // Earth radius in meters
    
    val dLat = Math.toRadians(lat2 - lat1)
    val dLng = Math.toRadians(lng2 - lng1)
    
    val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLng / 2).pow(2)
    
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    
    return earthRadius * c
}

/**
 * Restaurant location data
 */
data class RestaurantLocation(
    val lat: Double,
    val lng: Double,
    val name: String? = null
)

/**
 * User location data (from live tracking)
 */
data class UserLocation(
    val lat: Double,
    val lng: Double,
    val accuracy: Float? = null,
    val timestamp: Long = System.currentTimeMillis()
)

