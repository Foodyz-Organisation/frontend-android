package com.example.damprojectfinal.user.feautre_order.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*
import com.example.damprojectfinal.R
import com.example.damprojectfinal.UserRoutes
import com.example.damprojectfinal.core.api.BaseUrlProvider
import com.example.damprojectfinal.core.dto.order.OrderResponse
import com.example.damprojectfinal.core.dto.order.OrderStatus
import com.example.damprojectfinal.user.common._component.TopAppBar
import com.example.damprojectfinal.user.feautre_order.viewmodel.OrderViewModel
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape

// ----------- COLORS -----------
private val BackgroundLight = Color(0xFFF9FAFB)
private val AppDarkText = Color(0xFF1F2937)
private val LightBackground = Color.White // Consistent component color

// ===============================================================
// ORDER HISTORY SCREEN WITH TopAppBar
// ===============================================================

@Composable
fun OrderHistoryScreen(
    navController: NavController,
    orderViewModel: OrderViewModel,
    userId: String,
    onOrderClick: (String) -> Unit,
    onReclamationClick: (String) -> Unit,
    onLogout: () -> Unit
) {
    val ordersState by orderViewModel.orders.collectAsState()
    val isLoading by orderViewModel.loading.collectAsState()
    var isSearchActive by remember { mutableStateOf(false) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        orderViewModel.loadOrdersByUser(userId)
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            TopAppBar(
                navController = navController,
                currentRoute = UserRoutes.ORDERS_ROUTE, // "orders_history_route"
                openDrawer = {}, // if you have a drawer, implement it
                onSearchClick = { isSearchActive = true },
                currentUserId = userId,
                onProfileClick = { userId ->
                    // navigate to profile screen if needed
                },
                onReelsClick = {
                    navController.navigate(UserRoutes.REELS_SCREEN)
                },
                onLogoutClick = onLogout,
                showNavBar = false
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    color = Color.White,
                    shadowElevation = 16.dp,
                    shape = RoundedCornerShape(28.dp),
                    tonalElevation = 4.dp
                ) {
                    com.example.damprojectfinal.user.common._component.SecondaryNavBar(
                        navController = navController,
                        currentRoute = UserRoutes.ORDERS_ROUTE,
                        onReelsClick = {
                            navController.navigate(UserRoutes.REELS_SCREEN)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )
                }
            }
        },
        content = { paddingValues ->
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
            ) {
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AppDarkText)
                    }
                } else if (ordersState.isNullOrEmpty()) {
                    EmptyOrdersView(PaddingValues(0.dp))
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Delete All Button (only show if there are orders)
                        if (!ordersState.isNullOrEmpty()) {
                            item {
                                Button(
                                    onClick = { showDeleteAllDialog = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFEF4444),
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        "Delete Completed Orders",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }

                        items(ordersState!!, key = { it._id }) { order ->
                            OrderItemCard(order, onOrderClick, onReclamationClick)
                        }
                    }
                }
            }

            // --- Optional: Dynamic Search Overlay ---
            if (isSearchActive) {
                // Import and use your DynamicSearchOverlay if needed
            }

            // Delete All Confirmation Dialog
            if (showDeleteAllDialog) {
                val completedOrdersCount = ordersState?.count { it.status == com.example.damprojectfinal.core.dto.order.OrderStatus.COMPLETED } ?: 0
                AlertDialog(
                    onDismissRequest = { showDeleteAllDialog = false },
                    title = { Text("Delete Completed Orders", fontWeight = FontWeight.Bold) },
                    text = { Text("Are you sure you want to delete all completed orders? Only orders with status 'COMPLETED' will be deleted. This action cannot be undone.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                orderViewModel.deleteAllOrdersByUser(
                                    userId = userId,
                                    onSuccess = {
                                        showDeleteAllDialog = false
                                    },
                                    onError = { error ->
                                        showDeleteAllDialog = false
                                    }
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFEF4444),
                                contentColor = Color.White
                            )
                        ) {
                            Text("Delete All")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showDeleteAllDialog = false }
                        ) {
                            Text("Cancel", color = Color.Gray)
                        }
                    }
                )
            }
        }
    )
}

// ===============================================================
// EMPTY STATE VIEW
// ===============================================================
@Composable
fun EmptyOrdersView(paddingValues: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            val composition by rememberLottieComposition(
                LottieCompositionSpec.RawRes(R.raw.empty)
            )

            LottieAnimation(
                composition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.size(240.dp)
            )

            Text(
                text = "No past orders",
                color = AppDarkText,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "You haven’t placed any orders yet. Start exploring delicious food!",
                color = Color.Gray,
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ===============================================================
// ORDER CARD
// ===============================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderItemCard(
    order: OrderResponse,
    onClick: (String) -> Unit,
    onReclamationClick: (String) -> Unit
) {
    Card(
        onClick = { onClick(order._id) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = LightBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // HEADER
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Show menu items instead of order ID
                    val itemsSummary = order.items.take(2).joinToString(", ") {
                        "${it.name} x${it.quantity}"
                    } + if (order.items.size > 2) ", +${order.items.size - 2} more" else ""

                    Text(
                        text = itemsSummary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = AppDarkText
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Show order type badge
                    Surface(
                        color = when (order.orderType) {
                            com.example.damprojectfinal.core.dto.order.OrderType.DELIVERY -> Color(0xFF8B5CF6)
                            com.example.damprojectfinal.core.dto.order.OrderType.TAKEAWAY -> Color(0xFF10B981)
                            com.example.damprojectfinal.core.dto.order.OrderType.EAT_IN -> Color(0xFF3B82F6)
                        }.copy(alpha = 0.15f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = when (order.orderType) {
                                com.example.damprojectfinal.core.dto.order.OrderType.DELIVERY -> "🚚 Delivery"
                                com.example.damprojectfinal.core.dto.order.OrderType.TAKEAWAY -> "🛍️ Takeaway"
                                com.example.damprojectfinal.core.dto.order.OrderType.EAT_IN -> "🍽️ Dine-in"
                            },
                            color = when (order.orderType) {
                                com.example.damprojectfinal.core.dto.order.OrderType.DELIVERY -> Color(0xFF8B5CF6)
                                com.example.damprojectfinal.core.dto.order.OrderType.TAKEAWAY -> Color(0xFF10B981)
                                com.example.damprojectfinal.core.dto.order.OrderType.EAT_IN -> Color(0xFF3B82F6)
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Item Image
                AsyncImage(
                    model = BaseUrlProvider.getFullImageUrl(order.items.firstOrNull()?.image),
                    contentDescription = "Order item",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                )

                Text(
                    text = order.createdAt.take(10),
                    color = Color(0xFF9CA3AF),
                    fontSize = 12.sp
                )
            }

            Divider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = Color(0xFFE5E7EB)
            )

            // FOOTER
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${"%.2f".format(order.totalPrice)} TND",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AppDarkText
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reclamation Icon Button
                    IconButton(
                        onClick = { onReclamationClick(order._id) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ReportProblem,
                            contentDescription = "File a complaint",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    val statusColor = when (order.status) {
                        OrderStatus.COMPLETED -> Color(0xFF10B981)
                        OrderStatus.CANCELLED, OrderStatus.REFUSED -> Color(0xFFEF4444)
                        OrderStatus.PENDING -> Color(0xFFF59E0B)
                        OrderStatus.CONFIRMED -> Color(0xFF3B82F6)
                    }

                    Badge(
                        containerColor = statusColor.copy(alpha = 0.15f),
                        contentColor = statusColor
                    ) {
                        Text(
                            text = order.status.name,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}
