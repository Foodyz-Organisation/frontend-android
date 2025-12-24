package com.example.damprojectfinal.user.feature_posts.ui.post_management

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.damprojectfinal.UserRoutes
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(navController: NavController) {
    var selectedMediaUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    // We can tack if the selected media is video to change preview behavior if needed
    var isVideo by remember { mutableStateOf(false) }

    // --- Launchers ---
    val multipleImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10),
        onResult = { uris: List<Uri> ->
            if (uris.isNotEmpty()) {
                selectedMediaUris = uris
                isVideo = false
            }
        }
    )

    val singleImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            uri?.let { 
                selectedMediaUris = listOf(it)
                isVideo = false
            }
        }
    )

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let { 
                selectedMediaUris = listOf(it)
                isVideo = true
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "New Post",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937), // Dark Gray
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Filled.ArrowBack, 
                            contentDescription = "Back", 
                            tint = Color(0xFF1F2937)
                        )
                    }
                },
                actions = {
                    if (selectedMediaUris.isNotEmpty()) {
                        Button(
                            onClick = {
                                val encodedUris = selectedMediaUris.joinToString(",") { uri ->
                                    URLEncoder.encode(uri.toString(), StandardCharsets.UTF_8.toString())
                                }
                                navController.navigate("${UserRoutes.CAPTION_PUBLISH_SCREEN}/$encodedUris")
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFC107), // Yellow
                                contentColor = Color(0xFF1F2937) // Dark Gray for contrast
                            ),
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("Next", fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF9FAFB) // Light Gray Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (selectedMediaUris.isEmpty()) {
                // --- Empty State: Selection Options ---
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Create Content",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1F2937)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Share your food journey with the world",
                        fontSize = 16.sp,
                        color = Color(0xFF6B7280),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(48.dp))

                    // Option 1: Carousel
                    SelectionCard(
                        title = "Carousel",
                        subtitle = "Share multiple photos",
                        icon = Icons.Filled.Collections,
                        onClick = {
                             multipleImagePickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // Option 2: Single Photo
                    SelectionCard(
                        title = "Photo",
                        subtitle = "Share a single moment",
                        icon = Icons.Filled.Image,
                        onClick = {
                            singleImagePickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Option 3: Reel/Video
                    SelectionCard(
                        title = "Reel",
                        subtitle = "Share a short video",
                        icon = Icons.Filled.Videocam,
                        onClick = {
                            videoPickerLauncher.launch("video/*")
                        }
                    )
                }
            } else {
                // --- Media Preview State ---
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black), // Dark background for media preview
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        // Display first item as preview
                        AsyncImage(
                            model = selectedMediaUris.first(),
                            contentDescription = "Selected Media",
                            modifier = Modifier.fillMaxWidth(),
                            contentScale = ContentScale.Fit
                        )
                        
                        // Carousel Indicator
                        if (selectedMediaUris.size > 1) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(16.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "+${selectedMediaUris.size - 1} more",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Bottom Bar with "Change Selection"
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1F2937))
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                             OutlinedButton(
                                onClick = { selectedMediaUris = emptyList() },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color.White
                                ),
                                border = BorderStroke(1.dp, Color.Gray)
                            ) {
                                Icon(
                                    Icons.Filled.Close, 
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Change Selection")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SelectionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Container
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFC107).copy(alpha = 0.2f)), // Light Yellow
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFFFFC107), // Yellow
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1F2937)
                )
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = Color(0xFF9CA3AF)
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Arrow indicator
            Icon(
                imageVector = Icons.Filled.ArrowBack, // Using back arrow rotated or right chevron if available
                contentDescription = null,
                tint = Color(0xFFD1D5DB),
                modifier = Modifier
                    .size(20.dp)
                    .rotate(180f) // Point right
            )
        }
    }
}

// Helper for rotation
fun Modifier.rotate(degrees: Float) = this.then(
    Modifier.graphicsLayer(rotationZ = degrees)
)

@Preview(showBackground = true)
@Composable
fun CreatePostScreenPreview() {
    MaterialTheme {
        CreatePostScreen(navController = rememberNavController())
    }
}
