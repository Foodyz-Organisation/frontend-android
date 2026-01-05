# Post Publishing Logic Documentation

This document describes the complete logic flow for publishing a post in the Foodyz app.

## Overview

The post publishing flow consists of three main screens:
1. **HomeUserScreen** - Entry point with "Add Post" button
2. **CreatePostScreen** - Media selection screen
3. **CaptionAndPublishScreen** - Caption, details, and publishing screen

---

## 1. Entry Point: HomeUserScreen

**File:** `app/src/main/java/com/example/damprojectfinal/user/common/HomeUserScreen.kt`

### Navigation Trigger

```kotlin
// Line 210: SearchBar component
SearchBar(
    onSearchClick = { isSearchActive = true },
    onAddClick = { navController.navigate(UserRoutes.CREATE_POST) }
)
```

**Logic:**
- User clicks the yellow "+" button in the search bar
- Navigates to `UserRoutes.CREATE_POST` route

---

## 2. Media Selection: CreatePostScreen

**File:** `app/src/main/java/com/example/damprojectfinal/user/feature_posts/ui/post_management/CreatePostScreen.kt`

### State Management

```kotlin
var selectedMediaUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
var isVideo by remember { mutableStateOf(false) }
```

### Media Picker Launchers

```kotlin
// Multiple images (Carousel) - up to 10 images
val multipleImagePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10),
    onResult = { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            selectedMediaUris = uris
            isVideo = false
        }
    }
)

// Single image
val singleImagePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia(),
    onResult = { uri: Uri? ->
        uri?.let { 
            selectedMediaUris = listOf(it)
            isVideo = false
        }
    }
)

// Video/Reel
val videoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent(),
    onResult = { uri: Uri? ->
        uri?.let { 
            selectedMediaUris = listOf(it)
            isVideo = true
        }
    }
)
```

### Navigation to Caption Screen

```kotlin
// Lines 101-108: "Next" button action
Button(
    onClick = {
        val encodedUris = selectedMediaUris.joinToString(",") { uri ->
            URLEncoder.encode(uri.toString(), StandardCharsets.UTF_8.toString())
        }
        navController.navigate("${UserRoutes.CAPTION_PUBLISH_SCREEN}/$encodedUris")
    }
)
```

**Logic:**
- URIs are URL-encoded and joined with commas
- Passed as route parameter to caption screen
- Format: `caption_publish_screen/uri1,uri2,uri3`

---

## 3. Caption & Publishing: CaptionAndPublishScreen

**File:** `app/src/main/java/com/example/damprojectfinal/user/feature_posts/ui/post_management/CaptionAndPublishScreen.kt`

### State Management

```kotlin
// Parse media URIs from route parameter
val mediaUris = remember(mediaUriString) {
    mediaUriString?.split(",")?.mapNotNull {
        try {
            Uri.parse(URLDecoder.decode(it, "UTF-8"))
        } catch (e: Exception) {
            null
        }
    } ?: emptyList()
}

// User session info
val tokenManager = remember { TokenManager(context) }
val currentUserId = remember { tokenManager.getUserId() }
val currentUserType = remember { tokenManager.getUserType() }

// Input fields
var captionText by remember { mutableStateOf("") }
var selectedFoodType by remember { mutableStateOf<String?>(null) }
var priceText by remember { mutableStateOf("") }
var preparationTimeText by remember { mutableStateOf("") }

// UI state
var isPublishing by remember { mutableStateOf(false) }
var showSuccessDialog by remember { mutableStateOf(false) }
var showErrorDialog by remember { mutableStateOf(false) }
var errorMessage by remember { mutableStateOf("") }
```

### Publish Button Logic

```kotlin
// Lines 118-137: "Share" button
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
    enabled = !isPublishing && captionText.isNotBlank() && selectedFoodType != null
)
```

**Validation:**
- Button is disabled if:
  - Currently publishing (`isPublishing == true`)
  - Caption is blank
  - Food type is not selected

---

## 4. Core Publishing Function

**File:** `app/src/main/java/com/example/damprojectfinal/user/feature_posts/ui/post_management/CaptionAndPublishScreen.kt`

### Function Signature

```kotlin
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
)
```

### Step 1: Input Validation

```kotlin
if (mediaUris.isEmpty()) { 
    onError("No media selected."); 
    return 
}
if (caption.isBlank()) { 
    onError("Where is the love? Add a caption!"); 
    return 
}
if (foodType.isBlank()) { 
    onError("Please select a food type."); 
    return 
}
if (ownerId == null || ownerType == null) { 
    onError("Session expired. Please login again."); 
    return 
}
```

### Step 2: File Upload Preparation

