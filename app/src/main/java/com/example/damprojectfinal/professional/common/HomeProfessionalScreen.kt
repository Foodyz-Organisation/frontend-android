package com.example.damprojectfinal.professional.common

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.damprojectfinal.core.api.TokenManager
import com.example.damprojectfinal.core.dto.order.OrderResponse
import com.example.damprojectfinal.core.dto.order.OrderStatus
import com.example.damprojectfinal.core.dto.order.UpdateOrderStatusRequest
import com.example.damprojectfinal.core.repository.OrderRepository
import com.example.damprojectfinal.core.retro.RetrofitClient
import com.example.damprojectfinal.professional.common._component.CustomProTopBarWithIcons
import com.example.damprojectfinal.user.feautre_order.viewmodel.OrderViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.damprojectfinal.core.dto.order.OrderType as BackendOrderType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

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

private fun OrderResponse.toUiOrder(): Order {
    val orderType = when (this.orderType) {
        BackendOrderType.TAKEAWAY -> OrderType.PICKUP
        BackendOrderType.EAT_IN -> OrderType.DINE_IN
        BackendOrderType.DELIVERY -> OrderType.DELIVERY
    }

    // Calculate time ago
    val timeAgo = try {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val createdDate = dateFormat.parse(this.createdAt)
        val currentDate = Date()
        val diffInMillis = currentDate.time - (createdDate?.time ?: 0)
        val minutes = diffInMillis / (1000 * 60)
        when {
            minutes < 1 -> "Received just now"
            minutes < 60 -> "Received $minutes minutes ago"
            else -> "Received ${minutes / 60} hours ago"
        }
    } catch (e: Exception) {
        "Received recently"
    }

    // Create summary from items
    val summary = this.items.take(2).joinToString(", ") { "${it.name} (x${it.quantity})" } +
            if (this.items.size > 2) "," else ""

    return Order(
        id = this._id,
        customerName = this.getUserName(),
        summary = summary,
        total = String.format("%.2f TND", this.totalPrice),
        timeReceived = timeAgo,
        address = "No address",
        type = orderType
    )
}

// Helper function to get status color
private fun getStatusColor(status: OrderStatus): Color {
    return when (status) {
        OrderStatus.PENDING -> Color(0xFFFFA500)     // Orange
        OrderStatus.CONFIRMED -> Color(0xFF4CAF50)   // Green
        OrderStatus.COMPLETED -> Color(0xFF2196F3)   // Blue
        OrderStatus.CANCELLED -> Color(0xFF9E9E9E)   // Gray
        OrderStatus.REFUSED -> Color(0xFFF44336)     // Red
    }
}

// Helper function to get status display name
private fun getStatusDisplayName(status: OrderStatus): String {
    return when (status) {
        OrderStatus.PENDING -> "Pending"
        OrderStatus.CONFIRMED -> "Confirmed"
        OrderStatus.COMPLETED -> "Completed"
        OrderStatus.CANCELLED -> "Cancelled"
        OrderStatus.REFUSED -> "Refused"
    }
}

