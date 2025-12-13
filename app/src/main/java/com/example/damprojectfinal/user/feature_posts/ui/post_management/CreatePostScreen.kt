package com.example.damprojectfinal.user.feature_posts.ui.post_management

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage // For displaying selected media

import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.PhotoLibrary

import com.example.damprojectfinal.UserRoutes
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(navController: NavController) {
    var selectedMediaUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    val context = LocalContext.current

    // --- NEW: State to control dialog visibility ---
    var showMediaTypePickerDialog by remember { mutableStateOf(false) }

    // --- NEW: Launcher for picking multiple images (carousel) ---
    val multipleImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10),
        onResult = { uris: List<Uri> ->
            selectedMediaUris = uris // Update the state with the selected URIs
        }
    )

    // --- NEW: Launcher for picking single image ---
    val singleImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            uri?.let { selectedMediaUris = listOf(it) }
        }
    )

    // --- NEW: Launcher specifically for picking videos (single video only) ---
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let { selectedMediaUris = listOf(it) }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (selectedMediaUris.isEmpty()) "Créer une nouvelle publication" else "Rogné",
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    if (selectedMediaUris.isNotEmpty()) {
                        TextButton(
                            onClick = {
                                // Encode multiple URIs as comma-separated string
                                val encodedUris = selectedMediaUris.joinToString(",") { uri ->
                                    URLEncoder.encode(uri.toString(), StandardCharsets.UTF_8.toString())
                                }
                                navController.navigate("${UserRoutes.CAPTION_PUBLISH_SCREEN}/$encodedUris")
                            }
                        ) {
                            Text("Suivant", color = Color(0xFF6A5ACD), fontWeight = FontWeight.SemiBold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1E1E1E),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF1E1E1E)
    ) { paddingValues ->
        if (selectedMediaUris.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFF1E1E1E)),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.Collections,
                    contentDescription = "Select media",
                    tint = Color.Gray,
                    modifier = Modifier.size(100.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Faites glisser les photos et les vidéos ici",
                    color = Color.LightGray,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { showMediaTypePickerDialog = true }, // <--- MODIFIED: Show dialog when button clicked
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A5ACD)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(50.dp)
                ) {
                    Text(
                        text = "Sélectionner sur votre phone",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        } else {
            // Display selected media (single image/video or carousel)
            if (selectedMediaUris.size == 1) {
                // Single media display
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(Color.Black)
                ) {
                    AsyncImage(
                        model = selectedMediaUris.first(),
                        contentDescription = "Selected Media",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )

                    // Overlay for zoom controls (bottom-left)
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ZoomIn,
                            contentDescription = "Zoom",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Overlay for multi-select/video icon (bottom-right)
                    IconButton(
                        onClick = { showMediaTypePickerDialog = true },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.6f))
                            .size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PhotoLibrary,
                            contentDescription = "Select more media",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            } else {
                // Carousel display - show first image with indicator
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(Color.Black)
                ) {
                    AsyncImage(
                        model = selectedMediaUris.first(),
                        contentDescription = "Selected Media",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )

                    // Carousel indicator (top-center)
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(16.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Carousel: ${selectedMediaUris.size} photos",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Overlay for multi-select/video icon (bottom-right)
                    IconButton(
                        onClick = { showMediaTypePickerDialog = true },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.6f))
                            .size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PhotoLibrary,
                            contentDescription = "Select more media",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // --- NEW: Media Type Picker Dialog ---
        if (showMediaTypePickerDialog) {
            AlertDialog(
                onDismissRequest = { showMediaTypePickerDialog = false },
                title = { Text("Sélectionner le type de média", color = Color.White) },
                text = { 
                    Column {
                        Text("Choisissez une option:", color = Color.LightGray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("• Plusieurs photos = Carousel", color = Color.LightGray, fontSize = 12.sp)
                        Text("• Une photo = Image", color = Color.LightGray, fontSize = 12.sp)
                        Text("• Vidéo = Reel", color = Color.LightGray, fontSize = 12.sp)
                    }
                },
                confirmButton = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Multiple images (carousel)
                        TextButton(
                            onClick = {
                                showMediaTypePickerDialog = false
                                multipleImagePickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Plusieurs photos (Carousel)", color = Color(0xFF6A5ACD))
                        }
                        // Single image
                        TextButton(
                            onClick = {
                                showMediaTypePickerDialog = false
                                singleImagePickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Une photo", color = Color(0xFF6A5ACD))
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showMediaTypePickerDialog = false
                        videoPickerLauncher.launch("video/*") // Launch video picker
                    }) {
                        Text("Vidéo", color = Color(0xFF6A5ACD))
                    }
                },
                containerColor = Color(0xFF1E1E1E), // Dark background for dialog
                titleContentColor = Color.White,
                textContentColor = Color.White
            )
        }
        // --- END NEW ---
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CreatePostScreenPreview() {
    val dummyNavController = rememberNavController()
    MaterialTheme {
        CreatePostScreen(navController = dummyNavController)
    }
}
