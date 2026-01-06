package com.example.damprojectfinal.user.feautre_order.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.damprojectfinal.user.feautre_order.viewmodel.LocationTrackingViewModel
import kotlinx.coroutines.delay
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point

@Composable
fun OrderNavigationScreen(
    orderId: String,
    navController: NavController,
    userId: String,
    viewModel: LocationTrackingViewModel = viewModel() // Use shared or new VM
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val state by viewModel.state.collectAsState()

    // Initialize MapLibre
    remember { MapLibre.getInstance(context) }
    
    var mapboxMap by remember { mutableStateOf<MapLibreMap?>(null) }
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var routePoints by remember { mutableStateOf<List<Point>>(emptyList()) }
    
    // Connect to order tracking if not already connected
    LaunchedEffect(orderId) {
        if (!state.isConnected) {
            viewModel.connectToOrder(orderId, userId, "user")
        }
    }

    var isCalculatingRoute by remember { mutableStateOf(false) }
    
    // Start local tracking for navigation
    LaunchedEffect(Unit) {
        // Request local tracking to ensure we have "My Location" for the route
        viewModel.startLocalTracking()
    }

    // Lifecycle observer for MapView
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            mapViewRef?.let { mapView ->
                when (event) {
                    Lifecycle.Event.ON_START -> mapView.onStart()
                    Lifecycle.Event.ON_RESUME -> mapView.onResume()
                    Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                    Lifecycle.Event.ON_STOP -> mapView.onStop()
                    Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                    else -> {}
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapViewRef?.onDestroy()
        }
    }

    // Reuse route calculation from OrderTrackingMap (copy logic for now to avoid dependency cycle or extract to Utils)
    LaunchedEffect(state.restaurantLocation, state.currentLocation) {
        if (state.restaurantLocation != null && state.currentLocation != null) {
            // Re-calculate route here or pass it if possible. 
            // For now, simple recalculation
             val route = calculateRoute(
                state.currentLocation!!.lat, state.currentLocation!!.lng,
                state.restaurantLocation!!.lat, state.restaurantLocation!!.lng
            )
            routePoints = route
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val restaurantMarkerIcon = remember { createPinMarkerIcon(context, android.graphics.Color.parseColor("#EF4444")) }
        val userMarkerIcon = remember { createPinMarkerIcon(context, android.graphics.Color.parseColor("#3B82F6")) }

        // 1. Full Screen Map
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    onCreate(null)
                    getMapAsync { map ->
                        mapboxMap = map
                        map.setStyle("https://tiles.openfreemap.org/styles/liberty") { style ->
                            style.addImage("nav-restaurant-icon", restaurantMarkerIcon)
                            style.addImage("nav-user-icon", userMarkerIcon)
                            
                            // Sources
                            style.addSource(GeoJsonSource("nav-restaurant-source"))
                            style.addSource(GeoJsonSource("nav-user-source"))
                            style.addSource(GeoJsonSource("nav-route-source"))

                            // Layers
                            // Route: thicker line for navigation
                             style.addLayer(
                                LineLayer("nav-route-layer", "nav-route-source").withProperties(
                                    PropertyFactory.lineColor(android.graphics.Color.parseColor("#3B82F6")),
                                    PropertyFactory.lineWidth(8f), // Thicker
                                    PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                                    PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND)
                                )
                            )
                            
                            // Markers
                            style.addLayer(SymbolLayer("nav-restaurant-layer", "nav-restaurant-source").withProperties(
                                PropertyFactory.iconImage("nav-restaurant-icon"),
                                PropertyFactory.iconAnchor(Property.ICON_ANCHOR_BOTTOM),
                                PropertyFactory.iconAllowOverlap(true),
                                PropertyFactory.iconIgnorePlacement(true)
                            ))
                            style.addLayer(SymbolLayer("nav-user-layer", "nav-user-source").withProperties(
                                PropertyFactory.iconImage("nav-user-icon"),
                                PropertyFactory.iconAnchor(Property.ICON_ANCHOR_BOTTOM),
                                PropertyFactory.iconAllowOverlap(true),
                                PropertyFactory.iconIgnorePlacement(true)
                            ))
                        }
                        
                        map.uiSettings.isCompassEnabled = false 
                        map.uiSettings.isAttributionEnabled = false
                        map.uiSettings.isLogoEnabled = false
                    }
                    mapViewRef = this
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { _ ->
                mapboxMap?.let { map ->
                    map.getStyle { style ->
                         // Update Sources
                        val rSource = style.getSourceAs<GeoJsonSource>("nav-restaurant-source")
                        state.restaurantLocation?.let { 
                            if (it.lat != 0.0 && it.lng != 0.0) {
                                rSource?.setGeoJson(Point.fromLngLat(it.lng, it.lat))
                            }
                        }
                        
                        val uSource = style.getSourceAs<GeoJsonSource>("nav-user-source")
                        state.currentLocation?.let {
                            if (it.lat != 0.0 && it.lng != 0.0) {
                                uSource?.setGeoJson(Point.fromLngLat(it.lng, it.lat))
                            }
                        }
                        
                        val routeSource = style.getSourceAs<GeoJsonSource>("nav-route-source")
                        if (routePoints.isNotEmpty()) {
                             val lineString = LineString.fromLngLats(routePoints)
                             routeSource?.setGeoJson(FeatureCollection.fromFeatures(arrayOf(Feature.fromGeometry(lineString))))
                        }
                    }

                    // Camera Tracking Logic (Navigation Mode)
                     if (state.currentLocation != null && state.restaurantLocation != null) {
                        val userLat = state.currentLocation!!.lat
                        val userLng = state.currentLocation!!.lng
                        val destLat = state.restaurantLocation!!.lat
                        val destLng = state.restaurantLocation!!.lng
                        
                        android.util.Log.d("OrderNavigation", "📍 Camera Update Check:")
                        android.util.Log.d("OrderNavigation", "  User: $userLat, $userLng")
                        android.util.Log.d("OrderNavigation", "  Dest: $destLat, $destLng")
                        
                        // Only update if coordinates are valid (not 0,0)
                        if (userLat != 0.0 && userLng != 0.0 && destLat != 0.0 && destLng != 0.0) {
                            
                             // Strategy: 
                             // 1. If map just loaded (first few seconds), show OVERVIEW (User + Dest)
                             // 2. After that, switch to FOLLOW mode (User + Bearing)
                             
                             // For now, we stick to FOLLOW mode as user requested "navigation view"
                             // But we ensure the route is visible by ensuring the tilt isn't too extreme relative to zoom
                            
                            // Calculate bearing (heading) from user to destination
                            val bearing = calculateBearing(userLat, userLng, destLat, destLng)
                            
                            val cameraPosition = CameraPosition.Builder()
                                .target(LatLng(userLat, userLng))
                                .zoom(17.5) // Optimal for navigation
                                .tilt(60.0) // 3D Tilt
                                .bearing(bearing) // Face the destination
                                .build()
                                
                            // Use easeCamera for smoother updates (1s duration)
                            map.easeCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 1000)
                        }
                    } else if (state.restaurantLocation != null) {
                        // Fallback: if user location is missing, center on restaurant
                         val destLat = state.restaurantLocation!!.lat
                         val destLng = state.restaurantLocation!!.lng
                         if (destLat != 0.0 && destLng != 0.0) {
                             map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(destLat, destLng), 15.0), 1000)
                         }
                    }
                }
            }
        )

        // 2. Google Maps Style UI Overlays
        
        // Top Direction Card (Green)
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
             Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF00675b)), // Google Maps Green
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "Direction",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Head towards",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = state.restaurantLocation?.name ?: "Restaurant",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // Close Button (Top Left - Floating)
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .padding(top = 180.dp, start = 16.dp) // Below the green card
                .clip(CircleShape)
                .background(Color.White)
                .size(48.dp)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Black)
        }

        // Re-center Button (Bottom Left)
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 180.dp, start = 16.dp) // Above bottom card
        ) {
             Button(
                onClick = {
                    // Trigger 3D Animation (Recenter & Tilt)
                    mapboxMap?.let { map ->
                        val currentLoc = state.currentLocation
                        val restLoc = state.restaurantLocation
                        if (currentLoc != null && restLoc != null && 
                            currentLoc.lat != 0.0 && currentLoc.lng != 0.0 && 
                            restLoc.lat != 0.0 && restLoc.lng != 0.0) {
                            
                            val bearing = calculateBearing(currentLoc.lat, currentLoc.lng, restLoc.lat, restLoc.lng)
                            
                            val cameraPosition = CameraPosition.Builder()
                                .target(LatLng(currentLoc.lat, currentLoc.lng))
                                .zoom(17.5)
                                .tilt(60.0)
                                .bearing(bearing)
                                .build()
                            
                            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 1500)
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Icon(
                    Icons.Default.Navigation, 
                    contentDescription = "Re-center", 
                    tint = Color(0xFF00675b),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Re-center", color = Color(0xFF00675b), fontWeight = FontWeight.Bold)
            }
        }

        // 3. Bottom Card (Trip Info)
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 0.dp), // Check styling
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // ETA Section (Green & Big)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "15 min", // Mock ETA
                            color = Color(0xFF00675b),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = state.distanceFormatted ?: "-- km",
                            color = Color.Gray,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    
                    // Exit Button or chevron
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Exit",
                        tint = Color.Gray,
                        modifier = Modifier
                            .size(32.dp)
                            .clickable { navController.popBackStack() }
                            .padding(4.dp)
                    )
                }
                
                Spacer(Modifier.height(8.dp))
                
                Text(
                    text = "Fastest route now due to traffic conditions",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }
    }
}

// Reuse helper function (duplicated from OrderTrackingMap.kt for simplicity in this file)
private fun createPinMarkerIcon(context: Context, colorValue: Int): Bitmap {
    val size = (48 * context.resources.displayMetrics.density).toInt()
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint().apply {
        isAntiAlias = true
        color = colorValue
        style = Paint.Style.FILL
    }
    val cx = size / 2f
    val cy = size / 2.6f
    val radius = size / 2.8f
    val shadowPaint = Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.BLACK
        alpha = 50
    }
    canvas.drawCircle(cx, size - 4f, 6f, shadowPaint)
    val trianglePath = android.graphics.Path()
    trianglePath.moveTo(cx - radius * 0.9f, cy + radius * 0.1f)
    trianglePath.lineTo(cx + radius * 0.9f, cy + radius * 0.1f)
    trianglePath.lineTo(cx, size.toFloat())
    trianglePath.close()
    canvas.drawPath(trianglePath, paint)
    canvas.drawCircle(cx, cy, radius, paint)
    val holePaint = Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.WHITE
    }
    canvas.drawCircle(cx, cy, radius * 0.4f, holePaint)
    return bitmap
}

private fun calculateBearing(startLat: Double, startLng: Double, endLat: Double, endLng: Double): Double {
    val startLatRad = Math.toRadians(startLat)
    val startLngRad = Math.toRadians(startLng)
    val endLatRad = Math.toRadians(endLat)
    val endLngRad = Math.toRadians(endLng)

    val dLng = endLngRad - startLngRad

    val y = Math.sin(dLng) * Math.cos(endLatRad)
    val x = Math.cos(startLatRad) * Math.sin(endLatRad) -
            Math.sin(startLatRad) * Math.cos(endLatRad) * Math.cos(dLng)

    val bearingRad = Math.atan2(y, x)
    return (Math.toDegrees(bearingRad) + 360) % 360
}
