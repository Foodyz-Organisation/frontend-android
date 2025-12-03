package com.example.damprojectfinal.user.feautre_order.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.navigation.compose.rememberNavController
import com.airbnb.lottie.compose.*
import com.example.damprojectfinal.R
import com.example.damprojectfinal.UserRoutes
import com.example.damprojectfinal.core.dto.order.OrderResponse
import com.example.damprojectfinal.core.dto.order.OrderStatus
import com.example.damprojectfinal.user.common._component.TopAppBar
import com.example.damprojectfinal.user.feautre_order.viewmodel.OrderViewModel

// ----------- COLORS -----------
val BackgroundLight = Color(0xFFF9FAFB)
val AppDarkText = Color(0xFF1F2937)
val LightBackground = Color.White // Consistent component color

// ===============================================================
// ORDER HISTORY SCREEN WITH TopAppBar
// ===============================================================

@Composable
fun OrderHistoryScreen(
    navController: NavController,
    orderViewModel: OrderViewModel,
    userId: String,
    onOrderClick: (String) -> Unit,
    onLogout: () -> Unit
) {
    val ordersState by orderViewModel.orders.collectAsState()
    val isLoading by orderViewModel.loading.collectAsState()
    var isSearchActive by remember { mutableStateOf(false) }

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
                onLogoutClick = onLogout
            )
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
                        items(ordersState!!, key = { it._id }) { order ->
                            OrderItemCard(order, onOrderClick)
                        }
                    }
                }
            }

            // --- Optional: Dynamic Search Overlay ---
            if (isSearchActive) {
                // Import and use your DynamicSearchOverlay if needed
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
fun OrderItemCard(order: OrderResponse, onClick: (String) -> Unit) {
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
                Column {
                    Text(
                        text = "Order #${order._id.takeLast(6)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )

                    Text(
                        text = "Professional: ${order.professionalId}",
                        color = Color(0xFF6B7280),
                        fontSize = 13.sp
                    )
                }

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
                    text = "$${"%.2f".format(order.totalPrice)}",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AppDarkText
                )

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
