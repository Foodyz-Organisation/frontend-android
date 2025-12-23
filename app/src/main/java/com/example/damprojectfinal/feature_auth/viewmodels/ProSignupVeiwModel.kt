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
    val confirmPassword = mutableStateOf("")
    val fullName = mutableStateOf("")

    val isLoading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)
    val isSignupSuccess = mutableStateOf(false)
    val licenseNumber = mutableStateOf("")
    
    // Location data
    val selectedLocation = mutableStateOf<LocationData?>(null)
    val showLocationPicker = mutableStateOf(false)
    
    // Multi-step flow management
    val currentStep = mutableStateOf(1) // 1, 2, or 3
    val showSuccessDialog = mutableStateOf(false)
    
    // Step validation
    fun canProceedFromStep1(): Boolean {
        return fullName.value.isNotBlank() && 
               email.value.isNotBlank() && 
               password.value.length >= 6 &&
               confirmPassword.value == password.value
    }
    
    fun canProceedFromStep2(): Boolean {
        // License is optional, so always true
        return true
    }
    
    fun canProceedFromStep3(): Boolean {
        // Location is optional, so always true
        return true
    }
    
    fun nextStep() {
        when (currentStep.value) {
            1 -> {
                if (canProceedFromStep1()) {
                    errorMessage.value = null
                    currentStep.value = 2
                } else {
                    errorMessage.value = when {
                        fullName.value.isBlank() -> "Please enter your full name"
                        email.value.isBlank() -> "Please enter your email address"
                        password.value.length < 6 -> "Password must be at least 6 characters"
                        confirmPassword.value != password.value -> "Passwords do not match"
                        else -> "Please fill in all required fields"
                    }
                }
            }
            2 -> {
                errorMessage.value = null
                currentStep.value = 3
            }
            3 -> {
                // Final step - trigger signup
                signup()
            }
        }
    }
    
    fun previousStep() {
        if (currentStep.value > 1) {
            errorMessage.value = null
            currentStep.value--
        }
    }

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
                showSuccessDialog.value = true
                
                // Delay navigation to show success animation
                kotlinx.coroutines.delay(2500)

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