```kotlin
// Convert URIs to temporary files
val multipartBodyParts = mediaUris.map { uri ->
    val file = getTempFileFromUri(context, uri)
    val requestFile = file.asRequestBody(
        context.contentResolver.getType(uri)?.toMediaTypeOrNull()
    )
    MultipartBody.Part.createFormData("files", file.name, requestFile)
}
```

**Helper Function: `getTempFileFromUri`**

```kotlin
@Throws(Exception::class)
private fun getTempFileFromUri(context: Context, uri: Uri): File {
    val contentResolver = context.contentResolver
    val fileName = "upload_${System.currentTimeMillis()}"
    
    val mimeType = contentResolver.getType(uri)
    val fileExtension = when {
        mimeType?.startsWith("video/") == true -> 
            mimeType.split("/").getOrNull(1) ?: "mp4"
        mimeType?.startsWith("image/") == true -> 
            mimeType.split("/").getOrNull(1) ?: "jpg"
        else -> uri.path?.substringAfterLast('.', "") ?: "tmp"
    }
    
    val tempFile = File(context.cacheDir, "$fileName.$fileExtension")
    
    // Copy URI content to temp file
    contentResolver.openInputStream(uri)?.use { inputStream ->
        tempFile.outputStream().use { outputStream ->
            inputStream.copyTo(outputStream)
        }
    } ?: throw Exception("Failed to open input stream for URI: $uri.")
    
    if (!tempFile.exists() || tempFile.length() == 0L) {
        throw Exception("Temp file creation failed.")
    }
    
    return tempFile
}
```

### Step 3: Upload Files to Server

```kotlin
val uploadResponse = RetrofitClient.postsApiService.uploadFiles(multipartBodyParts)
if (uploadResponse.urls.isEmpty()) { 
    throw Exception("Upload returned no URLs.") 
}
```

**API Call:**
- Uses `RetrofitClient.postsApiService.uploadFiles()`
- Returns `UploadResponse` with list of media URLs
- These URLs are stored on the server and used in the post

### Step 4: Determine Media Type

```kotlin
val mediaType = when {
    mediaUris.any { 
        context.contentResolver.getType(it)?.startsWith("video") == true 
    } -> AppMediaType.REEL.value  // "reel"
    mediaUris.size > 1 -> AppMediaType.CAROUSEL.value  // "carousel"
    else -> AppMediaType.IMAGE.value  // "image"
}
```

**Logic:**
- If any URI is a video → `"reel"`
- If multiple URIs → `"carousel"`
- Otherwise → `"image"`

### Step 5: Create Post DTO

```kotlin
val createPostDto = CreatePostDto(
    caption = caption,
    mediaUrls = uploadResponse.urls,  // URLs from upload step
    mediaType = mediaType,  // "image", "reel", or "carousel"
    foodType = foodType,  // Required food type
    price = price,  // Optional
    preparationTime = preparationTime  // Optional
)
```

**CreatePostDto Structure:**

```kotlin
data class CreatePostDto(
    val caption: String,
    val mediaUrls: List<String>,
    val mediaType: String,  // "image", "reel", or "carousel"
    val foodType: String,  // Required
    val price: Double? = null,  // Optional
    val preparationTime: Int? = null  // Optional
)
```

### Step 6: Create Post via API

```kotlin
val createdPost = RetrofitClient.postsApiService.createPost(
    createPostDto = createPostDto
)
```

**API Call:**
- Sends POST request to create post
- Returns `PostResponse` with created post details including `_id`

### Step 7: Handle Reel Thumbnail (if applicable)

```kotlin
if (mediaType == AppMediaType.REEL.value && createdPost.thumbnailUrl == null) {
    kotlinx.coroutines.delay(2000)  // Wait 2 seconds for thumbnail generation
    try {
        RetrofitClient.postsApiService.getPostById(createdPost._id)
    } catch (_: Exception) { }
}
```

**Logic:**
- For reels, thumbnail generation happens asynchronously on server
- Waits 2 seconds then fetches post again to get thumbnail URL
- This is optional and doesn't block success

### Step 8: Success/Error Handling

```kotlin
try {
    // ... all publishing steps ...
    onSuccess()  // Show success dialog
} catch (e: Exception) {
    onError(e.message ?: "Something went wrong.")
} finally {
    onPublishing(false)  // Hide loading overlay
    
    // Cleanup temporary files
    mediaUris.forEach { 
        try { 
            File(context.cacheDir, it.pathSegments.lastOrNull() ?: "temp").delete() 
        } catch (_: Exception) {}
    }
}
```

---

## 5. Success Flow

### Success Dialog

```kotlin
if (showSuccessDialog) {
    AlertDialog(
        onDismissRequest = { /* Prevent dismiss */ },
        icon = { /* CheckCircle icon */ },
        title = { Text("Success!") },
        text = { Text("Your post has been published successfully.") },
        confirmButton = {
            Button(
                onClick = {
                    showSuccessDialog = false
                    postsViewModel.fetchPosts()  // Refresh posts list
                    navController.navigate(UserRoutes.HOME_SCREEN) {
                        popUpTo(UserRoutes.HOME_SCREEN) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            ) {
                Text("Awesome!")
            }
        }
    )
}
```

