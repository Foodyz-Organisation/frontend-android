package com.example.damprojectfinal.professional.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.* // Import all filled icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import kotlinx.coroutines.launch

// Import the CustomProTopBarWithIcons component from its new package
import com.example.damprojectfinal.professional.common._component.CustomProTopBarWithIcons

// --- Mock Data Structures (Adjusted for static content and simplified) ---
data class Order(
    val id: String,
    val customerName: String,
    val summary: String,
    val total: String,
    val timeReceived: String,
    val address: String,
    val type: OrderType
)

enum class OrderType { PICKUP, DINE_IN, DELIVERY }

// -----------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenPro(
    professionalId: String,
    navController: NavHostController,
    onLogout: () -> Unit // This onLogout is not used by a button in the top bar as per screenshot, but kept for signature.
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope() // Keep coroutine scope for Toast messages

    // --- STATIC ORDERS ---
    val staticOrders = remember {
        listOf(
            Order(
                id = "ahmed1",
                customerName = "Ahmed Ben Ali",
                summary = "Couscous Royal (x1)",
                total = "25.50 TND",
                timeReceived = "Received 5 minutes ago",
                address = "Avenue Habib Bourguiba, Tunis",
                type = OrderType.DELIVERY
            ),
            Order(
                id = "leila2",
                customerName = "Leila Jebali",
                summary = "Vegetarian Mezze Plat...",
                total = "28.00 TND",
                timeReceived = "Received 25 minutes ago",
                address = "Rue de Marseille, La Marsa",
                type = OrderType.DELIVERY // Assuming this is also delivery based on the screenshot
            )
        )
    }

    // State for filter, default to DELIVERY as per screenshot
    var selectedFilter by remember { mutableStateOf<OrderType?>(OrderType.DELIVERY) }

    // Filter static orders by selected type
    val filteredOrders = remember(staticOrders, selectedFilter) {
        staticOrders.filter { order ->
            selectedFilter == null || order.type == selectedFilter
        }
    }

    Scaffold(
        topBar = {
            CustomProTopBarWithIcons(
                professionalId = professionalId,
                navController = navController,
                currentRoute = "home" // Explicitly set "home" as the current route to highlight the home icon
            )
            // Note: The onLogout parameter is removed from CustomProTopBarWithIcons as per the screenshot,
            // but the lambda in HomeScreenPro remains part of its signature for potential future use or consistency.
        },
        bottomBar = {
            OrderFilterBottomBar(
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F7F7))
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Text(
                    text = "Pending Orders",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2A37),
                    modifier = Modifier.padding(top = 8.dp)
                )
                // Adjust text to match screenshot: "2 delivery orders waiting for confirmation"
                // Assuming filteredOrders contain only delivery orders if selectedFilter is Delivery.
                Text(
                    text = "${filteredOrders.size} delivery orders waiting for confirmation",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Order List
            if (filteredOrders.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No orders matching the selected filter.",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                }
            } else {
                items(filteredOrders, key = { it.id }) { order ->
                    OrderCard(
                        order = order,
                        onAccept = {
                            scope.launch {
                                Toast.makeText(context, "Accepted order ${order.id}", Toast.LENGTH_SHORT).show()
                                // In a real app, you'd update your data source here
                            }
                        },
                        onRefuse = {
                            scope.launch {
                                Toast.makeText(context, "Refused order ${order.id}", Toast.LENGTH_SHORT).show()
                                // In a real app, you'd update your data source here
                            }
                        }
                    )
                }
            }
        }
    }
}

// --- Component: Order Card ---
@Composable
fun OrderCard(order: Order, onAccept: () -> Unit, onRefuse: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Row 1: Customer Name and Order Total
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                // Customer Avatar (Placeholder)
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Customer",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF0F0F0)),
                    tint = Color(0xFF6B7280)
                )

                Spacer(Modifier.width(8.dp))

                // Name and Summary
                Column(modifier = Modifier.weight(1f)) {
                    Text(order.customerName, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Text(order.summary, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text(order.timeReceived, style = MaterialTheme.typography.bodySmall, color = Color(0xFFD6A42E))
                }

                // Order Total
                Text(order.total, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF218041))
            }

            Spacer(Modifier.height(12.dp))

            // Row 2: Address
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF5F3FF))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = Color(0xFF6D28D9),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(order.address, style = MaterialTheme.typography.bodySmall, color = Color(0xFF6D28D9))
            }

            Spacer(Modifier.height(16.dp))

            // Row 3: Action Buttons (Accept/Refuse)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Accept", modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Accept", fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = onRefuse,
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Refuse", modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Refuse", fontWeight = FontWeight.SemiBold)
                }

                IconButton(
                    onClick = { /* Flag */ },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF0F0F0))
                        .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.Flag, contentDescription = "Flag", tint = Color.Gray)
                }
            }
        }
    }
}


// --- Component: Order Filter Bottom Bar (Adjusted) ---
@Composable
fun OrderFilterBottomBar(
    selectedFilter: OrderType?,
    onFilterSelected: (OrderType?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- Pick-up ---
        BottomBarChip(
            icon = Icons.Filled.ShoppingBag,
            label = "Pick-up",
            isSelected = selectedFilter == OrderType.PICKUP,
            onClick = { onFilterSelected(OrderType.PICKUP) }
        )

        // --- Dine-in ---
        BottomBarChip(
            icon = Icons.Filled.TableBar, // Assuming TableBar for dine-in
            label = "Dine-in",
            isSelected = selectedFilter == OrderType.DINE_IN,
            onClick = { onFilterSelected(OrderType.DINE_IN) }
        )

        // --- Delivery --- (Selected by default as per screenshot)
        BottomBarChip(
            icon = Icons.Filled.LocalShipping, // Assuming LocalShipping for delivery
            label = "Delivery",
            isSelected = selectedFilter == OrderType.DELIVERY,
            onClick = { onFilterSelected(OrderType.DELIVERY) }
        )
    }
}

@Composable
fun BottomBarChip(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) Color(0xFFFFC107) else Color(0xFFF0F0F0)
    val contentColor = if (isSelected) Color.Black else Color.Gray

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = label, tint = contentColor, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(4.dp))
            Text(label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = contentColor)
        }
    }
}
