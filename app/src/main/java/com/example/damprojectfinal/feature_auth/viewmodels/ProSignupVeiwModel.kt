package com.example.damprojectfinal.feature_auth.viewmodels

import android.content.Context
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.example.damprojectfinal.AuthRoutes
import com.example.damprojectfinal.core.api.AuthApiService
import com.example.damprojectfinal.core.dto.auth.ProfessionalSignupRequest
import com.example.damprojectfinal.core.dto.auth.LocationDto
import com.example.damprojectfinal.core.utils.ImageCompressor
import com.example.damprojectfinal.professional.feature_event.LocationData

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class ProSignupViewModel(
    private val authApiService: AuthApiService,
    private val navController: NavHostController
) : ViewModel() {

    private val TAG = "ProSignupViewModel"

    val email = mutableStateOf("")
    val password = mutableStateOf("")
    val confirmPassword = mutableStateOf("")
    val fullName = mutableStateOf("")

    val isLoading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)
    val isSignupSuccess = mutableStateOf(false)
    val licenseNumber = mutableStateOf("")
    
    // Image upload states
    val permitImageUri = mutableStateOf<Uri?>(null)
    val permitImageBase64 = mutableStateOf<String?>(null)
    val isValidatingPermit = mutableStateOf(false)
    val isCompressingImage = mutableStateOf(false)
    val permitFileName = mutableStateOf<String?>(null)
    val permitFileSize = mutableStateOf<String?>(null)
    
    // Location data
    val selectedLocation = mutableStateOf<LocationData?>(null)
    val showLocationPicker = mutableStateOf(false)
    
    // Multi-step flow management
    val currentStep = mutableStateOf(1) // 1, 2, or 3
    val showSuccessDialog = mutableStateOf(false)
    val permitNumberExtracted = mutableStateOf<String?>(null)
    
    // Step validation
    fun canProceedFromStep1(): Boolean {
        val passwordStrength = com.example.damprojectfinal.core.utils.ValidationUtils.validatePasswordStrength(password.value)
        return fullName.value.isNotBlank() && 
               email.value.isNotBlank() && 
               passwordStrength == com.example.damprojectfinal.core.utils.PasswordStrength.STRONG &&
               confirmPassword.value == password.value
    }
    
    fun canProceedFromStep2(): Boolean {
        // Permit image is optional, so always true
        return true
    }
    
    /**
     * Convert and compress image URI to base64 with automatic compression
     * Uses ImageCompressor to ensure image is under 800 KB for OCR API
     */
    fun convertImageToBase64(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                isCompressingImage.value = true
                Log.d(TAG, "📸 Image selected: $uri")
                
                // Get file info before compression
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        val sizeIndex = it.getColumnIndex(android.provider.OpenableColumns.SIZE)
                        
                        if (nameIndex != -1) {
                            permitFileName.value = it.getString(nameIndex)
                        }
                        
                        if (sizeIndex != -1) {
                            val size = it.getLong(sizeIndex)
                            val sizeKB = size / 1024
                            Log.d(TAG, "📊 Original file size: $sizeKB KB")
                        }
                    }
                }
                
                // Compress image in background thread (takes 1-3 seconds)
                val compressedBase64 = withContext(Dispatchers.IO) {
                    ImageCompressor.compressImageToBase64(context, uri)
                }
                
                // Calculate compressed size
                val compressedSizeKB = (compressedBase64.length * 3 / 4) / 1024
                permitFileSize.value = formatFileSize(compressedSizeKB.toLong() * 1024)
                
                // Update state
                permitImageBase64.value = compressedBase64
                permitImageUri.value = uri
                
                Log.d(TAG, "✅ Image compressed and ready! Size: ${permitFileSize.value}")
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Image compression failed: ${e.message}", e)
                errorMessage.value = "Failed to process image. Please try another photo."
                
                // Clear any partial data
                clearPermitImage()
            } finally {
                isCompressingImage.value = false
            }
        }
    }
    
    // Helper function to format file size
    private fun formatFileSize(size: Long): String {
        val kb = size / 1024.0
        val mb = kb / 1024.0
        
        return when {
            mb >= 1.0 -> String.format("%.2f MB", mb)
            kb >= 1.0 -> String.format("%.2f KB", kb)
            else -> "$size B"
        }
    }
    
    // Clear permit image
    fun clearPermitImage() {
        permitImageUri.value = null
        permitImageBase64.value = null
        permitFileName.value = null
        permitFileSize.value = null
        isCompressingImage.value = false
    }
    
    fun canProceedFromStep3(): Boolean {
        // Location is optional, so always true
        return true
    }
    
    fun nextStep() {
        when (currentStep.value) {
            1 -> {
                val emailValidation = com.example.damprojectfinal.core.utils.ValidationUtils.validateEmail(email.value)
                val passwordValidation = com.example.damprojectfinal.core.utils.ValidationUtils.validatePassword(password.value)
                val passwordStrength = com.example.damprojectfinal.core.utils.ValidationUtils.validatePasswordStrength(password.value)

                if (canProceedFromStep1()) {
                    errorMessage.value = null
                    currentStep.value = 2
                } else {
                    errorMessage.value = when {
                        fullName.value.isBlank() -> "Please enter your full name"
                        !emailValidation.isValid -> emailValidation.errorMessage
                        !passwordValidation.isValid -> passwordValidation.errorMessage
                        passwordStrength != com.example.damprojectfinal.core.utils.PasswordStrength.STRONG -> 
                            "Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, one number, and one special character"
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

        Log.d(TAG, "🚀 Starting professional signup...")
        Log.d(TAG, "📧 Email: ${email.value}")
        Log.d(TAG, "📸 Has permit image: ${permitImageBase64.value != null}")
        
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
                    licenseImage = permitImageBase64.value, // Base64 image for OCR validation
                    locations = locations
                )

                Log.d(TAG, "📤 Sending signup request to backend...")
                val response = authApiService.professionalSignup(request)
                Log.d(TAG, "✅ Signup response received!")

                // Store the extracted permit number if available
                permitNumberExtracted.value = response.permitNumber
                
                isSignupSuccess.value = true
                showSuccessDialog.value = true
                
                // Delay navigation to show success animation
                kotlinx.coroutines.delay(3000)

                // ✅ Use AuthRoutes constants
                navController.navigate(AuthRoutes.LOGIN) {
                    popUpTo(AuthRoutes.PRO_SIGNUP) { inclusive = true }
                }

            } catch (e: Exception) {
                // Handle specific validation errors from backend
                val errorMsg = e.message ?: "Professional signup failed."
                
                Log.e(TAG, "❌ Professional signup failed: $errorMsg", e)
                
                // Check if it's a permit validation error
                val isPermitValidationError = errorMsg.contains("does not appear to be a Tunisian restaurant", ignoreCase = true) ||
                                             errorMsg.contains("Could not extract permit number", ignoreCase = true) ||
                                             errorMsg.contains("permit", ignoreCase = true) ||
                                             errorMsg.contains("validation", ignoreCase = true) ||
                                             errorMsg.contains("Image quality too low", ignoreCase = true) ||
                                             errorMsg.contains("no readable text", ignoreCase = true) ||
                                             errorMsg.contains("clearer photo", ignoreCase = true)
                
                when {
                    isPermitValidationError -> {
                        errorMessage.value = "We were not able to validate the document provided, please provide another one"
                        Log.w(TAG, "⚠️ Document validation failed - asking user to provide another document")
                        
                        // Clear the invalid image so user can upload a new one
                        clearPermitImage()
                        
                        // Go back to step 2 (document upload step) so user can upload new document
                        currentStep.value = 2
                    }
                    errorMsg.contains("email", ignoreCase = true) || errorMsg.contains("mail", ignoreCase = true) -> {
                        errorMessage.value = "This mail already exist"
                        Log.w(TAG, "⚠️ Email already exists")
                        // Go back to step 1 (Personal Details)
                        currentStep.value = 1
                    }
                    else -> {
                        errorMessage.value = errorMsg.replace("Technical Details: ", "")
                        Log.e(TAG, "⚠️ Error: $errorMsg")
                    }
                }
            } finally {
                isLoading.value = false
                Log.d(TAG, "✅ Loading state set to false")
            }
        }
    }
}
