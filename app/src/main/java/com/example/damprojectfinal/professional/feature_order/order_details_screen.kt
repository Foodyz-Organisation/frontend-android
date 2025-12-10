package com.example.damprojectfinal.professional.feature_order

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.damprojectfinal.core.dto.order.UpdateOrderStatusRequest
import com.example.damprojectfinal.core.dto.menu.IntensityType
import com.example.damprojectfinal.user.feautre_order.viewmodel.OrderViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private const val BASE_URL = "http://10.0.2.2:3000/"

private val PrimaryColor = Color(0xFFFFC107)
private val BackgroundLight = Color(0xFFF9FAFB)
private val CardBackground = Color.White
private val DarkText = Color(0xFF1F2937)
private val LightGrayText = Color(0xFF9CA3AF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfessionalOrderDetailsScreen(
    orderId: String,
    navController: NavController,
    orderViewModel: OrderViewModel
) {
    val singleOrderState by orderViewModel.singleOrder.collectAsState()
    val isLoading by orderViewModel.loading.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    // Load order by ID when screen opens
    LaunchedEffect(orderId) {
        orderViewModel.loadOrderById(orderId)
    }
    
    val order = singleOrderState

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
                    if (order != null && (order.status == OrderStatus.CONFIRMED || 
                        order.status == OrderStatus.PENDING)) {
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
                    ProfessionalOrderHeaderCard(order, orderViewModel)
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
                    ProfessionalOrderItemDetailCard(item)
                }

                // Summary Section
                item {
                    ProfessionalOrderSummaryCard(order)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfessionalOrderHeaderCard(order: OrderResponse, orderViewModel: OrderViewModel) {
    var expanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
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
                val currentDate = Date()
                val diffInMillis = currentDate.time - (createdDate?.time ?: 0)
                val minutes = diffInMillis / (1000 * 60)
                when {
                    minutes < 1 -> "Received just now"
                    minutes < 60 -> "Received $minutes minutes ago"
                    else -> "Received ${minutes / 60} hours ago"
                }
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
            
            Spacer(Modifier.height(16.dp))
            
            // Status Update Dropdown (for professionals)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Order Status:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkText
                )

                val validStatuses = getValidStatusTransitions(order.status)
                val canChangeStatus = validStatuses.isNotEmpty()

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { if (canChangeStatus) expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = getStatusDisplayName(order.status),
                        onValueChange = {},
                        readOnly = true,
                        enabled = canChangeStatus,
                        trailingIcon = {
                            if (canChangeStatus) {
                                Icon(
                                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Dropdown"
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Final State",
                                    tint = Color.Gray
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = getStatusColor(order.status).copy(alpha = 0.1f),
                            unfocusedContainerColor = getStatusColor(order.status).copy(alpha = 0.1f),
                            disabledContainerColor = getStatusColor(order.status).copy(alpha = 0.1f),
                            unfocusedBorderColor = getStatusColor(order.status),
                            focusedBorderColor = getStatusColor(order.status),
                            disabledBorderColor = getStatusColor(order.status).copy(alpha = 0.5f),
                            disabledTextColor = getStatusColor(order.status)
                        ),
                        modifier = Modifier
                            .menuAnchor()
                            .width(160.dp)
                            .height(56.dp),
                        textStyle = LocalTextStyle.current.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = getStatusColor(order.status)
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        validStatuses.forEach { status ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .background(getStatusColor(status))
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = getStatusDisplayName(status),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                },
                                onClick = {
                                    if (status != order.status) {
                                        scope.launch {
                                            orderViewModel.updateOrderStatus(
                                                order._id,
                                                UpdateOrderStatusRequest(status)
                                            )
                                            // Reload order after update
                                            orderViewModel.loadOrderById(order._id)
                                        }
                                    }
                                    expanded = false
                                },
                                enabled = status != order.status
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfessionalOrderItemDetailCard(item: OrderItemResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Item Image
            AsyncImage(
                model = if (item.image.isNullOrEmpty()) null else BASE_URL + item.image,
                contentDescription = item.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

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
                    ProfessionalOrderIngredientsListWithIntensity(item.chosenIngredients)
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
                            "  + ${option.name} (+${"%.2f".format(option.price)} TND)",
                            fontSize = 11.sp,
                            color = PrimaryColor
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Item Price
                Text(
                    "${"%.2f".format(item.calculatedPrice * item.quantity)} TND",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = PrimaryColor
                )
            }
        }
    }
}

@Composable
fun ProfessionalOrderSummaryCard(order: OrderResponse) {
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

// ---------------- Helper Functions ----------------
private fun getStatusColor(status: OrderStatus): Color {
    return when (status) {
        OrderStatus.PENDING -> Color(0xFFFFA500)
        OrderStatus.CONFIRMED -> Color(0xFF4CAF50)
        OrderStatus.COMPLETED -> Color(0xFF2196F3)
        OrderStatus.CANCELLED -> Color(0xFF9E9E9E)
        OrderStatus.REFUSED -> Color(0xFFF44336)
    }
}

private fun getStatusDisplayName(status: OrderStatus): String {
    return when (status) {
        OrderStatus.PENDING -> "Pending"
        OrderStatus.CONFIRMED -> "Confirmed"
        OrderStatus.COMPLETED -> "Completed"
        OrderStatus.CANCELLED -> "Cancelled"
        OrderStatus.REFUSED -> "Refused"
    }
}

private fun getValidStatusTransitions(currentStatus: OrderStatus): List<OrderStatus> {
    return when (currentStatus) {
        OrderStatus.PENDING -> listOf(
            OrderStatus.CONFIRMED,
            OrderStatus.REFUSED,
            OrderStatus.CANCELLED
        )
        OrderStatus.CONFIRMED -> listOf(
            OrderStatus.COMPLETED,
            OrderStatus.CANCELLED
        )
        OrderStatus.COMPLETED -> emptyList()
        OrderStatus.CANCELLED -> emptyList()
        OrderStatus.REFUSED -> emptyList()
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
        else -> "⭐"
    }
}

// ---------------- Helper: Get Intensity Color ----------------
fun getIntensityColor(intensityType: IntensityType?, intensityColorHex: String?, intensityValue: Float): Color {
    if (intensityColorHex != null) {
        try {
            val baseColor = Color(android.graphics.Color.parseColor(intensityColorHex))
            return Color(
                red = (baseColor.red * (0.5f + intensityValue * 0.5f)).coerceIn(0f, 1f),
                green = (baseColor.green * (0.5f + intensityValue * 0.5f)).coerceIn(0f, 1f),
                blue = (baseColor.blue * (0.5f + intensityValue * 0.5f)).coerceIn(0f, 1f)
            )
        } catch (e: Exception) {
            // Fall through
        }
    }
    
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
fun ProfessionalOrderIngredientsListWithIntensity(ingredients: List<ChosenIngredientResponse>) {
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
                                    .clip(CircleShape)
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

