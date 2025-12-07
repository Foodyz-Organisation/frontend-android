package com.example.damprojectfinal.professional.feature_profile.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.damprojectfinal.core.api.TokenManager
import com.example.damprojectfinal.core.api.posts.PostsApiService // Import PostsApiService
import com.example.damprojectfinal.core.api.professionalUser.ProfessionalApiService // Import ProfessionalApiService
import com.example.damprojectfinal.core.dto.posts.PostResponse // Import PostResponse
import com.example.damprojectfinal.core.dto.professionalUser.ProfessionalUserAccount // Import the new ProfessionalUserAccount DTO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update // Needed for update { } calls
import kotlinx.coroutines.launch
import android.util.Log
import kotlinx.coroutines.flow.combine // For combining flows for filtering (alternative to .also{})
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted

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
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )

    val reelPosts: StateFlow<List<PostResponse>> = _allPosts
        .map { posts -> posts.filter { it.mediaType == "video" || it.mediaType == "reel" } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )


    fun loadProfessionalProfile(professionalId: String) {
        viewModelScope.launch {
            Log.d("ProfileVM", "Loading profile for ID: $professionalId")
            try {
                // Actual API call to fetch professional account details
                val fetchedAccount = professionalApiService.getProfessionalAccount(professionalId)
                _profile.value = fetchedAccount
                Log.d("ProfileVM", "Profile loaded: ${fetchedAccount.professionalData.fullName}")
            } catch (e: Exception) {
                Log.e("ProfileVM", "Error loading professional profile: ${e.message}")
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
