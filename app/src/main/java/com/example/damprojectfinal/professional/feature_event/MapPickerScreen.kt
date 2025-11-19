package com.example.damprojectfinal.professional.feature_event

import android.content.Context
import android.location.Geocoder
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.util.Locale

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val name: String = ""
)

@Composable
fun MapPickerScreen(
    initialLocation: LocationData? = null,
    onLocationSelected: (LocationData) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Position par défaut : Tunis
    var currentLocation by remember {
        mutableStateOf(
            initialLocation ?: LocationData(
                latitude = 36.8065,
                longitude = 10.1815,
                name = "Tunis, Tunisie"
            )
        )
    }

    var locationName by remember { mutableStateOf(currentLocation.name) }
    var isLoadingAddress by remember { mutableStateOf(false) }

    // Initialiser OSMDroid
    DisposableEffect(Unit) {
        Configuration.getInstance().load(
            context,
            context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )
        onDispose { }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // La carte OSM
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(15.0)
                    controller.setCenter(
                        GeoPoint(currentLocation.latitude, currentLocation.longitude)
                    )

                    // Écouter les changements de position
                    addMapListener(object : org.osmdroid.events.MapListener {
                        override fun onScroll(event: org.osmdroid.events.ScrollEvent?): Boolean {
                            val center = mapCenter as GeoPoint
                            currentLocation = LocationData(
                                latitude = center.latitude,
                                longitude = center.longitude
                            )

                            // Géocoder avec debounce
                            scope.launch {
                                kotlinx.coroutines.delay(500)
                                isLoadingAddress = true
                                locationName = reverseGeocode(ctx, center.latitude, center.longitude)
                                isLoadingAddress = false
                            }
                            return true
                        }

                        override fun onZoom(event: org.osmdroid.events.ZoomEvent?): Boolean {
                            return true
                        }
                    })
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Pin fixe au centre
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "Pin",
            tint = Color.Red,
            modifier = Modifier
                .size(50.dp)
                .align(Alignment.Center)
                .offset(y = (-25).dp)
        )

        // Info en haut
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (isLoadingAddress) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Recherche de l'adresse...")
                    }
                } else {
                    Text(
                        text = locationName,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = String.format(
                            "%.4f, %.4f",
                            currentLocation.latitude,
                            currentLocation.longitude
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }

        // Instruction
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black.copy(alpha = 0.7f)
            )
        ) {
            Text(
                text = "📍 Déplacez la carte pour positionner le pin",
                color = Color.White,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // Boutons en bas
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Annuler")
                }

                Button(
                    onClick = {
                        onLocationSelected(
                            currentLocation.copy(name = locationName)
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Confirmer")
                }
            }
        }
    }
}

// Fonction de géocodage inversé
suspend fun reverseGeocode(
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
                "Lieu sélectionné"
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return@withContext "Lieu sélectionné"
}