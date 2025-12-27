package com.example.damprojectfinal.user.feautre_order.ui

import androidx.compose.animation.AnimatedVisibility
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
import com.example.damprojectfinal.ui.theme.AppDarkText
import com.example.damprojectfinal.ui.theme.AppPrimaryRed
import com.example.damprojectfinal.user.feautre_order.viewmodel.LocationTrackingViewModel
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController

/**
 * Live Order Tracking Screen - User Side
 * Shows user's live location being shared with restaurant
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveOrderTrackingScreen(
    navController: NavController,
    orderId: String,
    userId: String,
    onStopTracking: () -> Unit = {}
) {
    val context = LocalContext.current
    val locationViewModel: LocationTrackingViewModel = viewModel()
    val trackingState by locationViewModel.state.collectAsState()
    
    var showStopDialog by remember { mutableStateOf(false) }
    
    // Connect to order tracking on screen load
    LaunchedEffect(orderId, userId) {
        locationViewModel.connectToOrder(orderId, userId, "user")
        // Start sharing will be triggered by the button or automatically if permission granted
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
                            "Live Tracking",
                            color = AppDarkText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            "Sharing location with restaurant",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
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
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(
                                if (trackingState.isConnected) Color(0xFF10B981) 
                                else Color(0xFFEF4444)
                            )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            LiveTrackingBottomBar(
                isSharing = trackingState.isSharing,
                isConnected = trackingState.isConnected,
                onStartSharing = {
                    locationViewModel.startSharingLocation()
                },
                onStopSharing = {
                    showStopDialog = true
                }
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
            
            // Status Card (Top, below TopAppBar)
            AnimatedVisibility(
                visible = !trackingState.isSharing || !trackingState.isConnected,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            ) {
                StatusCard(
                    isConnected = trackingState.isConnected,
                    isSharing = trackingState.isSharing,
                    error = trackingState.error
                )
            }
            
            // Stop Sharing Confirmation Dialog
            if (showStopDialog) {
                AlertDialog(
                    onDismissRequest = { showStopDialog = false },
                    title = { Text("Stop Sharing Location?", fontWeight = FontWeight.Bold) },
                    text = { 
                        Text("The restaurant will no longer be able to see your live location. You can start sharing again anytime.")
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                locationViewModel.stopSharingLocation()
                                showStopDialog = false
                                Toast.makeText(
                                    context,
                                    "Location sharing stopped",
                                    Toast.LENGTH_SHORT
                                ).show()
                                onStopTracking()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppPrimaryRed
                            )
                        ) {
                            Text("Stop Sharing")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showStopDialog = false }) {
                            Text("Cancel", color = Color.Gray)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun StatusCard(
    isConnected: Boolean,
    isSharing: Boolean,
    error: String?
) {
    Card(
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
                error != null -> {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Error",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            "Error",
                            fontWeight = FontWeight.Bold,
                            color = AppDarkText,
                            fontSize = 14.sp
                        )
                        Text(
                            error,
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
                !isConnected -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = AppPrimaryRed
                    )
                    Text(
                        "Connecting to server...",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
                !isSharing -> {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "Info",
                        tint = Color(0xFF3B82F6),
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            "Not Sharing",
                            fontWeight = FontWeight.Bold,
                            color = AppDarkText,
                            fontSize = 14.sp
                        )
                        Text(
                            "Tap 'Start Sharing' to begin",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LiveTrackingBottomBar(
    isSharing: Boolean,
    isConnected: Boolean,
    onStartSharing: () -> Unit,
    onStopSharing: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            if (isSharing) {
                // Sharing Active - Show Stop Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Pulsing indicator
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                    
                    Text(
                        "Sharing location",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.weight(1f)
                    )
                    
                    OutlinedButton(
                        onClick = onStopSharing,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = AppPrimaryRed
                        )
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Stop",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Stop Sharing")
                    }
                }
            } else {
                // Not Sharing - Show Start Button
                Button(
                    onClick = onStartSharing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = isConnected,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981),
                        disabledContainerColor = Color.LightGray
                    )
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = "Start",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (isConnected) "Start Sharing Location" else "Connecting...",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
