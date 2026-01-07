package com.example.damprojectfinal.professional.feature_order.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.damprojectfinal.professional.feature_order.viewmodel.MultiOrderTrackingViewModel
import com.example.damprojectfinal.ui.theme.AppDarkText
import com.example.damprojectfinal.user.feautre_order.ui.OrderTrackingMap
import com.example.damprojectfinal.user.feautre_order.ui.RestaurantLocation
import com.example.damprojectfinal.user.feautre_order.ui.UserLocation
import kotlinx.coroutines.launch

/**
 * Screen showing all users sharing their location on one map
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllUsersTrackingScreen(
    navController: NavController,
    professionalId: String
) {
    val viewModel: MultiOrderTrackingViewModel = viewModel()
    val state by viewModel.state.collectAsState()
    
    // Connect to all orders when screen opens
    LaunchedEffect(professionalId) {
        viewModel.connectToAllOrders(professionalId)
    }
    
    // Cleanup on screen exit
    DisposableEffect(Unit) {
        onDispose {
            viewModel.disconnect()
        }
    }
    
    // For now, show first user's location on map (we'll enhance map later)
    val firstUser = state.activeUsers.values.firstOrNull()
    
    // Bottom sheet state
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "All Users Tracking",
                            color = AppDarkText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        if (state.activeUsers.isNotEmpty()) {
                            Text(
                                "${state.activeUsers.size} user${if (state.activeUsers.size > 1) "s" else ""} sharing",
                                color = Color.Gray,
                                fontSize = 12.sp
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
                                    if (state.isConnected) Color(0xFF10B981) 
                                    else Color(0xFFEF4444)
                                )
                        )
                        Text(
                            if (state.isConnected) "Connected" else "Offline",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF9FAFB),
        floatingActionButton = {
            // Button to open bottom sheet
            if (state.activeUsers.isNotEmpty()) {
                FloatingActionButton(
                    onClick = {
                        showBottomSheet = true
                        scope.launch {
                            sheetState.expand()
                        }
                    },
                    containerColor = Color(0xFFFFC107),
                    contentColor = Color.Black
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.People,
                            contentDescription = "View Users",
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            "${state.activeUsers.size}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        if (state.error != null && !state.isConnected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = "Error",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        state.error ?: "Unknown error",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            // Full screen map
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Full screen map
                OrderTrackingMap(
                    restaurantLocation = state.restaurantLocation?.let {
                        RestaurantLocation(
                            lat = it.lat,
                            lng = it.lng,
                            name = it.name,
                            address = it.address
                        )
                    },
                    userLocation = firstUser?.let {
                        UserLocation(
                            lat = it.lat,
                            lng = it.lng,
                            accuracy = it.accuracy?.toFloat()
                        )
                    },
                    distanceFormatted = firstUser?.distanceFormatted,
                    modifier = Modifier.fillMaxSize(),
                    showDistance = true
                )
                
                // Status overlay
                if (!state.isConnected || state.activeUsers.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.95f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            when {
                                !state.isConnected -> {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp,
                                        color = Color(0xFFEF4444)
                                    )
                                    Text(
                                        "Connecting...",
                                        color = Color.Gray,
                                        fontSize = 14.sp
                                    )
                                }
                                state.activeUsers.isEmpty() -> {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = "Waiting",
                                        tint = Color(0xFF3B82F6),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Column {
                                        Text(
                                            "No Active Users",
                                            fontWeight = FontWeight.Bold,
                                            color = AppDarkText,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            "Waiting for users to share location",
                                            color = Color.Gray,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Bottom Sheet Drawer
            if (showBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = {
                        showBottomSheet = false
                        scope.launch {
                            sheetState.hide()
                        }
                    },
                    sheetState = sheetState,
                    containerColor = Color.White,
                    dragHandle = {
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.Gray.copy(alpha = 0.3f))
                                .padding(vertical = 12.dp)
                        )
                    }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        // Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Active Users",
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = AppDarkText
                            )
                            Text(
                                "${state.activeUsers.size}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Color(0xFFFFC107)
                            )
                        }
                        
                        Divider(modifier = Modifier.padding(bottom = 16.dp))
                        
                        // Users list
                        if (state.activeUsers.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        Icons.Default.LocationOff,
                                        contentDescription = "No users",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Text(
                                        "No users sharing location",
                                        color = Color.Gray,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 500.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(bottom = 24.dp)
                            ) {
                                items(state.activeUsers.values.toList()) { user ->
                                    UserTrackingCard(
                                        user = user,
                                        onClick = {
                                            // Navigate to order details
                                            navController.navigate("pro_order_details/${user.orderId}")
                                            // Close bottom sheet
                                            scope.launch {
                                                sheetState.hide()
                                            }
                                            showBottomSheet = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserTrackingCard(
    user: com.example.damprojectfinal.professional.feature_order.viewmodel.SharingUser,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // User icon with live indicator
            Box {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF3B82F6).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "User",
                        tint = Color(0xFF3B82F6),
                        modifier = Modifier.size(24.dp)
                    )
                }
                // Live indicator
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981))
                        .align(Alignment.TopEnd)
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    user.userName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = AppDarkText
                )
                if (user.distanceFormatted != null) {
                    Text(
                        "📍 ${user.distanceFormatted}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Text(
                    "Order #${user.orderId.takeLast(6)}",
                    fontSize = 11.sp,
                    color = Color(0xFF9CA3AF)
                )
            }
            
            // Live badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981))
                )
                Text(
                    "LIVE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF10B981)
                )
            }
        }
    }
}

