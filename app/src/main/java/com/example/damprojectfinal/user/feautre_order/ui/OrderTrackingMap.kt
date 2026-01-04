package com.example.damprojectfinal.user.feautre_order.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.damprojectfinal.ui.theme.AppPrimaryRed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import android.graphics.Paint
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * Live tracking map showing restaurant location and user's real-time location
 * Using OpenStreetMap (OSM) - FREE, No API Key Required
 */
@Composable
fun OrderTrackingMap(
    restaurantLocation: RestaurantLocation?,
    userLocation: UserLocation?,
    distanceFormatted: String? = null,
    modifier: Modifier = Modifier,
    showDistance: Boolean = true
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var routePoints by remember { mutableStateOf<List<GeoPoint>>(emptyList()) }
    var isCalculatingRoute by remember { mutableStateOf(false) }

    // Initialize OSMDroid
    DisposableEffect(Unit) {
        Configuration.getInstance().load(
            context,
            context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )
        onDispose { }
    }

    // Calculate route when locations change (with debouncing for real-time updates)
    LaunchedEffect(restaurantLocation, userLocation) {
        // Debug logging
        android.util.Log.d("OrderTrackingMap", "=== LOCATION STATE UPDATE ===")
        android.util.Log.d("OrderTrackingMap", "Restaurant: ${restaurantLocation?.lat}, ${restaurantLocation?.lng}")
        android.util.Log.d("OrderTrackingMap", "User: ${userLocation?.lat}, ${userLocation?.lng}")
        android.util.Log.d("OrderTrackingMap", "Distance: $distanceFormatted")
        
        if (restaurantLocation != null && userLocation != null) {
            // Debounce route calculation to avoid too many API calls
            delay(1000) // Wait 1 second after location change
            
            android.util.Log.d("OrderTrackingMap", "🔄 Calculating route...")
            
            // Check if locations are still valid (user might have moved)
            if (restaurantLocation != null && userLocation != null) {
                isCalculatingRoute = true
                val route = calculateRoute(
                    userLocation.lat, userLocation.lng,
                    restaurantLocation.lat, restaurantLocation.lng
                )
                routePoints = route
                isCalculatingRoute = false
                android.util.Log.d("OrderTrackingMap", "✅ Route updated: ${route.size} points")
            }
        } else {
            android.util.Log.w("OrderTrackingMap", "⚠️ Missing location data - cannot calculate route")
            if (restaurantLocation == null) {
                android.util.Log.w("OrderTrackingMap", "  - Restaurant location: NULL")
            }
            if (userLocation == null) {
                android.util.Log.w("OrderTrackingMap", "  - User location: NULL")
            }
            routePoints = emptyList()
        }
    }

    // Calculate center point
    val centerPoint = remember(restaurantLocation, userLocation) {
        when {
            userLocation != null -> GeoPoint(userLocation.lat, userLocation.lng)
            restaurantLocation != null -> GeoPoint(restaurantLocation.lat, restaurantLocation.lng)
            else -> GeoPoint(36.8065, 10.1815) // Default: Tunis
        }
    }

    // Calculate zoom level to show both markers
    val zoomLevel = remember(restaurantLocation, userLocation) {
        if (restaurantLocation != null && userLocation != null) {
            // Calculate distance and adjust zoom
            val distance = calculateDistance(
                restaurantLocation.lat, restaurantLocation.lng,
                userLocation.lat, userLocation.lng
            )
            when {
                distance > 10000 -> 10.0 // Very far
                distance > 5000 -> 11.0
                distance > 2000 -> 12.0
                distance > 1000 -> 13.0
                distance > 500 -> 14.0
                else -> 15.0 // Close
            }
        } else {
            15.0
        }
    }

    // Markers - Create them once to avoid bitmap recreation on every update
    // context is already defined at top of function
    val restaurantMarkerIcon = remember { createPinMarkerIcon(context, Color(0xFFEF4444).toArgb()) }
    val userMarkerIcon = remember { createPinMarkerIcon(context, Color(0xFF3B82F6).toArgb()) }
    // Route color
    val routeColor = remember { Color(0xFF3B82F6).toArgb() }

    Box(modifier = modifier) {

                AndroidView(
                    factory = { ctx ->
                        MapView(ctx).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)
                            controller.setZoom(zoomLevel)
                            controller.setCenter(centerPoint)
                            
                            // Removed clipToOutline = true as it can cause crashes on some devices/views
                            
                            // Fix: Allow map interaction inside LazyColumn
                            setOnTouchListener { v, event ->
                                when (event.action) {
                                    android.view.MotionEvent.ACTION_DOWN,
                                    android.view.MotionEvent.ACTION_MOVE -> {
                                        v.parent.requestDisallowInterceptTouchEvent(true)
                                        false
                                    }
                                    android.view.MotionEvent.ACTION_UP,
                                    android.view.MotionEvent.ACTION_CANCEL -> {
                                        v.parent.requestDisallowInterceptTouchEvent(false)
                                        false
                                    }
                                    else -> false
                                }
                            }

                            // Restaurant marker (Red)
                            restaurantLocation?.let { location ->
                                val restaurantMarker = Marker(this)
                                restaurantMarker.position = GeoPoint(location.lat, location.lng)
                                restaurantMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                restaurantMarker.title = location.name ?: "Restaurant"
                                restaurantMarker.icon = android.graphics.drawable.BitmapDrawable(ctx.resources, restaurantMarkerIcon)
                                overlays.add(restaurantMarker)
                            }

                            // User marker (Blue)
                            userLocation?.let { location ->
                                val userMarker = Marker(this)
                                userMarker.position = GeoPoint(location.lat, location.lng)
                                userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                userMarker.title = "Your Location"
                                userMarker.icon = android.graphics.drawable.BitmapDrawable(ctx.resources, userMarkerIcon)
                                overlays.add(userMarker)
                            }

                            mapViewRef = this
                        }
                    },
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                    update = { mapView ->
                        // Update markers
                        mapView.overlays.clear()
                        
                        restaurantLocation?.let { location ->
                            val restaurantMarker = Marker(mapView)
                            restaurantMarker.position = GeoPoint(location.lat, location.lng)
                            restaurantMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            restaurantMarker.title = location.name ?: "Restaurant"
                            restaurantMarker.icon = android.graphics.drawable.BitmapDrawable(context.resources, restaurantMarkerIcon)
                            mapView.overlays.add(restaurantMarker)
                        }

                        userLocation?.let { location ->
                            val userMarker = Marker(mapView)
                            userMarker.position = GeoPoint(location.lat, location.lng)
                            userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            userMarker.title = "Your Location"
                            userMarker.icon = android.graphics.drawable.BitmapDrawable(context.resources, userMarkerIcon)
                            mapView.overlays.add(userMarker)
                        }

                        // Update route polyline
                        if (routePoints.isNotEmpty()) {
                            mapView.overlays.removeAll { it is Polyline }
                            
                            val routePolyline = Polyline()
                            routePolyline.setPoints(routePoints)
                            routePolyline.setColor(routeColor)
                            routePolyline.setWidth(10f)
                            mapView.overlays.add(routePolyline)
                        } else if (restaurantLocation != null && userLocation != null) {
                            mapView.overlays.removeAll { it is Polyline }
                            val polyline = Polyline()
                            polyline.setPoints(
                                listOf(
                                    GeoPoint(userLocation.lat, userLocation.lng),
                                    GeoPoint(restaurantLocation.lat, restaurantLocation.lng)
                                )
                            )
                            polyline.setColor(routeColor)
                            polyline.setWidth(8f)
                            mapView.overlays.add(polyline)
                        }

                        // Update camera
                        if (restaurantLocation != null && userLocation != null) {
                            val allPoints = mutableListOf<GeoPoint>()
                            allPoints.add(GeoPoint(restaurantLocation.lat, restaurantLocation.lng))
                            allPoints.add(GeoPoint(userLocation.lat, userLocation.lng))
                            if (routePoints.isNotEmpty()) {
                                allPoints.addAll(routePoints)
                            }
                            
                            val bounds = org.osmdroid.util.BoundingBox.fromGeoPoints(allPoints)
                            mapView.zoomToBoundingBox(bounds, true, 50)
                        } else {
                            val point = when {
                                userLocation != null -> GeoPoint(userLocation.lat, userLocation.lng)
                                restaurantLocation != null -> GeoPoint(restaurantLocation.lat, restaurantLocation.lng)
                                else -> centerPoint
                            }
                            mapView.controller.setCenter(point)
                            mapView.controller.setZoom(zoomLevel)
                        }

                        mapView.invalidate()
                    }
                )

                // Restaurant Address Card (Top Left)
        if (restaurantLocation != null && restaurantLocation.name != null) {
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
                    horizontalArrangement = Arrangement.Start
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Restaurant",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Restaurant",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = restaurantLocation.name,
                            fontSize = 12.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Medium,
                            maxLines = 2
                        )
                    }
                }
            }
        }

        // Distance indicator (Top Right)
        if (showDistance && distanceFormatted != null) {
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
                        text = distanceFormatted,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppPrimaryRed
                    )
                }
            }
        }

        // Status indicator
        if (userLocation == null || isCalculatingRoute) {
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
                        text = when {
                            userLocation == null -> "Waiting for location..."
                            isCalculatingRoute -> "Calculating route..."
                            else -> "Updating route..."
                        },
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

/**
 * Create a PIN/Teardrop marker icon (Safe drawing)
 */
private fun createPinMarkerIcon(context: Context, colorValue: Int): android.graphics.Bitmap {
    val size = (48 * context.resources.displayMetrics.density).toInt()
    val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    
    val paint = Paint().apply {
        isAntiAlias = true
        color = colorValue
        style = Paint.Style.FILL
    }
    
    // Draw Pin Shape (Teardrop)
    val cx = size / 2f
    val cy = size / 2.6f
    val radius = size / 2.8f
    
    // 1. Draw Shadow
    val shadowPaint = Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.BLACK
        alpha = 50
    }
    canvas.drawCircle(cx, size - 4f, 6f, shadowPaint)
    
    // 2. Draw Triangle (Bottom)
    val trianglePath = android.graphics.Path()
    trianglePath.moveTo(cx - radius * 0.9f, cy + radius * 0.1f) // Overlap slightly
    trianglePath.lineTo(cx + radius * 0.9f, cy + radius * 0.1f)
    trianglePath.lineTo(cx, size.toFloat())
    trianglePath.close()
    canvas.drawPath(trianglePath, paint)
    
    // 3. Draw Circle (Top)
    canvas.drawCircle(cx, cy, radius, paint)
    
    // 4. Draw White Hole
    val holePaint = Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.WHITE
    }
    canvas.drawCircle(cx, cy, radius * 0.4f, holePaint)
    
    return bitmap
}

