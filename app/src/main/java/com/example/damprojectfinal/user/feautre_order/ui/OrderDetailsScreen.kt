package com.example.damprojectfinal.user.feautre_order.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.DisposableEffect
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.*
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.damprojectfinal.core.dto.order.OrderItemResponse
import com.example.damprojectfinal.core.dto.order.OrderResponse
import com.example.damprojectfinal.core.dto.order.OrderStatus
import com.example.damprojectfinal.core.dto.order.ChosenIngredientResponse
import com.example.damprojectfinal.core.dto.menu.IntensityType
import com.example.damprojectfinal.user.feautre_order.viewmodel.OrderViewModel
import com.example.damprojectfinal.user.feautre_order.viewmodel.LocationTrackingViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.damprojectfinal.core.api.BaseUrlProvider
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val PrimaryColor = Color(0xFFFFC107)
private val BackgroundLight = Color(0xFFF9FAFB)
private val CardBackground = Color.White
private val DarkText = Color(0xFF1F2937)
private val LightGrayText = Color(0xFF9CA3AF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailsScreen(
    orderId: String,
    navController: NavController,
    orderViewModel: OrderViewModel,
    userId: String  // Add userId parameter
) {
    val ordersState by orderViewModel.orders.collectAsState()
    val isLoading by orderViewModel.loading.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    // Location tracking ViewModel
    val locationViewModel: LocationTrackingViewModel = viewModel()
    val locationState by locationViewModel.state.collectAsState()
    
    // Load orders when screen opens
    LaunchedEffect(userId) {
        orderViewModel.loadOrdersByUser(userId)
    }
    
    val order = remember(ordersState, orderId) {
        ordersState?.find { it._id == orderId }
    }
    
    // Connect to location tracking if order is PENDING/CONFIRMED and EAT_IN/TAKEAWAY
    LaunchedEffect(order) {
        order?.let { ord ->
            if ((ord.status == com.example.damprojectfinal.core.dto.order.OrderStatus.PENDING || 
                 ord.status == com.example.damprojectfinal.core.dto.order.OrderStatus.CONFIRMED) &&
                (ord.orderType == com.example.damprojectfinal.core.dto.order.OrderType.EAT_IN ||
                 ord.orderType == com.example.damprojectfinal.core.dto.order.OrderType.TAKEAWAY)) {
                // Connect to order tracking WebSocket
                locationViewModel.connectToOrder(ord._id, userId, "user")
            }
        }
    }
    
    // Cleanup on screen exit
    DisposableEffect(orderId) {
        onDispose {
            locationViewModel.disconnect()
        }
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            TopAppBar(
                title = { Text("Order Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    // Only show delete button if order status is CONFIRMED or PENDING
                    if (order != null && (order.status == com.example.damprojectfinal.core.dto.order.OrderStatus.CONFIRMED || 
                        order.status == com.example.damprojectfinal.core.dto.order.OrderStatus.PENDING)) {
                        IconButton(
                            onClick = { showDeleteDialog = true }
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete Order",
                                tint = Color(0xFFEF4444)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CardBackground
                )
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryColor)
            }
        } else if (order == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Order not found", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Section
                item {
                    OrderHeaderCard(order)
                }
                
                // Live Tracking Map (for EAT_IN and TAKEAWAY orders)
                if ((order.orderType == com.example.damprojectfinal.core.dto.order.OrderType.EAT_IN ||
                     order.orderType == com.example.damprojectfinal.core.dto.order.OrderType.TAKEAWAY) &&
                    (order.status == com.example.damprojectfinal.core.dto.order.OrderStatus.PENDING ||
                     order.status == com.example.damprojectfinal.core.dto.order.OrderStatus.CONFIRMED)) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = CardBackground),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column {
                                // Map Header
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Live Location Tracking",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = DarkText
                                    )
                                    // Connection status
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(androidx.compose.foundation.shape.CircleShape)
                                                .background(
                                                    if (locationState.isConnected) Color(0xFF10B981)
                                                    else Color(0xFFEF4444)
                                                )
                                        )
                                        Text(
                                            text = if (locationState.isConnected) "Connected" else "Connecting...",
                                            fontSize = 12.sp,
                                            color = if (locationState.isConnected) Color(0xFF10B981) else Color(0xFFEF4444)
                                        )
                                    }
                                }
                                
                                // Map
                                OrderTrackingMap(
                                    restaurantLocation = locationState.restaurantLocation?.let {
                                        RestaurantLocation(
                                            lat = it.lat,
                                            lng = it.lng,
                                            name = it.name ?: it.address,
                                            address = it.address
                                        )
                                    },
                                    userLocation = locationState.currentLocation?.let {
                                        UserLocation(
                                            lat = it.lat,
                                            lng = it.lng,
                                            accuracy = it.accuracy
                                        )
                                    },
                                    distanceFormatted = locationState.distanceFormatted,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(400.dp)
                                )
                                
                                // Tracking info
                                if (locationState.isSharing) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "📍 Sharing your location",
                                            fontSize = 14.sp,
                                            color = Color(0xFF10B981),
                                            fontWeight = FontWeight.Medium
                                        )
                                        TextButton(
                                            onClick = { locationViewModel.stopSharingLocation() }
                                        ) {
                                            Text("Stop Sharing", color = Color(0xFFEF4444))
                                        }
                                    }
                                } else if (locationState.isConnected) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        TextButton(
                                            onClick = { locationViewModel.startSharingLocation() }
                                        ) {
                                            Text("Start Sharing Location", color = PrimaryColor)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Items Section
                item {
                    Text(
                        "Order Items",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = DarkText
                    )
                }

                items(order.items) { item ->
                    OrderItemDetailCard(item)
                }

                // Summary Section
                item {
                    OrderSummaryCard(order)
                }

                // Spacing at bottom
                item {
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
        
        // Delete Confirmation Dialog
        if (showDeleteDialog && order != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Order", fontWeight = FontWeight.Bold) },
                text = { Text("Are you sure you want to delete order #${order._id.takeLast(8)}? This action cannot be undone.") },
                confirmButton = {
                    Button(
                        onClick = {
                            orderViewModel.deleteOrder(
                                orderId = order._id,
                                onSuccess = {
                                    showDeleteDialog = false
                                    navController.popBackStack()
                                },
                                onError = { error ->
                                    showDeleteDialog = false
                                    // Error will be shown via ViewModel error state
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEF4444),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteDialog = false }
                    ) {
                        Text("Cancel", color = LightGrayText)
                    }
                }
            )
        }
    }
}

