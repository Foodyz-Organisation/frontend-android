package com.example.damprojectfinal.professional.feature_profile.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.damprojectfinal.core.api.TokenManager // Still needed if you plan to use it later
import com.example.damprojectfinal.core.dto.professionalUser.ProfessionalProfile // Profile data class
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log // Keep Log import
import kotlinx.coroutines.delay

class ProfessionalProfileViewModel(
    private val tokenManager: TokenManager // Simplified: Removed postsApiService dependency
) : ViewModel() {

    private val _profile = MutableStateFlow<ProfessionalProfile?>(null)
    val profile: StateFlow<ProfessionalProfile?> = _profile.asStateFlow()

    private val _selectedProfileImageUri = MutableStateFlow<Uri?>(null)
    val selectedProfileImageUri: StateFlow<Uri?> = _selectedProfileImageUri.asStateFlow()

    // Removed: _allPosts, photoPosts, reelPosts StateFlows

    fun loadProfessionalProfile(professionalId: String) {
        viewModelScope.launch {
            Log.d("ProfileVM", "Loading profile for ID: $professionalId")
            delay(500) // Simulate API call delay
            // TODO: Replace with actual API call to fetch professional profile
            _profile.value = ProfessionalProfile(
                id = professionalId,
                name = "Chili's",
                imageUrl = "https://picsum.photos/id/237/200/300", // Added a dummy image URL for now
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

    // Removed: loadProfessionalPosts function

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

    // Reverted ViewModel Factory to match simpler constructor
    companion object {
        fun Factory(tokenManager: TokenManager): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                return ProfessionalProfileViewModel(tokenManager) as T
            }
        }
    }
}