// Helper function to get valid status transitions (matches backend logic)
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

        OrderStatus.COMPLETED -> emptyList() // Final state - no transitions
        OrderStatus.CANCELLED -> emptyList() // Final state - no transitions
        OrderStatus.REFUSED -> emptyList() // Final state - no transitions
    }
}
// -----------------------------------------------------------


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenPro(
    professionalId: String,
    navController: NavHostController,
    onLogout: () -> Unit
) {
    // Setup ViewModel
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val orderApi = remember { RetrofitClient.orderApi }
    val orderRepository = remember { OrderRepository(orderApi, tokenManager) }
    val orderViewModel: OrderViewModel = viewModel(
        factory = OrderViewModel.Factory(orderRepository)
    )

    // Coroutine scope for async operations
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    // State for filter
    var selectedFilter by remember { mutableStateOf<OrderType?>(null) }

    // Load orders on start
    LaunchedEffect(professionalId) {
        orderViewModel.loadOrdersByProfessional(professionalId)
    }

    // Observe ViewModel states
    val ordersFromBackend by orderViewModel.orders.collectAsState()
    val isLoading by orderViewModel.loading.collectAsState()
    val errorMessage by orderViewModel.error.collectAsState()

    // Convert to UI models
    val allOrders = remember(ordersFromBackend) {
        ordersFromBackend?.map { it.toUiOrder() } ?: emptyList()
    }

    // Filter by selected type
    val filteredOrders = remember(allOrders, selectedFilter) {
        allOrders.filter { order ->
            selectedFilter == null || order.type == selectedFilter
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(24.dp))
                Text(
                    text = "Professional Menu",
                    modifier = Modifier.padding(start = 24.dp, bottom = 12.dp),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                HorizontalDivider(modifier = Modifier.padding(bottom = 12.dp))

                NavigationDrawerItem(
                    label = { Text("Deals Management") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("pro_deals")
                    },
                    icon = { Icon(Icons.Default.LocalOffer, null) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    label = { Text("Reclamations") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("restaurant_reclamations")
                    },
                    icon = { Icon(Icons.Default.Report, null) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                // Menu Management (routed to existing add_meal as requested wrapper for menu buttons)
                // Assuming "get the routes of the existing buttons" refers to Menu Management button.
                // Button 1: "Manage Orders" -> menu_management
                // Button 2: "Menu Management" -> add_meal
                NavigationDrawerItem(
                    label = { Text("Menu Management") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("add_meal/$professionalId")
                    },
                    icon = { Icon(Icons.Default.MenuBook, null) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    label = { Text("Event Management") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        // Static onClick as requested
                    },
                    icon = { Icon(Icons.Default.Event, null) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(Modifier.weight(1f))
                HorizontalDivider()

                NavigationDrawerItem(
                    label = { Text("Logout", color = Color.Red, fontWeight = FontWeight.Bold) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onLogout()
                    },
                    icon = { Icon(Icons.Default.ExitToApp, null, tint = Color.Red) },
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                CustomProTopBarWithIcons(
                    professionalId = professionalId,
                    navController = navController,
                    onLogout = onLogout,
                    onMenuClick = { 
                        // Open the drawer (original functionality)
                        scope.launch { drawerState.open() }
                    }
                )
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
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
            ) {
                // --- 1. Top Metrics Cards & Quick Actions Header REMOVED ---
                when {
                    isLoading -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color(0xFFFFC107))
                            }
                        }
                    }

                    errorMessage != null -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Error loading orders",
                                        color = Color.Red,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = errorMessage ?: "Unknown error",
                                        color = Color.Gray,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }

                    else -> {
                        // Header
                        item {
                            Text(
                                text = "Pending Orders",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1F2A37),
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            Text(
                                text = "${filteredOrders.size} orders waiting for confirmation",
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
                                // Get original OrderResponse to access current status
                                val originalOrder = ordersFromBackend?.find { it._id == order.id }
                                val currentStatus = originalOrder?.status ?: OrderStatus.PENDING

                                OrderCardWithStatusDropdown(
                                    order = order,
                                    currentStatus = currentStatus,
                                    onStatusChange = { newStatus ->
                                        // Update order status using coroutine
                                        scope.launch {
                                            orderViewModel.updateOrderStatus(
                                                order.id,
                                                UpdateOrderStatusRequest(newStatus)
                                            )
                                            // Wait briefly for update to complete
                                            delay(300)
                                            // Reload orders
                                            orderViewModel.loadOrdersByProfessional(professionalId)
                                            Toast.makeText(
                                                context,
                                                "Order #${order.id.takeLast(6)} updated to ${getStatusDisplayName(newStatus)}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
                // --- 3. Navigation Cards & Recent Activity REMOVED ---

            }
        }
    }
}

// ---------------------------------------------------
// --- Component: Custom Top Bar with 5 Icons ---
// ---------------------------------------------------


@Composable
fun NavTopIcon(icon: ImageVector, description: String, selected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (selected) Color(0xFFFFC107) else Color(0xFFF0F0F0)
    val iconColor = if (selected) Color.Black else Color(0xFF64748B)

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = description, tint = iconColor, modifier = Modifier.size(24.dp))
    }
}

// ---------------------------------------------------
// --- Component: Order Filter Bottom Bar ---
// ---------------------------------------------------

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
        // --- ALL Orders (Implied by the request) ---
        BottomBarChip(
            icon = Icons.Filled.Dashboard, // Using Dashboard icon for 'All'
            label = "All",
            isSelected = selectedFilter == null,
            onClick = { onFilterSelected(null) }
        )

        // --- Pick-up ---
        BottomBarChip(
            icon = Icons.Filled.ShoppingBag,
            label = "Pick-up",
            isSelected = selectedFilter == OrderType.PICKUP,
            onClick = { onFilterSelected(OrderType.PICKUP) }
        )

        // --- Dine-in ---
        BottomBarChip(
            icon = Icons.Filled.TableBar,
            label = "Dine-in",
            isSelected = selectedFilter == OrderType.DINE_IN,
            onClick = { onFilterSelected(OrderType.DINE_IN) }
        )

        // --- Delivery ---
        BottomBarChip(
            icon = Icons.Filled.LocalShipping,
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

    // Using the button style from your screenshot's "Pick-up" button
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


// --- OrderCard with Status Dropdown ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderCardWithStatusDropdown(
    order: Order,
    currentStatus: OrderStatus,
    onStatusChange: (OrderStatus) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        elevation = CardDefaults.cardElevation(1.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Row 1: Customer Name and Order Total
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
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

                Column(modifier = Modifier.weight(1f)) {
                    Text(order.customerName, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Text(order.summary, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text(order.timeReceived, style = MaterialTheme.typography.bodySmall, color = Color(0xFFD6A42E))
                }

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

            // Row 3: Status Dropdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Order Status:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1F2A37)
                )

                // Check if status can be changed
                val validStatuses = getValidStatusTransitions(currentStatus)
                val canChangeStatus = validStatuses.isNotEmpty()

                // Status Dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { if (canChangeStatus) expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = getStatusDisplayName(currentStatus),
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
                            focusedContainerColor = getStatusColor(currentStatus).copy(alpha = 0.1f),
                            unfocusedContainerColor = getStatusColor(currentStatus).copy(alpha = 0.1f),
                            disabledContainerColor = getStatusColor(currentStatus).copy(alpha = 0.1f),
                            unfocusedBorderColor = getStatusColor(currentStatus),
                            focusedBorderColor = getStatusColor(currentStatus),
                            disabledBorderColor = getStatusColor(currentStatus).copy(alpha = 0.5f),
                            disabledTextColor = getStatusColor(currentStatus)
                        ),
                        modifier = Modifier
                            .menuAnchor()
                            .width(160.dp)
                            .height(56.dp),
                        textStyle = LocalTextStyle.current.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = getStatusColor(currentStatus)
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        // Only show valid status transitions
                        val validStatuses = getValidStatusTransitions(currentStatus)
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
                                            fontWeight = if (status == currentStatus) FontWeight.Bold else FontWeight.Normal
                                        )
                                        // Show "(Current)" for current status
                                        if (status == currentStatus) {
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                text = "(Current)",
                                                fontSize = 12.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    if (status != currentStatus) {
                                        onStatusChange(status)
                                    }
                                    expanded = false
                                },
                                enabled = status != currentStatus // Disable current status
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------
// --- Component: Metric Card ---
// ---------------------------------------------------

@Composable
fun MetricCard(
    title: String,
    value: String,
    change: String,
    icon: ImageVector,
    backgroundColor: Color,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(backgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = valueColor, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(change, style = MaterialTheme.typography.labelSmall, color = Color(0xFF4CAF50))
        }
    }
}

// ---------------------------------------------------
// --- Component: Action Card ---
// ---------------------------------------------------

@Composable
fun ActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    badge: String? = null,
    indicator: Boolean = false,
    iconBackground: Color,
    iconColor: Color,
    isEnabled: Boolean = true,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick, enabled = isEnabled),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isEnabled) Color.White else Color(0xFFF5F5F5)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isEnabled) iconBackground else Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isEnabled) iconColor else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isEnabled) Color.Black else Color.Gray
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            if (badge != null) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        badge,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else if (indicator && isEnabled) {
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = Color.Gray
                )
            }
        }
    }
}

// --- OrderCard (Legacy) - Fixed for compilation safety ---
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
            }
        }
    }
}