@Composable
fun OrderHeaderCard(order: OrderResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Order ID
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Order #${order._id.takeLast(8)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = DarkText
                )

                // Status Badge
                Surface(
                    color = when (order.status) {
                        OrderStatus.COMPLETED -> Color(0xFF10B981)
                        OrderStatus.CANCELLED, OrderStatus.REFUSED -> Color(0xFFEF4444)
                        OrderStatus.PENDING -> Color(0xFFF59E0B)
                        OrderStatus.CONFIRMED -> Color(0xFF3B82F6)
                    }.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = order.status.name,
                        color = when (order.status) {
                            OrderStatus.COMPLETED -> Color(0xFF10B981)
                            OrderStatus.CANCELLED, OrderStatus.REFUSED -> Color(0xFFEF4444)
                            OrderStatus.PENDING -> Color(0xFFF59E0B)
                            OrderStatus.CONFIRMED -> Color(0xFF3B82F6)
                        },
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Date & Time
            val formattedDate = try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                dateFormat.timeZone = TimeZone.getTimeZone("UTC")
                val createdDate = dateFormat.parse(order.createdAt)
                val displayFormat = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.US)
                displayFormat.format(createdDate ?: Date())
            } catch (e: Exception) {
                "Date unavailable"
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📅 ", fontSize = 16.sp)
                Text(formattedDate, color = LightGrayText, fontSize = 14.sp)
            }

            Spacer(Modifier.height(8.dp))

            // Order Type
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    when (order.orderType) {
                        com.example.damprojectfinal.core.dto.order.OrderType.DELIVERY -> "🚚 "
                        com.example.damprojectfinal.core.dto.order.OrderType.TAKEAWAY -> "🛍️ "
                        com.example.damprojectfinal.core.dto.order.OrderType.EAT_IN -> "🍽️ "
                    },
                    fontSize = 16.sp
                )
                Text(
                    when (order.orderType) {
                        com.example.damprojectfinal.core.dto.order.OrderType.DELIVERY -> "Delivery"
                        com.example.damprojectfinal.core.dto.order.OrderType.TAKEAWAY -> "Takeaway"
                        com.example.damprojectfinal.core.dto.order.OrderType.EAT_IN -> "Dine-in"
                    },
                    color = LightGrayText,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun OrderItemDetailCard(item: OrderItemResponse) {
    // Check if deal is applied
    val hasDeal = item.discountPercentage != null && item.discountPercentage > 0 && item.originalPrice != null
    val originalTotalPrice = if (hasDeal) (item.originalPrice!! * item.quantity) else null
    val discountedTotalPrice = item.calculatedPrice * item.quantity
    val savings = if (hasDeal && originalTotalPrice != null) (originalTotalPrice - discountedTotalPrice) else null
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Item Image with discount badge
            Box {
                AsyncImage(
                    model = BaseUrlProvider.getFullImageUrl(item.image),
                    contentDescription = item.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                
                // Discount badge overlay
                if (hasDeal) {
                    androidx.compose.material3.Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFFF5722),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                    ) {
                        Text(
                            text = "-${item.discountPercentage}%",
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Item Details
            Column(modifier = Modifier.weight(1f)) {
                // Name and Quantity
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        item.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = DarkText
                    )
                    Text(
                        "x${item.quantity}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = LightGrayText
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Ingredients with Intensity Display
                if (!item.chosenIngredients.isNullOrEmpty()) {
                    OrderIngredientsListWithIntensity(item.chosenIngredients)
                }

                // Options
                if (!item.chosenOptions.isNullOrEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Options:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DarkText
                    )
                    item.chosenOptions.forEach { option ->
                        Text(
                            "  + ${option.name} (+${"%.2f".format(option.price)} DT)",
                            fontSize = 11.sp,
                            color = PrimaryColor
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Item Price with deal info
                if (hasDeal && originalTotalPrice != null) {
                    // Original price (strikethrough)
                    Text(
                        "${"%.2f".format(originalTotalPrice)} TND",
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = Color.Gray,
                        style = androidx.compose.ui.text.TextStyle(
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                        )
                    )
                    
                    // Discounted price with savings
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "${"%.2f".format(discountedTotalPrice)} TND",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = PrimaryColor
                        )
                        
                        if (savings != null && savings > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "(-${"%.2f".format(savings)} TND)",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = Color(0xFF4CAF50)
                            )
                        }
                    }
                } else {
                    // Normal price (no deal)
                    Text(
                        "${"%.2f".format(discountedTotalPrice)} TND",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = PrimaryColor
                    )
                }
            }
        }
    }
}

@Composable
fun OrderSummaryCard(order: OrderResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Order Summary",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = DarkText
            )

            Spacer(Modifier.height(12.dp))

            // Subtotal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Subtotal", color = LightGrayText, fontSize = 14.sp)
                Text(
                    "${"%.2f".format(order.totalPrice)} TND",
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = DarkText
                )
            }

            Spacer(Modifier.height(16.dp))
            Divider()
            Spacer(Modifier.height(16.dp))

            // Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Total",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = DarkText
                )
                Text(
                    "${"%.2f".format(order.totalPrice)} TND",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = PrimaryColor
                )
            }
        }
    }
}

