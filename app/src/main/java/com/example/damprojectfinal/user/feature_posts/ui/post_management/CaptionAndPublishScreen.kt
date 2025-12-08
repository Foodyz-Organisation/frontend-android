package com.example.damprojectfinal.user.feature_posts.ui.post_management

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.example.damprojectfinal.core.retro.RetrofitClient
import com.example.damprojectfinal.core.dto.posts.CreatePostDto
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import android.widget.Toast
import com.example.damprojectfinal.core.api.TokenManager // <-- Ensure this import is here
import androidx.lifecycle.viewmodel.compose.viewModel


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
    val mediaUri = mediaUriString?.let { Uri.parse(it) }
    val context = LocalContext.current

    val tokenManager = remember { TokenManager(context) }
    val currentUserId = remember { tokenManager.getUserId() }
    val currentUserType = remember { tokenManager.getUserType() } // Correctly calls getUserType()

    var captionText by remember { mutableStateOf("") }
    var isPublishing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nouvelle publication", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                // --- MODIFIED CALL TO publishPost: Pass ownerId and ownerType ---
                                publishPost(
                                    context = context,
                                    mediaUri = mediaUri,
                                    caption = captionText,
                                    ownerId = currentUserId,   // <-- Pass currentUserId
                                    ownerType = currentUserType, // <-- Pass currentUserType
                                    onPublishing = { isPublishing = it },
                                    onSuccess = {
                                        Toast.makeText(context, "Post published successfully!", Toast.LENGTH_SHORT).show()
                                        postsViewModel.fetchPosts()
                                        navController.popBackStack()
                                    },
                                    onError = { errorMessage ->
                                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                                        Log.e("PublishPost", errorMessage)
                                    }
                                )
                            }
                        },
                        enabled = !isPublishing && captionText.isNotBlank(),
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF6A5ACD))
                    ) {
                        if (isPublishing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Publier", fontWeight = FontWeight.SemiBold)
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFF1E1E1E)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (mediaUri != null) {
                Log.d("CaptionAndPublishScreen", "Attempting to display media: $mediaUri")
                AsyncImage(
                    model = mediaUri,
                    contentDescription = "Selected Media",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .background(Color.Black),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .background(Color.Gray),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No Media Selected", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = captionText,
                onValueChange = { captionText = it },
                label = { Text("Légende...", color = Color.LightGray) },
                placeholder = { Text("Écrivez une légende...", color = Color.Gray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                textStyle = LocalTextStyle.current.copy(color = Color.White),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6A5ACD),
                    unfocusedBorderColor = Color.Gray,
                    cursorColor = Color(0xFF6A5ACD),
                    focusedLabelColor = Color(0xFF6A5ACD),
                    unfocusedLabelColor = Color.LightGray,
                    focusedContainerColor = Color(0xFF1E1E1E),
                    unfocusedContainerColor = Color(0xFF1E1E1E)
                ),
                singleLine = false,
                minLines = 3
            )

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Throws(Exception::class)
private fun getTempFileFromUri(context: Context, uri: Uri): File {
    val contentResolver = context.contentResolver
    val fileName = "upload_${System.currentTimeMillis()}"
    
    // Get MIME type to determine file extension
    val mimeType = contentResolver.getType(uri)
    Log.d("getTempFileFromUri", "URI: $uri, MIME type: $mimeType")
    
    val fileExtension = when {
        mimeType?.startsWith("video/") == true -> {
            mimeType.split("/").getOrNull(1) ?: "mp4"
        }
        mimeType?.startsWith("image/") == true -> {
            mimeType.split("/").getOrNull(1) ?: "jpg"
        }
        else -> {
            // Try to get extension from URI path
            uri.path?.substringAfterLast('.', "") ?: "tmp"
        }
    }
    
    val tempFile = File(context.cacheDir, "$fileName.$fileExtension")
    Log.d("getTempFileFromUri", "Creating temp file: ${tempFile.absolutePath}")

    try {
        contentResolver.openInputStream(uri)?.use { inputStream ->
            tempFile.outputStream().use { outputStream ->
                val bytesCopied = inputStream.copyTo(outputStream)
                Log.d("getTempFileFromUri", "Copied $bytesCopied bytes to temp file")
            }
        } ?: throw Exception("Failed to open input stream for URI: $uri. Check if you have proper permissions.")
        
        if (!tempFile.exists() || tempFile.length() == 0L) {
            throw Exception("Temp file was not created or is empty: ${tempFile.absolutePath}")
        }
        
        Log.d("getTempFileFromUri", "Successfully created temp file: ${tempFile.absolutePath}, size: ${tempFile.length()} bytes")
        return tempFile
    } catch (e: SecurityException) {
        Log.e("getTempFileFromUri", "SecurityException: ${e.message}", e)
        throw Exception("Permission denied. Please grant media access permissions. ${e.message}")
    } catch (e: Exception) {
        Log.e("getTempFileFromUri", "Error reading URI: ${e.message}", e)
        throw Exception("Failed to read file from URI: ${e.message}")
    }
}

private suspend fun publishPost(
    context: Context,
    mediaUri: Uri?,
    caption: String,
    ownerId: String?, // <-- NEW PARAMETER DEFINITION
    ownerType: String?, // <-- NEW PARAMETER DEFINITION
    onPublishing: (Boolean) -> Unit,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    if (mediaUri == null) {
        onError("No media selected to publish.")
        return
    }

    if (caption.isBlank()) {
        onError("Caption cannot be empty.")
        return
    }

    // --- Validate ownerId and ownerType before proceeding ---
    if (ownerId == null || ownerType == null) {
        onError("User information (ID or type) is missing. Please log in again.")
        return
    }

    onPublishing(true)

    try {
        // Step 1: Upload Media
        val file = getTempFileFromUri(context, mediaUri)
        val requestFile = file.asRequestBody(context.contentResolver.getType(mediaUri)?.toMediaTypeOrNull())
        val multipartBodyPart = MultipartBody.Part.createFormData("files", file.name, requestFile)

        val uploadResponse = RetrofitClient.postsApiService.uploadFiles(listOf(multipartBodyPart))
        val uploadedMediaUrls = uploadResponse.urls

        if (uploadedMediaUrls.isEmpty()) {
            onError("Media upload failed: No URLs returned.")
            return
        }

        // Determine media type (simple heuristic for now)
        val mediaType = if (context.contentResolver.getType(mediaUri)?.startsWith("video") == true) {
            AppMediaType.REEL.value
        } else {
            AppMediaType.IMAGE.value
        }

        // Step 2: Create Post
        val createPostDto = CreatePostDto(
            caption = caption,
            mediaUrls = uploadedMediaUrls,
            mediaType = mediaType
        )

        // --- MODIFIED API CALL: Pass ownerType ---
        // AuthInterceptor handles x-user-id automatically.
        // AuthInterceptor will also handle x-owner-type if we removed it from PostsApiService.createPost()
        // and let AuthInterceptor add it. If you kept it in PostsApiService, then you'd also pass ownerId.
        // For consistency with AuthInterceptor handling x-user-id, let AuthInterceptor handle x-owner-type as well.
        val createdPost = RetrofitClient.postsApiService.createPost(
            // ownerType is not passed directly here because AuthInterceptor will add x-owner-type.
            // If you still have @Header("x-owner-type") in PostsApiService.createPost,
            // then you MUST uncomment the line below:
            // ownerType = ownerType,
            createPostDto = createPostDto
        )
        // --- END MODIFIED API CALL ---

        // If this is a reel post and thumbnail is not yet generated, wait a bit and fetch again
        // The backend generates thumbnails asynchronously, so we need to wait for it
        if (mediaType == AppMediaType.REEL.value && createdPost.thumbnailUrl == null) {
            // Wait 2 seconds for thumbnail generation, then fetch the post again
            kotlinx.coroutines.delay(2000)
            try {
                val updatedPost = RetrofitClient.postsApiService.getPostById(createdPost._id)
                // The updated post should now have the thumbnail
                // The thumbnail will be included when posts are fetched for profiles
            } catch (e: Exception) {
                // If fetching fails, continue anyway - thumbnail might be generated later
                Log.d("PublishPost", "Could not fetch updated post with thumbnail: ${e.message}")
            }
        }

        onSuccess()
    } catch (e: Exception) {
        val errorMessage = e.message ?: "An unknown error occurred during publishing."
        Log.e("PublishPost", "Error publishing post: $errorMessage", e)
        onError("Failed to publish post: ${e.localizedMessage ?: errorMessage}")
    } finally {
        onPublishing(false)
        mediaUri?.let { uri ->
            try {
                // Attempt to delete temporary file
                val tempFile = File(context.cacheDir, uri.pathSegments.lastOrNull() ?: "temp_file")
                if(tempFile.exists()) tempFile.delete()
            } catch (cleanupError: Exception) {
                Log.e("PublishPost", "Error cleaning up temp file: ${cleanupError.message}")
            }
        }
    }
}



@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CaptionAndPublishScreenPreview() {
    val dummyNavController = rememberNavController()
    MaterialTheme {
        val context = LocalContext.current
        val packageName = context.packageName
        val drawableUriString = "android.resource://$packageName/drawable/foodone"

        CaptionAndPublishScreen(
            navController = dummyNavController,
            mediaUriString = drawableUriString
        )
    }
}
