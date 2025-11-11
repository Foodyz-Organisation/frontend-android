package com.example.damprojectfinal.feature_auth.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.example.damprojectfinal.AuthRoutes
import com.example.damprojectfinal.core.api.AuthApiService
import com.example.damprojectfinal.core.dto.auth.ProfessionalSignupRequest

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

    fun signup() {
        if (email.value.isBlank() || password.value.isBlank() || fullName.value.isBlank()) {
            errorMessage.value = "Please fill in all required fields."
            return
        }

        isLoading.value = true
        errorMessage.value = null

        viewModelScope.launch {
            try {
                val request = ProfessionalSignupRequest(
                    email = email.value,
                    password = password.value,
                    fullName = fullName.value,
                    licenseNumber = "" // optional
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