/**
 * Calculate route using OSRM (Open Source Routing Machine) - FREE
 * Returns list of GeoPoints representing the route
 */
suspend fun calculateRoute(
    startLat: Double,
    startLon: Double,
    endLat: Double,
    endLon: Double
): List<GeoPoint> = withContext(Dispatchers.IO) {
    try {
        val urlString = "https://router.project-osrm.org/route/v1/driving/" +
                "$startLon,$startLat;$endLon,$endLat" +
                "?overview=full&geometries=geojson"
        
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("User-Agent", "FoodyzApp/1.0")
        connection.connectTimeout = 10000
        connection.readTimeout = 10000

        val responseCode = connection.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            val reader = BufferedReader(InputStreamReader(connection.inputStream, "UTF-8"))
            val response = StringBuilder()
            var line: String?
            
            while (reader.readLine().also { line = it } != null) {
                response.append(line)
            }
            reader.close()

            val jsonResponse = JSONObject(response.toString())
            
            if (jsonResponse.optString("code") == "Ok") {
                val routes = jsonResponse.getJSONArray("routes")
                if (routes.length() > 0) {
                    val route = routes.getJSONObject(0)
                    val geometry = route.getJSONObject("geometry")
                    val coordinates = geometry.getJSONArray("coordinates")
                    
                    val points = mutableListOf<GeoPoint>()
                    for (i in 0 until coordinates.length()) {
                        val coord = coordinates.getJSONArray(i)
                        val lon = coord.getDouble(0)
                        val lat = coord.getDouble(1)
                        points.add(GeoPoint(lat, lon))
                    }
                    
                    android.util.Log.d("OrderTrackingMap", "✅ Route calculated: ${points.size} points")
                    return@withContext points
                }
            } else {
                android.util.Log.w("OrderTrackingMap", "⚠️ Route not found: ${jsonResponse.optString("code")}")
            }
        } else {
            android.util.Log.e("OrderTrackingMap", "❌ OSRM API error: HTTP $responseCode")
        }
    } catch (e: Exception) {
        android.util.Log.e("OrderTrackingMap", "❌ Route calculation error: ${e.message}", e)
    }

    return@withContext emptyList()
}

/**
 * Calculate distance between two points using Haversine formula
 */
private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val earthRadius = 6371000.0 // meters
    
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2)
    
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    
    return earthRadius * c
}

/**
 * Restaurant location data
 */
data class RestaurantLocation(
    val lat: Double,
    val lng: Double,
    val name: String? = null,
    val address: String? = null
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
