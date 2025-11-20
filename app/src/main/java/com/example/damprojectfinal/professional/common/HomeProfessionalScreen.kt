package com.example.damprojectfinal.professional.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenPro(
    professionalId: String,
    navController: NavHostController,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },

                // 🔥 Added Logout Button Here
                actions = {
                    IconButton(
                        onClick = {
                            navController.navigate("login") {
                                popUpTo(0)   // Clears navigation history
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Logout",
                            tint = Color.Red
                        )
                    }
                }
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

            // --- 1. Top Metrics Cards ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    MetricCard(
                        title = "Total Orders",
                        value = "127",
                        change = "+12% from last week",
                        icon = Icons.Default.Inventory,
                        backgroundColor = Color(0xFFFFFBEA),
                        valueColor = Color(0xFF333333),
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Revenue",
                        value = "$3,847",
                        change = "+8% from last week",
                        icon = Icons.Default.TrendingUp,
                        backgroundColor = Color(0xFFE8FFE8),
                        valueColor = Color(0xFF333333),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // --- 3. Navigation Cards ---
            item {
                ActionCard(
                    icon = Icons.Default.Inventory,
                    title = "Manage Orders",
                    subtitle = "View and process customer orders",
                    badge = "3",
                    iconBackground = Color(0xFFE8EAF6),
                    iconColor = Color(0xFF3F51B5),
                    onClick = {
                        navController.navigate("menu_management/$professionalId")
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                ActionCard(
                    icon = Icons.Default.MenuBook,
                    title = "Menu Management",
                    subtitle = "Edit your menu and items",
                    indicator = true,
                    iconBackground = Color(0xFFF3E5F5),
                    iconColor = Color(0xFF9C27B0),
                    onClick = {
                        navController.navigate("menu_management/$professionalId")
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                ActionCard(
                    icon = Icons.Default.BarChart,
                    title = "Analytics",
                    subtitle = "Coming soon...",
                    iconBackground = Color(0xFFE0F7FA),
                    iconColor = Color(0xFF00BCD4),
                    onClick = {},
                    isEnabled = false
                )

                Spacer(modifier = Modifier.height(12.dp))

                ActionCard(
                    icon = Icons.Default.Settings,
                    title = "Settings",
                    subtitle = "Coming soon...",
                    iconBackground = Color(0xFFFBE9E7),
                    iconColor = Color(0xFFFF5722),
                    onClick = {},
                    isEnabled = false
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Recent Activity",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text("No recent activity.")
            }
        }
    }
}

// --- Component: Top Metric Card ---
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
        modifier = modifier.height(120.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(title, style = MaterialTheme.typography.bodyMedium)
            }

            Text(value, style = MaterialTheme.typography.headlineMedium.copy(color = valueColor))
            Text(change, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}

// --- Component: Quick Action Navigation Card ---
@Composable
fun ActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    iconBackground: Color,
    iconColor: Color,
    modifier: Modifier = Modifier,
    badge: String? = null,
    indicator: Boolean = false,
    isEnabled: Boolean = true
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick, enabled = isEnabled),
        elevation = CardDefaults.cardElevation(1.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }

            if (badge != null) {
                Badge(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = Color.White
                ) {
                    Text(badge)
                }
            } else if (indicator) {
                Icon(Icons.Default.ArrowForward, contentDescription = null)
            }
        }
    }
}
