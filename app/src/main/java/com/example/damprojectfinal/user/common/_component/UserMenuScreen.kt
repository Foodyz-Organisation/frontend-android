package com.example.damprojectfinal.user.common._component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.damprojectfinal.UserRoutes
import com.example.damprojectfinal.core.api.BaseUrlProvider

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
    showTopBar: Boolean = true,
    userId: String? = null,
    profilePictureUrl: String? = null,
    userName: String? = null,
    userEmail: String? = null
) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(paddingValues)
            .verticalScroll(
                state = scrollState,
                enabled = true
            )
    ) {
        // Top bar when drawer is full screen
        if (showTopBar) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .windowInsetsPadding(WindowInsets.statusBars)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Menu",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Color(0xFF1F2937)
                    )
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = Color(0xFFF3F4F6),
                                shape = RoundedCornerShape(20.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF1F2937),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        } else {
            // Close button at the top (only show if no top bar)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = Color(0xFFF3F4F6),
                            shape = RoundedCornerShape(20.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF1F2937),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(if (showTopBar) 8.dp else 8.dp))

        // Profile Card - Clickable to navigate to profile
        if (userId != null) {
            ProfileCard(
                profilePictureUrl = profilePictureUrl,
                userName = userName,
                userEmail = userEmail,
                onClick = {
                    navController.navigate("${UserRoutes.PROFILE_VIEW.substringBefore("/")}/$userId") {
                        launchSingleTop = true
                    }
                    onBackClick()
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

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

                // Orders History
                MenuOptionCard(
                    icon = Icons.Filled.ReceiptLong,
                    title = "Orders History",
                    onClick = {
                        navController.navigate(UserRoutes.ORDERS_ROUTE)
                        onBackClick()
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
            
            // Add bottom padding to ensure last item is fully visible when scrolled
            Spacer(modifier = Modifier.height(24.dp))
        }
    }


    @Composable
    fun ProfileCard(
        profilePictureUrl: String?,
        userName: String?,
        userEmail: String?,
        onClick: () -> Unit
    ) {
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp
        val isSmallScreen = screenWidth < 360
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF8F9FA)
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
                // Profile Picture
                Box(
                    modifier = Modifier
                        .size(if (isSmallScreen) 56.dp else 64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE5E7EB)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!profilePictureUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = BaseUrlProvider.getFullImageUrl(profilePictureUrl),
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier.size(if (isSmallScreen) 28.dp else 32.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // User Info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = userName ?: "User",
                        fontSize = if (isSmallScreen) 18.sp else 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
                    if (!userEmail.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = userEmail,
                            fontSize = if (isSmallScreen) 14.sp else 15.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                }
                
                // Chevron Icon
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "View Profile",
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(24.dp)
                )
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


