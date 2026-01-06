package com.example.damprojectfinal.feature_auth.ui

import android.content.Context
import android.location.Geocoder
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.damprojectfinal.professional.feature_event.LocationData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.maplibre.geojson.Point
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import org.maplibre.android.camera.CameraUpdateFactory
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

/**
 * Search addresses using Nominatim API (FREE - No API key required)
 */
suspend fun searchAddressesOSM(
    context: Context,
    query: String
): List<LocationData> = withContext(Dispatchers.IO) {
    if (query.isBlank()) {
        return@withContext emptyList()
    }
    
    try {
        val encodedQuery = URLEncoder.encode(query.trim(), "UTF-8")
        val urlString = "https://nominatim.openstreetmap.org/search" +
                "?q=$encodedQuery" +
                "&format=json" +
                "&limit=10" +
                "&addressdetails=1" +
                "&extratags=1"
        
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("User-Agent", "FoodyzApp/1.0 (Android)")
        connection.setRequestProperty("Accept", "application/json")
        connection.setRequestProperty("Accept-Language", "en,fr,ar")
        connection.connectTimeout = 15000
        connection.readTimeout = 15000

        val responseCode = connection.responseCode
        
        if (responseCode == HttpURLConnection.HTTP_OK) {
            val reader = BufferedReader(InputStreamReader(connection.inputStream, "UTF-8"))
            val response = StringBuilder()
            var line: String?
            
            while (reader.readLine().also { line = it } != null) {
                response.append(line)
            }
            reader.close()

            val jsonArray = JSONArray(response.toString())
            val results = mutableListOf<LocationData>()

            for (i in 0 until jsonArray.length()) {
                try {
                    val item = jsonArray.getJSONObject(i)
                    val lat = item.getDouble("lat")
                    val lon = item.getDouble("lon")
                    val displayName = item.optString("display_name", "")
                    
                    val address = item.optJSONObject("address")
                    var shortName = displayName
                    
                    if (address != null) {
                        val nameParts = mutableListOf<String>()
                        address.optString("name")?.takeIf { it.isNotBlank() }?.let { nameParts.add(it) }
                        address.optString("road")?.takeIf { it.isNotBlank() }?.let { nameParts.add(it) }
                        address.optString("house_number")?.takeIf { it.isNotBlank() }?.let { nameParts.add(it) }
                        address.optString("neighbourhood")?.takeIf { it.isNotBlank() }?.let { nameParts.add(it) }
                        address.optString("suburb")?.takeIf { it.isNotBlank() }?.let { nameParts.add(it) }
                        address.optString("city")?.takeIf { it.isNotBlank() }?.let { nameParts.add(it) }
                        address.optString("town")?.takeIf { it.isNotBlank() }?.let { nameParts.add(it) }
                        address.optString("state")?.takeIf { it.isNotBlank() }?.let { nameParts.add(it) }
                        address.optString("country")?.takeIf { it.isNotBlank() }?.let { nameParts.add(it) }
                        
                        if (nameParts.isNotEmpty()) {
                            shortName = nameParts.joinToString(", ")
                        }
                    }
                    
                    val finalName = if (shortName.isNotBlank() && shortName != displayName) {
                        "$shortName ($displayName)"
                    } else {
                        displayName.ifBlank { "Location at $lat, $lon" }
                    }

                    results.add(
                        LocationData(
                            latitude = lat,
                            longitude = lon,
                            name = finalName
                        )
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            return@withContext results
        } else {
            android.util.Log.e("OSMLocationPicker", "Nominatim API error: HTTP $responseCode")
            if (responseCode == 429) {
                android.util.Log.w("OSMLocationPicker", "Rate limit exceeded. Please wait a moment.")
            }
        }
    } catch (e: java.net.SocketTimeoutException) {
        android.util.Log.e("OSMLocationPicker", "Search timeout: ${e.message}")
    } catch (e: java.net.UnknownHostException) {
        android.util.Log.e("OSMLocationPicker", "No internet connection: ${e.message}")
    } catch (e: Exception) {
        android.util.Log.e("OSMLocationPicker", "Search error: ${e.message}", e)
    }

    return@withContext emptyList()
}

/**
 * Reverse geocode using Android Geocoder (FREE)
 */
suspend fun reverseGeocodeOSM(
    context: Context,
    latitude: Double,
    longitude: Double
): String = withContext(Dispatchers.IO) {
    try {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)

        if (!addresses.isNullOrEmpty()) {
            val address = addresses[0]
            val components = mutableListOf<String>()

            address.featureName?.let { components.add(it) }
            address.thoroughfare?.let {
                if (!components.contains(it)) components.add(it)
            }
            address.locality?.let { components.add(it) }
            address.countryName?.let { components.add(it) }

            return@withContext if (components.isNotEmpty()) {
                components.joinToString(", ")
            } else {
                "Selected Location"
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return@withContext "Selected Location"
}

/**
 * OpenStreetMap Location Picker with Search (FREE - No API Key Required)
 * Uses Nominatim API for geocoding (completely free)
 */
@Composable
fun OSMLocationPicker(
    initialLocation: LocationData? = null,
    onLocationSelected: (LocationData) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var currentLocation by remember {
        mutableStateOf(
            initialLocation ?: LocationData(
                latitude = 36.8065,
                longitude = 10.1815,
                name = "Tunis, Tunisia"
            )
        )
    }

    var locationName by remember { mutableStateOf(currentLocation.name) }
    var isLoadingAddress by remember { mutableStateOf(false) }
    
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<LocationData>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var showSearchResults by remember { mutableStateOf(false) }
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var mapboxMap by remember { mutableStateOf<org.maplibre.android.maps.MapLibreMap?>(null) }

    remember { MapLibre.getInstance(context) }

    LaunchedEffect(searchQuery) {
        if (searchQuery.isBlank()) {
            searchResults = emptyList()
            showSearchResults = false
            isSearching = false
            return@LaunchedEffect
        }

        delay(500)
        
        if (searchQuery.isNotBlank()) {
            isSearching = true
            showSearchResults = true
            
            try {
                val results = searchAddressesOSM(context, searchQuery)
                searchResults = results
                
                if (results.isEmpty()) {
                    android.util.Log.d("OSMLocationPicker", "No results found for: $searchQuery")
                } else {
                    android.util.Log.d("OSMLocationPicker", "Found ${results.size} results for: $searchQuery")
                }
            } catch (e: Exception) {
                android.util.Log.e("OSMLocationPicker", "Search failed: ${e.message}", e)
                searchResults = emptyList()
            } finally {
                isSearching = false
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = true
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        onCreate(null)
                        getMapAsync { map ->
                            map.setStyle("https://tiles.openfreemap.org/styles/liberty") { style ->
                                // Setup marker icon
                                val markerIcon = createPinMarkerIcon(ctx, android.graphics.Color.RED)
                                style.addImage("marker-icon", markerIcon)

                                // Setup source
                                style.addSource(GeoJsonSource("location-source", 
                                    Point.fromLngLat(currentLocation.longitude, currentLocation.latitude)))

                                // Setup layer
                                style.addLayer(
                                    SymbolLayer("location-layer", "location-source").withProperties(
                                        PropertyFactory.iconImage("marker-icon"),
                                        PropertyFactory.iconAnchor(Property.ICON_ANCHOR_BOTTOM),
                                        PropertyFactory.iconAllowOverlap(true)
                                    )
                                )
                            }
                            map.uiSettings.isAttributionEnabled = false
                            map.uiSettings.isLogoEnabled = false
                            
                            val cameraPosition = CameraPosition.Builder()
                                .target(LatLng(currentLocation.latitude, currentLocation.longitude))
                                .zoom(15.0)
                                .build()
                            map.cameraPosition = cameraPosition
                            
                            // Listener for camera movement
                            map.addOnCameraIdleListener {
                                val center = map.cameraPosition.target ?: return@addOnCameraIdleListener
                                currentLocation = LocationData(
                                    latitude = center.latitude,
                                    longitude = center.longitude
                                )
                                
                                // Update source
                                map.getStyle { style ->
                                    val source = style.getSourceAs<GeoJsonSource>("location-source")
                                    source?.setGeoJson(Point.fromLngLat(center.longitude, center.latitude))
                                }

                                scope.launch {
                                    delay(500)
                                    isLoadingAddress = true
                                    locationName = reverseGeocodeOSM(ctx, center.latitude, center.longitude)
                                    isLoadingAddress = false
                                }
                            }
                            
                            mapboxMap = map
                        }
                        mapViewRef = this
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { mapView ->
                     // Handle updates if needed, primarily relying on map async callback
                     mapView.onResume()
                     
                     mapboxMap?.let { map ->
                         map.getStyle { style ->
                             val source = style.getSourceAs<GeoJsonSource>("location-source")
                             source?.setGeoJson(Point.fromLngLat(currentLocation.longitude, currentLocation.latitude))
                         }
                     }
                }
            )

            // Zoom Controls (Bottom Right - More Visible)
            Card(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 100.dp, end = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Zoom In Button
                    Card(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF3B82F6).copy(alpha = 0.1f)
                        ),
                        onClick = {
                            val currentZoom = mapboxMap?.cameraPosition?.zoom ?: 15.0
                             mapboxMap?.animateCamera(
                                CameraUpdateFactory.zoomTo(currentZoom + 1),
                                300
                            )
                        }
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Zoom In",
                                    tint = Color(0xFF3B82F6),
                                    modifier = Modifier.size(28.dp)
                                )
                                Text(
                                    text = "In",
                                    fontSize = 9.sp,
                                    color = Color(0xFF3B82F6),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Zoom Out Button
                    Card(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF3B82F6).copy(alpha = 0.1f)
                        ),
                        onClick = {
                             val currentZoom = mapboxMap?.cameraPosition?.zoom ?: 15.0
                             mapboxMap?.animateCamera(
                                CameraUpdateFactory.zoomTo(currentZoom - 1),
                                300
                            )
                        }
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Zoom Out",
                                    tint = Color(0xFF3B82F6),
                                    modifier = Modifier.size(28.dp)
                                )
                                Text(
                                    text = "Out",
                                    fontSize = 9.sp,
                                    color = Color(0xFF3B82F6),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        placeholder = { Text("Search for an address...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search"
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear"
                                    )
                                }
                            }
                            if (isSearching) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = Color.Gray
                        )
                    )

                    if (showSearchResults) {
                        if (isSearching) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Searching...",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        } else if (searchResults.isEmpty() && searchQuery.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No results found for \"$searchQuery\"",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        } else if (searchResults.isNotEmpty()) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 300.dp)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                items(searchResults) { location ->
                                    SearchResultItem(
                                        location = location,
                                        onClick = {
                                            val targetLocation = LocationData(
                                                latitude = location.latitude,
                                                longitude = location.longitude,
                                                name = location.name
                                            )
                                            currentLocation = targetLocation
                                            locationName = location.name
                                            searchQuery = location.name
                                            showSearchResults = false
                                            
                                            // Animate camera
                                            mapboxMap?.animateCamera(
                                                CameraUpdateFactory.newLatLngZoom(
                                                    LatLng(location.latitude, location.longitude),
                                                    17.0
                                                ),
                                                1500
                                            )
                                            
                                            // Update source
                                            mapboxMap?.getStyle { style ->
                                                val source = style.getSourceAs<GeoJsonSource>("location-source")
                                                source?.setGeoJson(Point.fromLngLat(location.longitude, location.latitude))
                                            }
                                            
                                            scope.launch {
                                                isLoadingAddress = true
                                                val fullAddress = reverseGeocodeOSM(context, location.latitude, location.longitude)
                                                locationName = fullAddress
                                                currentLocation = targetLocation.copy(name = fullAddress)
                                                isLoadingAddress = false
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Current Location Info Card (Top Center - Below Search Bar)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 80.dp) // Position below search bar
                    .align(Alignment.TopCenter),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        if (isLoadingAddress) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Loading address...",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        } else {
                            Text(
                                text = locationName.ifBlank { "Selected Location" },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black,
                                maxLines = 1
                            )
                            Text(
                                text = String.format(
                                    "%.6f, %.6f",
                                    currentLocation.latitude,
                                    currentLocation.longitude
                                ),
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            // Instructions (Above buttons)
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 80.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Black.copy(alpha = 0.7f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Drag map or search to select location",
                        color = Color.White,
                        fontSize = 11.sp
                    )
                }
            }

            // Action Buttons
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.98f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Medium)
                    }

                    Button(
                        onClick = {
                            onLocationSelected(
                                currentLocation.copy(name = locationName)
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3B82F6)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Confirm", fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultItem(
    location: LocationData,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Place,
                contentDescription = null,
                tint = Color(0xFF3B82F6),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = location.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                Text(
                    text = String.format(
                        "%.4f, %.4f",
                        location.latitude,
                        location.longitude
                    ),
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
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
