package com.example.damprojectfinal.professional.feature_order.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.damprojectfinal.ui.theme.AppDarkText
import com.example.damprojectfinal.ui.theme.AppPrimaryRed
import com.example.damprojectfinal.user.feautre_order.ui.OrderTrackingMap
import com.example.damprojectfinal.user.feautre_order.ui.RestaurantLocation
import com.example.damprojectfinal.user.feautre_order.ui.UserLocation
import com.example.damprojectfinal.user.feautre_order.viewmodel.LocationTrackingViewModel

/**
 * Professional Order Tracking Screen - Professional Side
 * Shows customer's live location approaching the restaurant
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfessionalOrderTrackingScreen(
    navController: NavController,
    orderId: String,
    professionalId: String
) {
    val locationViewModel: LocationTrackingViewModel = viewModel()
    val trackingState by locationViewModel.state.collectAsState()
    
    // Connect to order tracking as professional
    LaunchedEffect(orderId, professionalId) {
        android.util.Log.d("ProTracking", "🏪 Professional tracking order: $orderId")
        locationViewModel.connectToOrder(orderId, professionalId, "pro")
    }
    
    // Cleanup on screen exit
    DisposableEffect(Unit) {
        onDispose {
            locationViewModel.disconnect()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Customer Tracking",
                            color = AppDarkText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        if (trackingState.distanceFormatted != null) {
                            Text(
                                "📍 ${trackingState.distanceFormatted} away",
                                color = Color(0xFF10B981),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = AppDarkText
                        )
                    }
                },
                actions = {
                    // Connection status indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(
                                    if (trackingState.isConnected) Color(0xFF10B981) 
                                    else Color(0xFFEF4444)
                                )
                        )
                        Text(
                            if (trackingState.isConnected) "Connected" else "Offline",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF9FAFB)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Map View
            OrderTrackingMap(
                restaurantLocation = trackingState.restaurantLocation?.let {
                    RestaurantLocation(
                        lat = it.lat,
                        lng = it.lng,
                        name = it.name,
                        address = it.address
                    )
                },
                userLocation = trackingState.currentLocation?.let {
                    UserLocation(
                        lat = it.lat,
                        lng = it.lng,
                        accuracy = it.accuracy
                    )
                },
                distanceFormatted = trackingState.distanceFormatted,
                showDistance = true,
                modifier = Modifier.fillMaxSize()
            )
            
            // Status Card
            if (!trackingState.isConnected || trackingState.currentLocation == null) {
                Card(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.95f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        when {
                            !trackingState.isConnected -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp,
                                    color = AppPrimaryRed
                                )
                                Text(
                                    "Connecting to order...",
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            }
                            trackingState.currentLocation == null -> {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = "Waiting",
                                    tint = Color(0xFF3B82F6),
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        "Waiting for Customer",
                                        fontWeight = FontWeight.Bold,
                                        color = AppDarkText,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        "Customer hasn't started sharing location yet",
                                        color = Color.Gray,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Customer Info Card (bottom)
            if (trackingState.currentLocation != null) {
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF3B82F6).copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "Customer",
                                    tint = Color(0xFF3B82F6),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Customer Location",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = AppDarkText
                                )
                                if (trackingState.distanceFormatted != null) {
                                    Text(
                                        trackingState.distanceFormatted!!,
                                        fontSize = 14.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                            
                            // Live indicator
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF10B981))
                                )
                                Text(
                                    "LIVE",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981)
                                )
                            }
                        }
                        
                        Divider()
                        
                        // Additional info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            InfoItem(
                                icon = Icons.Default.LocationOn,
                                label = "Accuracy",
                                value = trackingState.currentLocation?.accuracy?.let { 
                                    "±${it.toInt()}m" 
                                } ?: "Unknown"
                            )
                            
                            InfoItem(
                                icon = Icons.Default.GpsFixed,
                                label = "Coordinates",
                                value = trackingState.currentLocation?.let {
                                    "${String.format("%.4f", it.lat)}, ${String.format("%.4f", it.lng)}"
                                } ?: "N/A"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = Color.Gray,
                modifier = Modifier.size(16.dp)
            )
            Text(
                label,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
        Text(
            value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = AppDarkText,
            modifier = Modifier.padding(start = 20.dp)
        )
    }
}
