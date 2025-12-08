package com.example.damprojectfinal.user.feautre_order.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.damprojectfinal.core.dto.order.OrderItemResponse
import com.example.damprojectfinal.core.dto.order.OrderResponse
import com.example.damprojectfinal.core.dto.order.OrderStatus
import com.example.damprojectfinal.user.feautre_order.viewmodel.OrderViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private const val BASE_URL = "http://10.0.2.2:3000/"

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
    
    // Load orders when screen opens
    LaunchedEffect(userId) {
        orderViewModel.loadOrdersByUser(userId)
    }
    
    val order = remember(ordersState, orderId) {
        ordersState?.find { it._id == orderId }
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
                val instant = Instant.parse(order.createdAt)
                instant.atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("MMM dd, yyyy • hh:mm a"))
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

                Spacer(Modifier.height(4.dp))

                // Ingredients
                if (!item.chosenIngredients.isNullOrEmpty()) {
                    Text(
                        "Ingredients:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DarkText
                    )
                    item.chosenIngredients.forEach { ingredient ->
                        Text(
                            "  ${if (ingredient.isDefault) "✓" else "+"} ${ingredient.name}",
                            fontSize = 11.sp,
                            color = LightGrayText
                        )
                    }
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
