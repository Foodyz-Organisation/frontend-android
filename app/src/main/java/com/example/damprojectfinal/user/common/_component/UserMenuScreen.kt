package com.example.damprojectfinal.user.common._component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@Composable
fun UserMenuScreen(
    navController: NavHostController,
    onLogout: () -> Unit,
    onBackClick: () -> Unit,
    loyaltyPoints: Int? = null
) {
    UserMenuScreenContent(
        navController = navController,
        onLogout = onLogout,
        onBackClick = onBackClick,
        loyaltyPoints = loyaltyPoints,
        paddingValues = PaddingValues(0.dp)
    )
}

@Composable
fun UserMenuScreenContent(
    navController: NavHostController,
    onLogout: () -> Unit,
    onBackClick: () -> Unit,
    loyaltyPoints: Int? = null,
    paddingValues: PaddingValues = PaddingValues(0.dp),
    showTopBar: Boolean = true
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Loyalty Points Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF6200EA).copy(alpha = 0.1f)
                ),
                onClick = {
                    navController.navigate("loyalty_points_route")
                    onBackClick()
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Points de fidélité",
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(32.dp)
                        )
                        Column {
                            Text(
                                text = "${loyaltyPoints ?: 0} Points",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color(0xFF6200EA)
                            )
                            Text(
                                text = "Points de Fidélité",
                                fontSize = 12.sp,
                                color = Color(0xFF6B7280)
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Voir détails",
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Menu Options
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Mes Réclamations
                MenuOptionCard(
                    icon = Icons.Default.List,
                    title = "Mes Réclamations",
                    onClick = {
                        navController.navigate("list_reclamation_route")
                    }
                )

                // Événements
                MenuOptionCard(
                    icon = Icons.Default.Event,
                    title = "Événements",
                    onClick = {
                        navController.navigate("event_list")
                    }
                )

                // Liste des Deals
                MenuOptionCard(
                    icon = Icons.Default.ShoppingCart,
                    title = "Liste des Deals",
                    onClick = {
                        navController.navigate("deals")
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)

                // Signup as Professional
                MenuOptionCard(
                    icon = Icons.Default.Fastfood,
                    title = "Signup as Professional",
                    onClick = {
                        navController.navigate("pro_signup_route")
                        onBackClick()
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Logout Button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable(onClick = onLogout),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFEBEE)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Logout",
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Logout",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD32F2F)
                    )
                }
            }
        }
    }


    @Composable
    fun MenuOptionCard(
        icon: ImageVector,
        title: String,
        onClick: () -> Unit
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = Color(0xFFFFC107).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1F2937)
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Navigate",
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }


