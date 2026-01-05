package com.example.damprojectfinal.user.feature_posts.ui.post_management

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.AttachMoney
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.damprojectfinal.core.retro.RetrofitClient
import com.example.damprojectfinal.core.dto.posts.CreatePostDto
import com.example.damprojectfinal.core.dto.posts.FoodType
import com.example.damprojectfinal.core.api.TokenManager
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.net.URLDecoder

enum class AppMediaType(val value: String) {
    IMAGE("image"),
    REEL("reel"),
    CAROUSEL("carousel")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptionAndPublishScreen(
    navController: NavController,
    mediaUriString: String?,
    postsViewModel: PostsViewModel = viewModel()
) {
    // --- State & Setup ---
    val mediaUris = remember(mediaUriString) {
        mediaUriString?.split(",")?.mapNotNull {
            try {
                Uri.parse(URLDecoder.decode(it, "UTF-8"))
            } catch (e: Exception) {
                null
            }
        } ?: emptyList()
    }

    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val currentUserId = remember { tokenManager.getUserId() }
    val currentUserType = remember { tokenManager.getUserType() }
    val coroutineScope = rememberCoroutineScope()

    // Input State
    var captionText by remember { mutableStateOf("") }
    var selectedFoodType by remember { mutableStateOf<String?>(null) }
    var priceText by remember { mutableStateOf("") }
    var preparationTimeText by remember { mutableStateOf("") }
    
    // UI State
    var isPublishing by remember { mutableStateOf(false) }
    var showFoodTypeDropdown by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // --- Main Layout ---
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "New Post",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF1F2937)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF1F2937)
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                publishPost(
                                    context = context,
                                    mediaUris = mediaUris,
                                    caption = captionText,
                                    foodType = selectedFoodType ?: "",
                                    price = priceText.toDoubleOrNull(),
                                    preparationTime = preparationTimeText.toIntOrNull(),
                                    ownerId = currentUserId,
                                    ownerType = currentUserType,
                                    onPublishing = { isPublishing = it },
                                    onSuccess = { showSuccessDialog = true },
                                    onError = { msg ->
                                        errorMessage = msg
                                        showErrorDialog = true
                                    }
                                )
                            }
                        },
                        enabled = !isPublishing && captionText.isNotBlank() && selectedFoodType != null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFC107), // Yellow
                            contentColor = Color(0xFF1F2937), // Dark Gray
                            disabledContainerColor = Color(0xFFE5E7EB),
                            disabledContentColor = Color(0xFF9CA3AF)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        if (isPublishing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Share", fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // 1. Media Preview Section
                if (mediaUris.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .background(Color(0xFFF3F4F6))
                    ) {
                        if (mediaUris.size > 1) {
                            // Multiple Images: Carousel View
                            LazyRow(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(mediaUris) { uri ->
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                        modifier = Modifier
                                            .width(280.dp)
                                            .fillMaxHeight()
                                    ) {
                                        AsyncImage(
                                            model = uri,
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                            }
                        } else {
                            // Single Image/Video
                            AsyncImage(
                                model = mediaUris.first(),
                                contentDescription = "Preview",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop // Crop to fill styled area
                            )
                        }
                    }
                }

                // 2. Caption Input
                OutlinedTextField(
                    value = captionText,
                    onValueChange = { captionText = it },
                    placeholder = { 
                        Text(
                            "Write a caption...", 
                            color = Color(0xFF9CA3AF),
                            fontSize = 16.sp
                        ) 
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent, // No background
                        unfocusedContainerColor = Color.Transparent
                    ),
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 16.sp, 
                        color = Color(0xFF1F2937)
                    ),
                    minLines = 3
                )
                
                Divider(thickness = 0.5.dp, color = Color(0xFFE5E7EB))

                // 3. Details Section
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Details",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF111827),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Food Type Dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedFoodType ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Food Type (Required)") },
                            placeholder = { Text("Select type") },
                            trailingIcon = { 
                                Icon(Icons.Filled.KeyboardArrowDown, "Select", tint = Color(0xFF6B7280)) 
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showFoodTypeDropdown = true },
                            enabled = false, // Disable typing, handle click on overlay box if needed, or just use readOnly
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = Color(0xFF1F2937),
                                disabledBorderColor = Color(0xFFE5E7EB),
                                disabledLabelColor = Color(0xFF6B7280),
                                disabledContainerColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        // Invisible overlay to catch clicks
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { showFoodTypeDropdown = true }
                        )
                        
                        DropdownMenu(
                            expanded = showFoodTypeDropdown,
                            onDismissRequest = { showFoodTypeDropdown = false },
                            modifier = Modifier
                                .background(Color.White)
                                .width(300.dp) // Adjust width as needed
                        ) {
                            FoodType.values().forEach { type ->
                                DropdownMenuItem(
                                    text = { 
                                        Text(
                                            "${type.emoji}  ${type.displayName}",
                                            color = Color(0xFF1F2937)
                                        ) 
                                    },
                                    onClick = { 
                                        selectedFoodType = type.value
                                        showFoodTypeDropdown = false 
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Price & Time Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Price Input
                        StyledInput(
                            value = priceText,
                            onValueChange = { 
                                if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) priceText = it 
                            },
                            label = "Price (TND)",
                            icon = Icons.Filled.AttachMoney,
                            keyboardType = KeyboardType.Decimal,
                            modifier = Modifier.weight(1f)
                        )

                        // Time Input
                        StyledInput(
                            value = preparationTimeText,
                            onValueChange = { 
                                if (it.isEmpty() || it.matches(Regex("^\\d+$"))) preparationTimeText = it 
                            },
                            label = "Time (min)",
                            icon = Icons.Filled.Timer,
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }

            // --- Dialogs ---
            
            // Success Dialog
            if (showSuccessDialog) {
                AlertDialog(
                    onDismissRequest = { /* Prevent dismiss, must click OK */ },
                    icon = {
                        Icon(
                            Icons.Filled.CheckCircle, 
                            contentDescription = null,
                            tint = Color(0xFF10B981), // Green
                            modifier = Modifier.size(48.dp)
                        )
                    },
                    title = {
                        Text(
                            "Success!", 
                            fontWeight = FontWeight.Bold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    },
                    text = {
                        Text(
                            "Your post has been published successfully.",
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showSuccessDialog = false
                                postsViewModel.fetchPosts()
                                // Navigate to home screen
                                navController.navigate(com.example.damprojectfinal.UserRoutes.HOME_SCREEN) {
                                    popUpTo(com.example.damprojectfinal.UserRoutes.HOME_SCREEN) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFC107),
                                contentColor = Color(0xFF1F2937)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Awesome!")
                        }
                    },
                    containerColor = Color.White,
                    tonalElevation = 0.dp,
                    shape = RoundedCornerShape(16.dp)
                )
            }

            // Error Dialog
            if (showErrorDialog) {
                AlertDialog(
                    onDismissRequest = { showErrorDialog = false },
                    icon = {
                        Icon(
                            Icons.Filled.Error,
                            contentDescription = null,
                            tint = Color(0xFFEF4444), // Red
                            modifier = Modifier.size(48.dp)
                        )
                    },
                    title = {
                        Text(
                            "Oops!",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Text(
                            errorMessage,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = { showErrorDialog = false }
                        ) {
                            Text("Try Again", color = Color(0xFFEF4444))
                        }
                    },
                    containerColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                )
            }
            
            // Loading Overlay
            if (isPublishing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable(enabled = false) { }, // Block clicks
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFFFFC107)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Publishing...", fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StyledInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    keyboardType: KeyboardType,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 14.sp) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(20.dp)
            )
        },
        modifier = modifier,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFFFFC107),
            unfocusedBorderColor = Color(0xFFE5E7EB),
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedLabelColor = Color(0xFFFFC107),
            unfocusedLabelColor = Color(0xFF6B7280)
        )
    )
}

@Throws(Exception::class)
private fun getTempFileFromUri(context: Context, uri: Uri): File {
    val contentResolver = context.contentResolver
    val fileName = "upload_${System.currentTimeMillis()}"
    
    val mimeType = contentResolver.getType(uri)
    val fileExtension = when {
        mimeType?.startsWith("video/") == true -> mimeType.split("/").getOrNull(1) ?: "mp4"
        mimeType?.startsWith("image/") == true -> mimeType.split("/").getOrNull(1) ?: "jpg"
        else -> uri.path?.substringAfterLast('.', "") ?: "tmp"
    }
    
    val tempFile = File(context.cacheDir, "$fileName.$fileExtension")

    try {
        contentResolver.openInputStream(uri)?.use { inputStream ->
            tempFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        } ?: throw Exception("Failed to open input stream for URI: $uri.")
        
        if (!tempFile.exists() || tempFile.length() == 0L) {
            throw Exception("Temp file creation failed.")
        }
        return tempFile
    } catch (e: Exception) {
        throw Exception("Failed to process file: ${e.message}")
    }
}

private suspend fun publishPost(
    context: Context,
    mediaUris: List<Uri>,
    caption: String,
    foodType: String,
    price: Double?,
    preparationTime: Int?,
    ownerId: String?,
    ownerType: String?,
    onPublishing: (Boolean) -> Unit,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    if (mediaUris.isEmpty()) { onError("No media selected."); return }
    if (caption.isBlank()) { onError("Where is the love? Add a caption!"); return }
    if (foodType.isBlank()) { onError("Please select a food type."); return }
    if (ownerId == null || ownerType == null) { onError("Session expired. Please login again."); return }

    onPublishing(true)

    try {
        // Upload Files
        val multipartBodyParts = mediaUris.map { uri ->
            val file = getTempFileFromUri(context, uri)
            val requestFile = file.asRequestBody(context.contentResolver.getType(uri)?.toMediaTypeOrNull())
            MultipartBody.Part.createFormData("files", file.name, requestFile)
        }

        val uploadResponse = RetrofitClient.postsApiService.uploadFiles(multipartBodyParts)
        if (uploadResponse.urls.isEmpty()) { throw Exception("Upload returned no URLs.") }

        // Determine Type
        val mediaType = when {
            mediaUris.any { context.contentResolver.getType(it)?.startsWith("video") == true } -> AppMediaType.REEL.value
            mediaUris.size > 1 -> AppMediaType.CAROUSEL.value
            else -> AppMediaType.IMAGE.value
        }

        // Create Post
        val createPostDto = CreatePostDto(
            caption = caption,
            mediaUrls = uploadResponse.urls,
            mediaType = mediaType,
            foodType = foodType,
            price = price,
            preparationTime = preparationTime
        )

        val createdPost = RetrofitClient.postsApiService.createPost(createPostDto = createPostDto)

        // Wait for Reel thumbnail if needed
        if (mediaType == AppMediaType.REEL.value && createdPost.thumbnailUrl == null) {
            kotlinx.coroutines.delay(2000)
            try {
                RetrofitClient.postsApiService.getPostById(createdPost._id)
            } catch (_: Exception) { }
        }

        onSuccess()
    } catch (e: Exception) {
        onError(e.message ?: "Something went wrong.")
    } finally {
        onPublishing(false)
        // Cleanup
        mediaUris.forEach { 
             try { File(context.cacheDir, it.pathSegments.lastOrNull() ?: "temp").delete() } catch (_: Exception) {}
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CaptionAndPublishScreenPreview() {
    MaterialTheme {
        CaptionAndPublishScreen(
            navController = rememberNavController(),
            mediaUriString = null
        )
    }
}
