package com.example.damprojectfinal.user.feautre_order.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.damprojectfinal.ui.theme.AppPrimaryRed
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * Live tracking map showing restaurant location and user's real-time location
 * Using MapLibre SDK for 3D views
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
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Initialize MapLibre
    remember { MapLibre.getInstance(context) }

    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var mapboxMap by remember { mutableStateOf<org.maplibre.android.maps.MapLibreMap?>(null) }
    var routePoints by remember { mutableStateOf<List<Point>>(emptyList()) }
    var isCalculatingRoute by remember { mutableStateOf(false) }

    // Lifecycle observer for MapView
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            val mapView = mapViewRef ?: return@LifecycleEventObserver
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            // Ensure onDestroy is called if not handled by lifecycle
            mapViewRef?.onDestroy()
        }
    }

    // Calculate route logic
    LaunchedEffect(restaurantLocation, userLocation) {
        if (restaurantLocation != null && userLocation != null) {
            delay(1000)
            isCalculatingRoute = true
            val route = calculateRoute(
                userLocation.lat, userLocation.lng,
                restaurantLocation.lat, restaurantLocation.lng
            )
            routePoints = route
            isCalculatingRoute = false
        }
    }

    // Create marker bitmaps once
    val restaurantMarkerIcon = remember { createPinMarkerIcon(context, android.graphics.Color.parseColor("#EF4444")) }
    val userMarkerIcon = remember { createPinMarkerIcon(context, android.graphics.Color.parseColor("#3B82F6")) }

    Box(modifier = modifier) {
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    onCreate(null) // Important for MapView initialization
                    getMapAsync { map ->
                        mapboxMap = map
                        
                        // Set style (using OpenFreeMap for better reliability)
                        map.setStyle("https://tiles.openfreemap.org/styles/liberty") { style ->
                            // 1. Add Images for markers
                            style.addImage("restaurant-icon", restaurantMarkerIcon)
                            style.addImage("user-icon", userMarkerIcon)

                            // 2. Setup Sources
                            // Restaurant source
                            style.addSource(GeoJsonSource("restaurant-source"))
                            // User source
                            style.addSource(GeoJsonSource("user-source"))
                            // Route source
                            style.addSource(GeoJsonSource("route-source"))

                            // 3. Setup Layers
                            // Route Line
                            style.addLayer(
                                LineLayer("route-layer", "route-source").withProperties(
                                    PropertyFactory.lineColor(android.graphics.Color.parseColor("#3B82F6")),
                                    PropertyFactory.lineWidth(5f),
                                    PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                                    PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND)
                                )
                            )

                            // Restaurant Marker
                            style.addLayer(
                                SymbolLayer("restaurant-layer", "restaurant-source").withProperties(
                                    PropertyFactory.iconImage("restaurant-icon"),
                                    PropertyFactory.iconAnchor(Property.ICON_ANCHOR_BOTTOM),
                                    PropertyFactory.iconAllowOverlap(true),
                                    PropertyFactory.iconIgnorePlacement(true)
                                )
                            )

                            // User Marker
                            style.addLayer(
                                SymbolLayer("user-layer", "user-source").withProperties(
                                    PropertyFactory.iconImage("user-icon"),
                                    PropertyFactory.iconAnchor(Property.ICON_ANCHOR_BOTTOM),
                                    PropertyFactory.iconAllowOverlap(true),
                                    PropertyFactory.iconIgnorePlacement(true)
                                )
                            )
                        }
                        
                        // UI Settings
                        map.uiSettings.isCompassEnabled = true
                        map.uiSettings.isLogoEnabled = false
                        map.uiSettings.isAttributionEnabled = false
                        map.uiSettings.isRotateGesturesEnabled = true
                        map.uiSettings.isTiltGesturesEnabled = true
                        map.uiSettings.isZoomGesturesEnabled = true
                        map.uiSettings.isScrollGesturesEnabled = true
                    }
                    mapViewRef = this
                }
            },
            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
            update = { mapView ->
                // Lifecycle methods are now handled by DisposableEffect
                
                mapboxMap?.let { map ->
                    map.getStyle { style ->
                        // Update Restaurant Source
                        val restaurantSource = style.getSourceAs<GeoJsonSource>("restaurant-source")
                        if (restaurantLocation != null) {
                            restaurantSource?.setGeoJson(
                                Point.fromLngLat(restaurantLocation.lng, restaurantLocation.lat)
                            )
                        }

                        // Update User Source
                        val userSource = style.getSourceAs<GeoJsonSource>("user-source")
                        if (userLocation != null) {
                            userSource?.setGeoJson(
                                Point.fromLngLat(userLocation.lng, userLocation.lat)
                            )
                        }

                        // Update Route Source
                        val routeSource = style.getSourceAs<GeoJsonSource>("route-source")
                        if (routePoints.isNotEmpty()) {
                            val lineString = LineString.fromLngLats(routePoints)
                            routeSource?.setGeoJson(FeatureCollection.fromFeatures(arrayOf(Feature.fromGeometry(lineString))))
                        } else {
                             routeSource?.setGeoJson(FeatureCollection.fromFeatures(arrayOf()))
                        }
                    }

                    // Update Camera (3D View)
                    if (restaurantLocation != null && userLocation != null) {
                        val bounds = LatLngBounds.Builder()
                            .include(LatLng(restaurantLocation.lat, restaurantLocation.lng))
                            .include(LatLng(userLocation.lat, userLocation.lng))
                            .apply {
                                routePoints.forEach { p -> include(LatLng(p.latitude(), p.longitude())) }
                            }
                            .build()

                        // Animate camera to bounds with tilt
                        map.easeCamera(
                            CameraUpdateFactory.newLatLngBounds(bounds, 150),
                            1000
                        )
                        
                        // Apply Tilt (Pitch) separately or after bounds to ensure 3D effect
                         // But newLatLngBounds might reset tilt. 
                         // Better approach: Get target position and manually set camera with tilt
                         // For simplicity, we just set a fixed tilt on every update for now or rely on user interaction
                         // Let's force a tilt after the bounds update if possible, 
                         // or construct a CameraPosition that includes the target center but with tilt.
                         
                         // Note: newLatLngBounds sets tilt to 0. 
                         // We can look at the center of the bounds and set camera there with tilt + zoom
                         /*
                         val center = bounds.center
                         val zoom = map.cameraPosition.zoom // might be inaccurate if bounds calc happens async
                         // So we stick to bounds for coverage, and user can tilt, OR we try to enforce it.
                         // Let's just create a CameraPosition for a "Follow" mode if user is moving.
                         */
                    }
                }
            }
        )
        
        // --- Overlay Cards (Same as before) ---
        
        // Restaurant Address Card
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

        // Distance indicator
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
                    .align(Alignment.BottomCenter) // Moved to bottom for better view
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
        
        // 3D Toggle / Re-center Button could be added here
        
        // Add a "3D Mode" simple effect: automatically tilt when map loads? 
        // We can do this in the `update` block cautiously.
        LaunchedEffect(mapboxMap) {
            mapboxMap?.cameraPosition = CameraPosition.Builder()
                .tilt(60.0) // Set 60 degree tilt
                .build()
        }
    }
}

/**
 * Create a PIN/Teardrop marker icon (Safe drawing)
 */
private fun createPinMarkerIcon(context: Context, colorValue: Int): Bitmap {
    val size = (48 * context.resources.displayMetrics.density).toInt()
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    
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
 * Returns list of Points (Mapbox/MapLibre format)
 */
suspend fun calculateRoute(
    startLat: Double,
    startLon: Double,
    endLat: Double,
    endLon: Double
): List<Point> = withContext(Dispatchers.IO) {
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
                    
                    val points = mutableListOf<Point>()
                    for (i in 0 until coordinates.length()) {
                        val coord = coordinates.getJSONArray(i)
                        val lon = coord.getDouble(0)
                        val lat = coord.getDouble(1)
                        points.add(Point.fromLngLat(lon, lat))
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
