package com.example.damprojectfinal.user.feautre_order.ui

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.airbnb.lottie.compose.*
import com.example.damprojectfinal.R
import com.example.damprojectfinal.core.dto.order.OrderResponse
import com.example.damprojectfinal.core.dto.order.OrderStatus
import com.example.damprojectfinal.user.feautre_order.viewmodel.OrderViewModel

// ----------- COLORS -----------
val BackgroundLight = Color(0xFFF9FAFB)
val AppDarkText = Color(0xFF1F2937)

// ===============================================================
// ORDER HISTORY SCREEN
// ===============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(
    navController: NavController,
    orderViewModel: OrderViewModel,
    userId: String,
    onOrderClick: (String) -> Unit
) {
    val ordersState by orderViewModel.orders.collectAsState()
    val isLoading by orderViewModel.loading.collectAsState()

    LaunchedEffect(userId) {
        orderViewModel.loadOrdersByUser(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Order History",
                        fontWeight = FontWeight.ExtraBold,
                        color = AppDarkText
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = AppDarkText
                        )
                    }
                }
            )
        },
        containerColor = BackgroundLight
    ) { paddingValues ->

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (ordersState.isNullOrEmpty()) {
            EmptyOrdersView(paddingValues)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(ordersState!!, key = { it._id }) { order ->
                    OrderItemCard(order, onOrderClick)
                }
            }
        }
    }
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
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "You haven’t placed any orders yet.",
                color = Color.Gray,
                fontSize = 14.sp,
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {

        Column(modifier = Modifier.padding(16.dp)) {

            // HEADER
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Order #${order._id.takeLast(6)}", // Show last 6 chars of ID
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    Text(
                        text = "Professional: ${order.professionalId}", // Ideally fetch name
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                }

                Text(
                    text = order.createdAt.take(10), // Show date part
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            Divider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = Color(0xFFF3F4F6)
            )

            // FOOTER
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$${"%.2f".format(order.totalPrice)}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                val statusColor = when (order.status) {
                    OrderStatus.COMPLETED -> Color(0xFF10B981)
                    OrderStatus.CANCELLED, OrderStatus.REFUSED -> Color(0xFFEF4444)
                    OrderStatus.PENDING -> Color(0xFFF59E0B)
                    OrderStatus.CONFIRMED -> Color(0xFF3B82F6)
                }

                Badge(
                    containerColor = statusColor.copy(alpha = 0.1f),
                    contentColor = statusColor
                ) {
                    Text(
                        text = order.status.name,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
