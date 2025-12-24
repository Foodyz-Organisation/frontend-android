package com.example.damprojectfinal.user.feature_posts.ui.post_management

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.damprojectfinal.core.api.BaseUrlProvider
import com.example.damprojectfinal.core.dto.posts.PostResponse
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPostScreen(
    navController: NavController,
    postId: String,
    initialCaption: String, 
    postsViewModel: PostsViewModel = viewModel()
) {
    var caption by remember { mutableStateOf(initialCaption) }
    var post by remember { mutableStateOf<PostResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Feedback States
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    // Fetch Post Details
    LaunchedEffect(postId) {
        isLoading = true
        try {
            post = postsViewModel.getPostById(postId)
            // Update caption from fetched post if available, though initialCaption is passed
            post?.let { caption = it.caption }
        } catch (e: Exception) {
            errorMessage = "Failed to load post details."
            showErrorDialog = true
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Edit Post", 
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
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
                    // Update Button
                    Button(
                        onClick = {
                            if (caption.isNotBlank()) {
                                postsViewModel.updatePostCaption(postId, caption)
                                showSuccessDialog = true
                            }
                        },
                        enabled = caption.isNotBlank() && !isLoading, // Disable if blank or loading
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFC107), // Yellow
                            contentColor = Color(0xFF1F2937) // Dark Text
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFFFC107))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Media Preview
                item {
                    val currentPost = post
                    if (currentPost != null) {
                         val rawUrl = if (currentPost.mediaType == "reel" && currentPost.thumbnailUrl != null) {
                            currentPost.thumbnailUrl
                        } else {
                            currentPost.mediaUrls.firstOrNull()
                        }
                        val imageUrlToLoad = BaseUrlProvider.getFullImageUrl(rawUrl)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFF3F4F6)),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = imageUrlToLoad,
                                contentDescription = "Post Media",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                // Inputs
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Caption
                        OutlinedTextField(
                            value = caption,
                            onValueChange = { caption = it },
                            label = { Text("Caption") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 10,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFFC107),
                                focusedLabelColor = Color(0xFFFFC107)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Read-Only Fields
                        val currentPost = post
                        if (currentPost != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Price
                                OutlinedTextField(
                                    value = "${currentPost.price} Dt",
                                    onValueChange = {},
                                    label = { Text("Price") },
                                    modifier = Modifier.weight(1f),
                                    readOnly = true,
                                    enabled = false, 
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledBorderColor = Color(0xFFE5E7EB),
                                        disabledLabelColor = Color(0xFF9CA3AF),
                                        disabledTextColor = Color(0xFF4B5563)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                // Prep Time
                                OutlinedTextField(
                                    value = "${currentPost.preparationTime} min",
                                    onValueChange = {},
                                    label = { Text("Time") },
                                    modifier = Modifier.weight(1f),
                                    readOnly = true,
                                    enabled = false,
                                    trailingIcon = { Icon(Icons.Outlined.Timer, null) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledBorderColor = Color(0xFFE5E7EB),
                                        disabledLabelColor = Color(0xFF9CA3AF),
                                        disabledTextColor = Color(0xFF4B5563)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                            
                            // Food Type
                            OutlinedTextField(
                                value = currentPost.foodType ?: "",
                                onValueChange = {},
                                label = { Text("Cuisine") },
                                modifier = Modifier.fillMaxWidth(),
                                readOnly = true,
                                enabled = false,
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledBorderColor = Color(0xFFE5E7EB),
                                    disabledLabelColor = Color(0xFF9CA3AF),
                                    disabledTextColor = Color(0xFF4B5563)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            
                            Text(
                                text = "Only caption can be edited at this time.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF9CA3AF),
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Success Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { /* Prevent dismiss, must click OK */ },
            icon = {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF10B981), // Green for success
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = "Post Updated!",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
            },
            text = {
                Text(
                    text = "Your post has been successfully updated.",
                    color = Color(0xFF4B5563),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFC107),
                        contentColor = Color(0xFF1F2937)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Awesome!", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
    
     // Error Dialog
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Error,
                    contentDescription = null,
                    tint = Color(0xFFEF4444), 
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = "Update Failed",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
            },
            text = {
                Text(
                    text = errorMessage ?: "Something went wrong. Please try again.",
                    color = Color(0xFF4B5563),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = { showErrorDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEF4444),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Try Again", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