// ---------------- Animated Bouncing Icon for Intensity ----------------
@Composable
fun AnimatedBouncingIcon(
    emoji: String,
    fontSize: TextUnit,
    delayMillis: Int = 0
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bounce")
    
    val bounceOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 600,
                delayMillis = delayMillis,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce_offset"
    )
    
    Text(
        text = emoji,
        fontSize = fontSize,
        modifier = Modifier
            .graphicsLayer {
                translationY = bounceOffset
            }
    )
}

// ---------------- Helper: Get Emoji for Intensity Type ----------------
fun getEmojiForIntensityType(type: IntensityType?): String {
    return when (type) {
        IntensityType.COFFEE -> "☕"
        IntensityType.HARISSA -> "🌶️"
        IntensityType.SAUCE -> "🍯"
        IntensityType.SPICE -> "🌿"
        IntensityType.SUGAR -> "🍬"
        IntensityType.SALT -> "🧂"
        IntensityType.PEPPER -> "🫚"
        IntensityType.CHILI -> "🌶️"
        IntensityType.GARLIC -> "🧄"
        IntensityType.LEMON -> "🍋"
        else -> "⭐" // Default for CUSTOM or null
    }
}

// ---------------- Helper: Get Intensity Color ----------------
fun getIntensityColor(intensityType: IntensityType?, intensityColorHex: String?, intensityValue: Float): Color {
    // If backend provided a color, parse it and adjust by intensity
    if (intensityColorHex != null) {
        try {
            val baseColor = Color(android.graphics.Color.parseColor(intensityColorHex))
            // Adjust brightness based on intensity
            return Color(
                red = (baseColor.red * (0.5f + intensityValue * 0.5f)).coerceIn(0f, 1f),
                green = (baseColor.green * (0.5f + intensityValue * 0.5f)).coerceIn(0f, 1f),
                blue = (baseColor.blue * (0.5f + intensityValue * 0.5f)).coerceIn(0f, 1f)
            )
        } catch (e: Exception) {
            // Fall through to default colors
        }
    }
    
    // Default colors based on type
    return when (intensityType) {
        IntensityType.COFFEE -> {
            Color(
                red = (0.2f + intensityValue * 0.2f).coerceIn(0f, 1f),
                green = (0.15f + intensityValue * 0.1f).coerceIn(0f, 1f),
                blue = (0.1f + intensityValue * 0.1f).coerceIn(0f, 1f)
            )
        }
        IntensityType.HARISSA, IntensityType.CHILI -> {
            Color(
                red = (0.6f + intensityValue * 0.4f).coerceIn(0f, 1f),
                green = (0.2f - intensityValue * 0.2f).coerceIn(0f, 1f),
                blue = (0.2f - intensityValue * 0.2f).coerceIn(0f, 1f)
            )
        }
        IntensityType.SAUCE -> {
            Color(
                red = (0.9f + intensityValue * 0.1f).coerceIn(0f, 1f),
                green = (0.6f + intensityValue * 0.2f).coerceIn(0f, 1f),
                blue = (0.2f - intensityValue * 0.1f).coerceIn(0f, 1f)
            )
        }
        IntensityType.SPICE -> {
            Color(
                red = (0.8f + intensityValue * 0.2f).coerceIn(0f, 1f),
                green = (0.5f + intensityValue * 0.2f).coerceIn(0f, 1f),
                blue = (0.2f - intensityValue * 0.1f).coerceIn(0f, 1f)
            )
        }
        IntensityType.SUGAR -> {
            Color(
                red = (0.95f + intensityValue * 0.05f).coerceIn(0f, 1f),
                green = (0.9f + intensityValue * 0.1f).coerceIn(0f, 1f),
                blue = (0.7f + intensityValue * 0.2f).coerceIn(0f, 1f)
            )
        }
        IntensityType.SALT -> {
            Color(
                red = (0.85f + intensityValue * 0.1f).coerceIn(0f, 1f),
                green = (0.85f + intensityValue * 0.1f).coerceIn(0f, 1f),
                blue = (0.9f + intensityValue * 0.1f).coerceIn(0f, 1f)
            )
        }
        IntensityType.PEPPER -> {
            Color(
                red = (0.2f + intensityValue * 0.2f).coerceIn(0f, 1f),
                green = (0.2f + intensityValue * 0.2f).coerceIn(0f, 1f),
                blue = (0.2f + intensityValue * 0.2f).coerceIn(0f, 1f)
            )
        }
        IntensityType.GARLIC -> {
            Color(
                red = (0.95f + intensityValue * 0.05f).coerceIn(0f, 1f),
                green = (0.95f + intensityValue * 0.05f).coerceIn(0f, 1f),
                blue = (0.9f + intensityValue * 0.1f).coerceIn(0f, 1f)
            )
        }
        IntensityType.LEMON -> {
            Color(
                red = (0.95f + intensityValue * 0.05f).coerceIn(0f, 1f),
                green = (0.9f + intensityValue * 0.1f).coerceIn(0f, 1f),
                blue = (0.4f - intensityValue * 0.2f).coerceIn(0f, 1f)
            )
        }
        IntensityType.CUSTOM, null -> {
            Color(
                red = (0.6f + intensityValue * 0.2f).coerceIn(0f, 1f),
                green = (0.6f + intensityValue * 0.2f).coerceIn(0f, 1f),
                blue = (0.6f + intensityValue * 0.2f).coerceIn(0f, 1f)
            )
        }
    }
}

