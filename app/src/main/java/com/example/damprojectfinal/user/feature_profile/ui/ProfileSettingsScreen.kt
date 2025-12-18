package com.example.damprojectfinal.user.feature_profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.damprojectfinal.core.api.BaseUrlProvider
import com.example.damprojectfinal.core.api.TokenManager
import com.example.damprojectfinal.core.dto.user.UserResponse
import com.example.damprojectfinal.feature_auth.viewmodels.LogoutViewModel
import com.example.damprojectfinal.core.utils.LogoutViewModelFactory
import com.example.damprojectfinal.core.api.AuthApiService
import com.example.damprojectfinal.UserRoutes
import com.example.damprojectfinal.AuthRoutes

// Color constants
private val SettingsCard = Color(0xFFFFFFFF)
private val SettingsBackground = Color(0xFFFAFAFA)
private val SettingsAccent = Color(0xFF1F2937)
private val SettingsSecondary = Color(0xFF6B7280)
private val IconYellow = Color(0xFFF59E0B)
private val LogoutRed = Color(0xFFEF4444)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScreen(
    navController: NavController,
    profileViewModel: com.example.damprojectfinal.user.feature_profile.viewmodel.ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val userId = remember { tokenManager.getUserId() ?: "" }
    
    // Fetch user profile
    LaunchedEffect(Unit) {
        profileViewModel.fetchUserProfile()
    }
    
    val userState by profileViewModel.userState.collectAsState()
    val user = userState
    
    // Logout ViewModel
    val logoutViewModel: LogoutViewModel = viewModel(
        factory = LogoutViewModelFactory(
            authApiService = AuthApiService(),
            tokenManager = tokenManager
        )
    )
    
    val logoutSuccess by logoutViewModel.logoutSuccess.collectAsState()
    
    // Handle logout
    LaunchedEffect(logoutSuccess) {
        if (logoutSuccess) {
            navController.navigate(AuthRoutes.LOGIN) {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
    }
    
    // Notification state (local for now)
    var notificationsEnabled by remember { mutableStateOf(true) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "PROFILE SETTING",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = SettingsAccent
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = SettingsAccent
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SettingsCard,
                    titleContentColor = SettingsAccent
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(SettingsBackground)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // General Section
                SettingsSection(
                    title = "General",
                    items = listOf(
                        SettingsItem(
                            icon = Icons.Outlined.Person,
                            title = "Edit Profile",
                            description = "Change profile picture, number, E-mail",
                            onClick = {
                                if (userId.isNotEmpty()) {
                                    navController.navigate(UserRoutes.PROFILE_UPDATE.replace("{userId}", userId))
                                }
                            }
                        ),
                        SettingsItem(
                            icon = Icons.Outlined.Lock,
                            title = "Change Password",
                            description = "Update and strengthen account security",
                            onClick = {
                                if (userId.isNotEmpty()) {
                                    navController.navigate(UserRoutes.CHANGE_PASSWORD.replace("{userId}", userId))
                                }
                            }
                        ),
                        SettingsItem(
                            icon = Icons.Outlined.Shield,
                            title = "Terms of Use",
                            description = "Protect your account now",
                            onClick = {
                                // TODO: Navigate to Terms of Use
                            }
                        ),
                        SettingsItem(
                            icon = Icons.Outlined.CreditCard,
                            title = "Add Card",
                            description = "Securely add payment method",
                            onClick = {
                                // TODO: Navigate to Add Card
                            }
                        )
                    )
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Preferences Section
                PreferencesSection(
                    notificationsEnabled = notificationsEnabled,
                    onNotificationsToggle = { notificationsEnabled = it },
                    onFaqClick = {
                        // TODO: Navigate to FAQ
                    },
                    onLogoutClick = {
                        logoutViewModel.logout()
                    }
                )
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    items: List<SettingsItem>
) {
    Column {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = SettingsSecondary,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SettingsCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column {
                items.forEachIndexed { index, item ->
                    SettingsItemRow(item = item)
                    if (index < items.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = Color(0xFFE5E7EB),
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsItemRow(item: SettingsItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = item.onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = IconYellow,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = SettingsAccent
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.description,
                fontSize = 13.sp,
                color = SettingsSecondary
            )
        }
        
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = SettingsSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun PreferencesSection(
    notificationsEnabled: Boolean,
    onNotificationsToggle: (Boolean) -> Unit,
    onFaqClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Column {
            Text(
                text = "Preferences",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = SettingsSecondary,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SettingsCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column {
                // Notification Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = null,
                        tint = IconYellow,
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Notification",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = SettingsAccent
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Customize your notification preferences",
                            fontSize = 13.sp,
                            color = SettingsSecondary
                        )
                    }
                    
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = onNotificationsToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = IconYellow
                        )
                    )
                }
                
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = Color(0xFFE5E7EB),
                    thickness = 0.5.dp
                )
                
                // FAQ
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onFaqClick)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.HelpOutline,
                        contentDescription = null,
                        tint = IconYellow,
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "FAQ",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = SettingsAccent
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Frequently asked questions",
                            fontSize = 13.sp,
                            color = SettingsSecondary
                        )
                    }
                    
                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = SettingsSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = Color(0xFFE5E7EB),
                    thickness = 0.5.dp
                )
                
                // Logout
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onLogoutClick)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ExitToApp,
                        contentDescription = null,
                        tint = LogoutRed,
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Log Out",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = LogoutRed
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Securely log out of Account",
                            fontSize = 13.sp,
                            color = SettingsSecondary
                        )
                    }
                    
                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = SettingsSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

private data class SettingsItem(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val onClick: () -> Unit
)

