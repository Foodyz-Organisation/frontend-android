package com.example.damprojectfinal.professional.feature_profile.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.damprojectfinal.core.api.TokenManager
import com.example.damprojectfinal.core.api.posts.PostsApiService
import com.example.damprojectfinal.core.api.professionalUser.ProfessionalApiService
import com.example.damprojectfinal.core.dto.posts.PostResponse
import com.example.damprojectfinal.core.dto.posts.UploadResponse
import com.example.damprojectfinal.core.dto.professionalUser.ProfessionalUserAccount
import com.example.damprojectfinal.core.dto.professionalUser.UpdateProfessionalRequest
import com.example.damprojectfinal.core.retro.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import java.io.File
import java.net.ConnectException
import java.net.SocketTimeoutException

class ProfessionalProfileViewModel(
    private val tokenManager: TokenManager,
    private val professionalApiService: ProfessionalApiService, // ADDED: Dependency for fetching professional account details
    private val postsApiService: PostsApiService // ADDED: Dependency for fetching posts
) : ViewModel() {

    // Changed to ProfessionalUserAccount
    private val _profile = MutableStateFlow<ProfessionalUserAccount?>(null)
    val profile: StateFlow<ProfessionalUserAccount?> = _profile.asStateFlow()

    private val _selectedProfileImageUri = MutableStateFlow<Uri?>(null)
    val selectedProfileImageUri: StateFlow<Uri?> = _selectedProfileImageUri.asStateFlow()

    // ADDED: StateFlows for posts
    private val _allPosts = MutableStateFlow<List<PostResponse>>(emptyList())

    // Derived StateFlows for photo and reel posts, combining _allPosts
    val photoPosts: StateFlow<List<PostResponse>> = _allPosts
        .map { posts -> posts.filter { it.mediaType == "image" || it.mediaType == "photo" } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.Lazily,
            initialValue = emptyList()
        )

    val reelPosts: StateFlow<List<PostResponse>> = _allPosts
        .map { posts -> posts.filter { it.mediaType == "video" || it.mediaType == "reel" } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.Lazily,
            initialValue = emptyList()
        )


    fun loadProfessionalProfile(professionalId: String) {
        viewModelScope.launch {
            Log.d("ProfileVM", "Loading profile for ID: $professionalId")
            try {
                // Actual API call to fetch professional account details
                val fetchedAccount = professionalApiService.getProfessionalAccount(professionalId)
                _profile.value = fetchedAccount
                Log.d("ProfileVM", "Profile loaded: ${fetchedAccount.fullName}")
                Log.d("ProfileVM", "📍 Locations count: ${fetchedAccount.locations.size}")
                fetchedAccount.locations.forEachIndexed { index, loc ->
                    Log.d("ProfileVM", "  Location $index:")
                    Log.d("ProfileVM", "    - Name: ${loc.name}")
                    Log.d("ProfileVM", "    - Address: ${loc.address}")
                    Log.d("ProfileVM", "    - Lat/Lng: ${loc.lat}, ${loc.lon}")
                }
                Log.d("ProfileVM", "📬 Old address field: ${fetchedAccount.address}")
            } catch (e: ConnectException) {
                // Network connection error - backend not available
                Log.w("ProfileVM", "Cannot connect to backend: ${e.message}. Profile will not be loaded.")
                // Don't crash - just leave profile as null
            } catch (e: SocketTimeoutException) {
                // Timeout error
                Log.w("ProfileVM", "Request timeout while loading profile: ${e.message}")
            } catch (e: Exception) {
                // Any other error
                Log.e("ProfileVM", "Error loading professional profile: ${e.message}", e)
                // Handle error (e.g., show error message to user)
            }
        }
    }

    // ADDED: Function to fetch posts
    fun fetchProfessionalPosts(professionalId: String) {
        viewModelScope.launch {
            try {
                Log.d("ProfileVM", "Fetching posts for professional ID: $professionalId")
                val fetchedPosts = postsApiService.getPostsByOwnerId(professionalId)
                _allPosts.value = fetchedPosts // Update _allPosts
                Log.d("ProfileVM", "Fetched ${fetchedPosts.size} posts for $professionalId")
            } catch (e: Exception) {
                Log.e("ProfileVM", "Error fetching professional posts: ${e.message}")
                // Handle error (e.g., show a Toast, update UI state with error)
            }
        }
    }

    // State for update operations
    private val _isUpdating = MutableStateFlow(false)
    val isUpdating: StateFlow<Boolean> = _isUpdating.asStateFlow()

    private val _updateSuccess = MutableStateFlow(false)
    val updateSuccess: StateFlow<Boolean> = _updateSuccess.asStateFlow()

    private val _updateError = MutableStateFlow<String?>(null)
    val updateError: StateFlow<String?> = _updateError.asStateFlow()

    fun setSelectedProfileImageUri(uri: Uri?) {
        _selectedProfileImageUri.value = uri
        Log.d("ProfileVM", "Selected Profile Image URI set: $uri")
    }

    fun uploadProfileImage(professionalId: String, imageUri: Uri) {
        viewModelScope.launch {
            Log.d("ProfileVM", "Attempting to upload image for $professionalId: $imageUri")
            // TODO: Implement actual profile image upload and update profile.imageUrl
        }
    }

    suspend fun uploadImage(file: File, mimeType: String? = null): String? {
        return try {
            // Use provided MIME type or detect from file extension
            val detectedMimeType = mimeType ?: when {
                file.name.endsWith(".jpg", ignoreCase = true) || file.name.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
                file.name.endsWith(".png", ignoreCase = true) -> "image/png"
                file.name.endsWith(".gif", ignoreCase = true) -> "image/gif"
                file.name.endsWith(".webp", ignoreCase = true) -> "image/webp"
                else -> "image/jpeg" // Default to jpeg if unknown
            }
            
            Log.d("ProfileVM", "Uploading file: ${file.name} with MIME type: $detectedMimeType")
            val requestFile = file.asRequestBody(detectedMimeType.toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("files", file.name, requestFile)
            val uploadResponse = RetrofitClient.postsApiService.uploadFiles(listOf(filePart))
            if (uploadResponse.urls.isNotEmpty()) {
                Log.d("ProfileVM", "Image uploaded successfully: ${uploadResponse.urls.first()}")
                uploadResponse.urls.first()
            } else {
                Log.e("ProfileVM", "Upload response has no URLs")
                null
            }
        } catch (e: HttpException) {
            // Pass through backend message (e.g. AI food validation) so UI can show it
            val errorBody = e.response()?.errorBody()?.string()
            val finalMsg = errorBody ?: "Failed to upload image (HTTP ${e.code()})"
            Log.e("ProfileVM", "Image upload HTTP ${e.code()} error body: $finalMsg")
            throw Exception(finalMsg)
        } catch (e: Exception) {
            Log.e("ProfileVM", "Error uploading image: ${e.message}", e)
            throw e
        }
    }

    fun updateProfile(
        professionalId: String,
        phone: String?,
        hours: String?,
        address: String?,
        description: String?,
        profilePictureFile: File?,
        profilePictureMimeType: String?,
        backgroundImageFile: File?,
        backgroundImageMimeType: String?,
        locations: List<com.example.damprojectfinal.core.dto.professionalUser.ProfessionalLocation>?
    ) {
        viewModelScope.launch {
            _isUpdating.value = true
            _updateError.value = null
            _updateSuccess.value = false

            try {
                var profilePictureUrl: String? = null
                var imageUrl: String? = null

                // Upload profile picture if provided
                profilePictureFile?.let { file ->
                    Log.d("ProfileVM", "Uploading profile picture... File: ${file.name}, MIME: $profilePictureMimeType")
                    try {
                        profilePictureUrl = uploadImage(file, profilePictureMimeType)
                        Log.d("ProfileVM", "Profile picture uploaded successfully: $profilePictureUrl")
                    } catch (e: Exception) {
                        // Do NOT abort entire profile update if picture fails; just report error and keep existing image
                        Log.e("ProfileVM", "Error uploading profile picture: ${e.message}", e)
                        _updateError.value = e.message ?: "Failed to upload profile picture"
                        profilePictureUrl = _profile.value?.profilePictureUrl
                    }
                } ?: run {
                    Log.d("ProfileVM", "No profile picture file provided, keeping existing: ${_profile.value?.profilePictureUrl}")
                }

                // Upload background image if provided
                backgroundImageFile?.let { file ->
                    Log.d("ProfileVM", "Uploading background image... File: ${file.name}, MIME: $backgroundImageMimeType")
                    try {
                        imageUrl = uploadImage(file, backgroundImageMimeType)
                        Log.d("ProfileVM", "Background image uploaded successfully: $imageUrl")
                    } catch (e: Exception) {
                        // Do NOT abort entire profile update if background fails; just report error and keep existing image
                        Log.e("ProfileVM", "Error uploading background image: ${e.message}", e)
                        _updateError.value = e.message ?: "Failed to upload background image"
                        imageUrl = _profile.value?.imageUrl
                    }
                } ?: run {
                    Log.d("ProfileVM", "No background image file provided, keeping existing: ${_profile.value?.imageUrl}")
                }

                // If no new images uploaded, keep existing URLs
                if (profilePictureUrl == null) {
                    profilePictureUrl = _profile.value?.profilePictureUrl
                }
                if (imageUrl == null) {
                    imageUrl = _profile.value?.imageUrl
                }

                val request = UpdateProfessionalRequest(
                    phone = phone,
                    hours = hours,
                    address = address,
                    description = description,
                    profilePictureUrl = profilePictureUrl,
                    imageUrl = imageUrl,
                    locations = locations
                )

                Log.d("ProfileVM", "Updating profile for $professionalId")
                Log.d("ProfileVM", "Request details:")
                Log.d("ProfileVM", "  - phone: $phone")
                Log.d("ProfileVM", "  - hours: $hours")
                Log.d("ProfileVM", "  - description: $description")
                Log.d("ProfileVM", "  - profilePictureUrl: $profilePictureUrl")
                Log.d("ProfileVM", "  - imageUrl: $imageUrl")
                Log.d("ProfileVM", "  - locations: ${locations?.size ?: 0}")
                
                val updatedProfile = professionalApiService.updateProfessional(professionalId, request)
                
                Log.d("ProfileVM", "Updated profile received:")
                Log.d("ProfileVM", "  - phone: ${updatedProfile.phone}")
                Log.d("ProfileVM", "  - hours: ${updatedProfile.hours}")
                Log.d("ProfileVM", "  - description: ${updatedProfile.description}")
                Log.d("ProfileVM", "  - profilePictureUrl: ${updatedProfile.profilePictureUrl}")
                Log.d("ProfileVM", "  - imageUrl: ${updatedProfile.imageUrl}")
                _profile.value = updatedProfile
                // Clear selected image URI state after successful update
                _selectedProfileImageUri.value = null
                _updateSuccess.value = true
                Log.d("ProfileVM", "Profile updated successfully")
            } catch (e: Exception) {
                Log.e("ProfileVM", "Error updating profile: ${e.message}")
                _updateError.value = e.message ?: "Failed to update profile"
            } finally {
                _isUpdating.value = false
            }
        }
    }

    fun resetUpdateStatus() {
        _updateSuccess.value = false
        _updateError.value = null
    }

    // Follow/Unfollow methods
    fun followProfessional(professionalId: String) {
        viewModelScope.launch {
            try {
                Log.d("ProfileVM", "Following professional: $professionalId")
                val updatedProfile = professionalApiService.followProfessional(professionalId)
                _profile.value = updatedProfile
                Log.d("ProfileVM", "Successfully followed professional")
            } catch (e: Exception) {
                Log.e("ProfileVM", "Error following professional: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun unfollowProfessional(professionalId: String) {
        viewModelScope.launch {
            try {
                Log.d("ProfileVM", "Unfollowing professional: $professionalId")
                val updatedProfile = professionalApiService.unfollowProfessional(professionalId)
                _profile.value = updatedProfile
                Log.d("ProfileVM", "Successfully unfollowed professional")
            } catch (e: Exception) {
                Log.e("ProfileVM", "Error unfollowing professional: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    // UPDATED: ViewModel Factory to provide all necessary dependencies
    companion object {
        fun Factory(
            tokenManager: TokenManager,
            professionalApiService: ProfessionalApiService,
            postsApiService: PostsApiService
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                return ProfessionalProfileViewModel(tokenManager, professionalApiService, postsApiService) as T
            }
        }
    }
}