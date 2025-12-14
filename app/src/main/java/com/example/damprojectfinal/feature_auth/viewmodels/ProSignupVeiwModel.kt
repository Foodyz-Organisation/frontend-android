package com.example.damprojectfinal.feature_auth.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.example.damprojectfinal.AuthRoutes
import com.example.damprojectfinal.core.api.AuthApiService
import com.example.damprojectfinal.core.dto.auth.ProfessionalSignupRequest
import com.example.damprojectfinal.core.dto.auth.LocationDto
import com.example.damprojectfinal.professional.feature_event.LocationData

import kotlinx.coroutines.launch

class ProSignupViewModel(
    private val authApiService: AuthApiService,
    private val navController: NavHostController
) : ViewModel() {

    val email = mutableStateOf("")
    val password = mutableStateOf("")
    val fullName = mutableStateOf("")

    val isLoading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)
    val isSignupSuccess = mutableStateOf(false)
    val licenseNumber = mutableStateOf("")
    
    // Location data
    val selectedLocation = mutableStateOf<LocationData?>(null)
    val showLocationPicker = mutableStateOf(false)

    fun signup() {
        if (email.value.isBlank() || password.value.isBlank() || fullName.value.isBlank()) {
            errorMessage.value = "Please fill in all required fields."
            return
        }

        isLoading.value = true
        errorMessage.value = null

        viewModelScope.launch {
            try {
                // Build locations array if location is selected
                val locations = selectedLocation.value?.let { location ->
                    listOf(
                        LocationDto(
                            name = null, // Optional branch name (can be set later)
                            address = location.name.ifEmpty { null }, // Full address from geocoding
                            lat = location.latitude,
                            lon = location.longitude
                        )
                    )
                }

                val request = ProfessionalSignupRequest(
                    email = email.value,
                    password = password.value,
                    fullName = fullName.value,
                    licenseNumber = licenseNumber.value, // now dynamic
                    locations = locations
                )

                val response = authApiService.professionalSignup(request)

                isSignupSuccess.value = true

                // ✅ Use AuthRoutes constants
                navController.navigate(AuthRoutes.LOGIN) {
                    popUpTo(AuthRoutes.PRO_SIGNUP) { inclusive = true }
                }

            } catch (e: Exception) {
                errorMessage.value = e.message ?: "Professional signup failed."
            } finally {
                isLoading.value = false
            }
        }
    }
}