// Custom Slider with Vertical Bar Thumb Design
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomIntensitySlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    activeColor: Color,
    inactiveColor: Color,
    modifier: Modifier = Modifier
) {
    val trackHeight = 6.dp
    val thumbWidth = 3.dp
    val thumbHeight = 20.dp
    val density = LocalDensity.current
    
    BoxWithConstraints(modifier = modifier.height(thumbHeight)) {
        val trackWidthPx = with(density) { maxWidth.toPx() }
        val thumbWidthPx = with(density) { thumbWidth.toPx() }
        val thumbOffsetPx = value * (trackWidthPx - thumbWidthPx)
        val thumbOffset = with(density) { thumbOffsetPx.toDp() }
        val activeTrackWidth = with(density) { (thumbOffsetPx + thumbWidthPx / 2).toDp() }
        
        // Inactive track (background) - rounded
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(trackHeight)
                .clip(RoundedCornerShape(8.dp))
                .background(inactiveColor)
                .align(Alignment.Center)
        )
        
        // Active track (filled portion) - rounded left side
        Box(
            modifier = Modifier
                .width(activeTrackWidth)
                .height(trackHeight)
                .clip(
                    RoundedCornerShape(
                        topStart = 8.dp,
                        bottomStart = 8.dp,
                        topEnd = if (value >= 1f) 8.dp else 0.dp,
                        bottomEnd = if (value >= 1f) 8.dp else 0.dp
                    )
                )
                .background(activeColor)
                .align(Alignment.CenterStart)
        )
        
        // Vertical bar thumb
        Box(
            modifier = Modifier
                .width(thumbWidth)
                .height(thumbHeight)
                .offset(x = thumbOffset)
                .background(activeColor)
                .align(Alignment.CenterStart)
        )
        
        // Small dot at the end of inactive track
        if (value < 1f) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(inactiveColor)
                    .offset(x = maxWidth - 4.dp)
                    .align(Alignment.CenterEnd)
            )
        }
        
        // Invisible touch target for interaction (disabled for read-only)
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..1f,
            modifier = Modifier.fillMaxSize(),
            enabled = false,
            colors = SliderDefaults.colors(
                thumbColor = Color.Transparent,
                activeTrackColor = Color.Transparent,
                inactiveTrackColor = Color.Transparent
            ),
            thumb = {
                Box(modifier = Modifier.size(0.dp))
            },
            track = {
                Box(modifier = Modifier.fillMaxSize())
            }
        )
    }
}

