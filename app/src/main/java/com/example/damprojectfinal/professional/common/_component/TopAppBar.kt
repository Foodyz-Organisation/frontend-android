package com.example.damprojectfinal.professional.common._component

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import coil.compose.AsyncImage
import com.example.damprojectfinal.core.api.BaseUrlProvider
import com.example.damprojectfinal.ProRoutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomProTopBarWithIcons(
    professionalId: String,
    navController: NavHostController,
    profilePictureUrl: String? = null,
    onLogout: () -> Unit,
    onMenuClick: () -> Unit
) {
    Column(modifier = Modifier.background(Color.White).statusBarsPadding()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Icon/Avatar with real image when available
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF0F0F0))
                    .clickable {
                        // Navigate directly to the professional profile *settings* screen
                        navController.navigate(
                            ProRoutes.PROFESSIONAL_PROFILE_SETTINGS.replace(
                                "{professionalId}",
                                professionalId
                            )
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                // Fallback icon
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = Color(0xFF6B7280),
                    modifier = Modifier.size(20.dp)
                )

                // Overlay profile image if URL is available
                if (!profilePictureUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = BaseUrlProvider.getFullImageUrl(profilePictureUrl),
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        placeholder = rememberVectorPainter(Icons.Default.Person),
                        error = rememberVectorPainter(Icons.Default.Person)
                    )
                }
            }

            Text(
                text = "Foodyz Pro",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.weight(1f).padding(start = 8.dp)
            )

            // Map/Location Tracking Button
            IconButton(
                onClick = {
                    navController.navigate("all_users_tracking/$professionalId")
                }
            ) {
                Icon(
                    Icons.Filled.LocationOn,
                    contentDescription = "View All Users Tracking",
                    tint = Color(0xFFFFC107)
                )
            }

            // Menu Icon (trigger drawer)
            IconButton(
                onClick = {
                    Log.d("TopAppBar", "Menu IconButton clicked")
                    onMenuClick()
                }
            ) {
                Icon(Icons.Filled.Menu, contentDescription = "Menu")
            }
        }

    }
}