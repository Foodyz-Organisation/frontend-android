// File: com.example.damprojectfinal.professional.viewmodel/ProfessionalProfileViewModel.kt

package com.example.damprojectfinal.professional.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.damprojectfinal.core.api.TokenManager
import com.example.damprojectfinal.core.dto.professionalUser.ProfessionalProfile
import com.example.damprojectfinal.core.dto.posts.PostResponse
import com.example.damprojectfinal.core.api.posts.RetrofitClient
import com.example.damprojectfinal.user.feature_posts.ui.post_management.AppMediaType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.util.Log

class ProfessionalProfileViewModel(
    private val tokenManager: TokenManager,
    private val postsApiService: com.example.damprojectfinal.core.api.posts.PostsApiService
) : ViewModel() {

    private val _profile = MutableStateFlow<ProfessionalProfile?>(null)
    val profile: StateFlow<ProfessionalProfile?> = _profile.asStateFlow()

    private val _selectedProfileImageUri = MutableStateFlow<Uri?>(null)
    val selectedProfileImageUri: StateFlow<Uri?> = _selectedProfileImageUri.asStateFlow()

    private val _allPosts = MutableStateFlow<List<PostResponse>>(emptyList())
    val allPosts: StateFlow<List<PostResponse>> = _allPosts.asStateFlow() // Still useful for internal ViewModel state

    val photoPosts: StateFlow<List<PostResponse>> = _allPosts.map { posts ->
        posts.filter { it.mediaType == AppMediaType.IMAGE.value }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = emptyList()
    )

    val reelPosts: StateFlow<List<PostResponse>> = _allPosts.map { posts ->
        posts.filter { it.mediaType == AppMediaType.REEL.value }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = emptyList()
    )

    fun loadProfessionalProfile(professionalId: String) {
        viewModelScope.launch {
            Log.d("ProfileVM", "Loading profile for ID: $professionalId")
            kotlinx.coroutines.delay(500) // Simulate API call delay
            // TODO: Replace with actual API call to fetch professional profile
            _profile.value = ProfessionalProfile(
                id = professionalId,
                name = "Chili's",
                imageUrl = null,
                rating = 4.7,
                reviewCount = 1243,
                priceRange = "$$",
                cuisine = "Italian, Pizza, Pasta",
                deliveryTime = "30-45 min",
                takeawayTime = "Ready in 15 min",
                dineInAvailable = true,
                address = "123 Avenue Habib Bourguiba, Tunis",
                phoneNumber = "+216 71 123 456",
                openingHours = "10:00 AM - 11:00 PM"
            )
            Log.d("ProfileVM", "Profile loaded: ${_profile.value?.name}")
        }
    }

    fun loadProfessionalPosts(professionalId: String) {
        viewModelScope.launch {
            try {
                Log.d("ProfileVM", "Attempting to load posts by owner ID: $professionalId")
                // CORRECTED: Use the dedicated API endpoint for posts by owner
                val fetchedPosts = postsApiService.getPostsByOwnerId(ownerId = professionalId)

                if (fetchedPosts == null) { // Check if the list itself is null (though Retrofit usually returns emptyList)
                    Log.e("ProfileVM", "postsApiService.getPostsByOwnerId() returned null.")
                    _allPosts.value = emptyList()
                    return@launch
                }

                Log.d("ProfileVM", "Fetched ${fetchedPosts.size} posts for ID: $professionalId")
                _allPosts.value = fetchedPosts // No client-side filtering needed now
            } catch (e: Exception) {
                Log.e("ProfileVM", "Error loading professional posts: ${e.message}", e)
                _allPosts.value = emptyList()
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

    companion object {
        fun Factory(tokenManager: TokenManager, postsApiService: com.example.damprojectfinal.core.api.posts.PostsApiService): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                return ProfessionalProfileViewModel(tokenManager, postsApiService) as T
            }
        }
    }
}