// ---------------- Ingredients Display with Intensity for Orders ----------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderIngredientsListWithIntensity(ingredients: List<ChosenIngredientResponse>) {
    if (ingredients.isEmpty()) return
    
    Text(
        text = "Ingredients:",
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        color = DarkText,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    
    ingredients.forEach { ingredient ->
        if (ingredient.intensityType != null) {
            // Convert Double to Float, or use default if null
            val intensityValue = ingredient.intensityValue?.toFloat() ?: 0.5f
            
            // Debug logging to see actual values
            android.util.Log.d("OrderDetails", "Ingredient: ${ingredient.name}, intensityValue (Double): ${ingredient.intensityValue}, intensityValue (Float): $intensityValue")
            
            val primaryColor = getIntensityColor(ingredient.intensityType, ingredient.intensityColor, intensityValue)
            val baseEmoji = getEmojiForIntensityType(ingredient.intensityType)
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    // Ingredient name
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(PrimaryColor)
                            )
                            Text(
                                text = ingredient.name,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = DarkText
                            )
                            if (ingredient.isDefault) {
                                Text(
                                    text = "(Default)",
                                    fontSize = 10.sp,
                                    color = LightGrayText
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Intensity slider (read-only display)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CustomIntensitySlider(
                            value = intensityValue,
                            onValueChange = { }, // Read-only
                            activeColor = primaryColor,
                            inactiveColor = primaryColor.copy(alpha = 0.3f),
                            modifier = Modifier.weight(1f)
                        )
                        // Intensity icons on the right
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(start = 12.dp)
                        ) {
                            when {
                                intensityValue >= 0.8f -> {
                                    AnimatedBouncingIcon(baseEmoji, fontSize = 16.sp, delayMillis = 0)
                                    if (ingredient.intensityType == IntensityType.HARISSA || ingredient.intensityType == IntensityType.CHILI) {
                                        AnimatedBouncingIcon("🔥", fontSize = 16.sp, delayMillis = 100)
                                    }
                                }
                                intensityValue >= 0.3f -> {
                                    AnimatedBouncingIcon(baseEmoji, fontSize = 14.sp, delayMillis = 0)
                                    AnimatedBouncingIcon(baseEmoji, fontSize = 14.sp, delayMillis = 150)
                                }
                                else -> {
                                    AnimatedBouncingIcon(baseEmoji, fontSize = 14.sp, delayMillis = 0)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Regular ingredient without intensity
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "  ${if (ingredient.isDefault) "✓" else "+"} ${ingredient.name}",
                    fontSize = 11.sp,
                    color = LightGrayText
                )
            }
        }
    }
}