**Actions:**
1. Shows success dialog
2. Refreshes posts list via `postsViewModel.fetchPosts()`
3. Navigates to home screen
4. Clears back stack to prevent going back to create screen

---

## 6. Error Handling

### Error Dialog

```kotlin
if (showErrorDialog) {
    AlertDialog(
        onDismissRequest = { showErrorDialog = false },
        icon = { /* Error icon */ },
        title = { Text("Oops!") },
        text = { Text(errorMessage) },
        confirmButton = {
            TextButton(
                onClick = { showErrorDialog = false }
            ) {
                Text("Try Again")
            }
        }
    )
}
```

**Common Error Messages:**
- `"No media selected."` - No URIs provided
- `"Where is the love? Add a caption!"` - Caption is blank
- `"Please select a food type."` - Food type not selected
- `"Session expired. Please login again."` - User session invalid
- `"Upload returned no URLs."` - File upload failed
- `"Something went wrong."` - Generic error

---

## 7. Loading State

### Loading Overlay

```kotlin
if (isPublishing) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(enabled = false) { },  // Block clicks
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = Color(0xFFFFC107))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Publishing...", fontWeight = FontWeight.Medium)
            }
        }
    }
}
```

**Features:**
- Blocks all user interaction during publishing
- Shows spinner and "Publishing..." text
- Semi-transparent black overlay

---

## 8. Data Flow Diagram

```
┌─────────────────┐
│  HomeUserScreen │
│   (Add Button)  │
└────────┬────────┘
         │ navigate(CREATE_POST)
         ▼
┌─────────────────┐
│ CreatePostScreen│
│ (Media Selection)│
└────────┬────────┘
         │ navigate(CAPTION_PUBLISH_SCREEN/uris)
         ▼
┌──────────────────────┐
│CaptionAndPublishScreen│
│  (Details & Publish)  │
└────────┬─────────────┘
         │ publishPost()
         ▼
┌─────────────────┐
│  File Upload    │
│  (Multipart)    │
└────────┬────────┘
         │ uploadResponse.urls
         ▼
┌─────────────────┐
│  Create Post    │
│  (API Call)     │
└────────┬────────┘
         │ createdPost
         ▼
┌─────────────────┐
│  Success/Error  │
│  (Dialog)       │
└────────┬────────┘
         │ navigate(HOME_SCREEN)
         ▼
┌─────────────────┐
│  HomeUserScreen │
│  (Posts Refreshed)│
└─────────────────┘
```

---

## 9. Key Components

### Media Type Enum

```kotlin
enum class AppMediaType(val value: String) {
    IMAGE("image"),
    REEL("reel"),
    CAROUSEL("carousel")
}
```

### API Services Used

1. **`RetrofitClient.postsApiService.uploadFiles()`**
   - Uploads media files
   - Returns `UploadResponse` with URLs

2. **`RetrofitClient.postsApiService.createPost()`**
   - Creates post with metadata
   - Returns `PostResponse`

3. **`RetrofitClient.postsApiService.getPostById()`**
   - Fetches post by ID (for reel thumbnail)

### Dependencies

- **TokenManager**: Manages user session and authentication
- **RetrofitClient**: Handles API communication
- **PostsViewModel**: Manages posts state and refresh

---

## 10. Summary

**Complete Flow:**
1. User clicks "Add Post" button → Navigate to `CreatePostScreen`
2. User selects media (image/carousel/reel) → URIs stored
3. User clicks "Next" → Navigate to `CaptionAndPublishScreen` with URIs
4. User enters caption, food type, price, time → Form validation
5. User clicks "Share" → `publishPost()` function executes:
   - Validates inputs
   - Converts URIs to temp files
   - Uploads files via multipart
   - Determines media type
   - Creates post DTO
   - Calls create post API
   - Handles reel thumbnail (if needed)
6. Success → Show dialog → Refresh posts → Navigate home
7. Error → Show error dialog → User can retry

**Key Features:**
- ✅ Multi-media support (image, carousel, reel)
- ✅ Comprehensive validation
- ✅ File upload with multipart
- ✅ Automatic media type detection
- ✅ Error handling and cleanup
- ✅ User feedback (loading, success, error)
- ✅ Temporary file cleanup

---

## Files Involved

1. `HomeUserScreen.kt` - Entry point
2. `CreatePostScreen.kt` - Media selection
3. `CaptionAndPublishScreen.kt` - Caption, details, and publishing logic
4. `CreatePostDto.kt` - Data transfer object
5. `PostsApiService.kt` - API interface
6. `RetrofitClient.kt` - API client configuration

