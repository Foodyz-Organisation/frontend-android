package com.example.damprojectfinal.core.utils

import android.util.Patterns

/**
 * Password strength levels
 */
enum class PasswordStrength {
    WEAK,
    MEDIUM,
    STRONG
}

/**
 * Validation result with error message
 */
data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)

/**
 * Centralized validation utilities matching backend DTO validation rules
 */
object ValidationUtils {

    /**
     * Validate email format
     */
    fun validateEmail(email: String): ValidationResult {
        return when {
            email.isBlank() -> ValidationResult(false, "Email is required")
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> 
                ValidationResult(false, "Please enter a valid email address")
            else -> ValidationResult(true)
        }
    }

    /**
     * Validate password with minimum length requirement
     */
    fun validatePassword(password: String): ValidationResult {
        return when {
            password.isBlank() -> ValidationResult(false, "Password is required")
            password.length < 8 -> ValidationResult(false, "Password must be at least 8 characters long")
            else -> ValidationResult(true)
        }
    }

    /**
     * Calculate password strength based on complexity
     */
    fun validatePasswordStrength(password: String): PasswordStrength {
        if (password.length < 8) return PasswordStrength.WEAK

        val hasUppercase = password.any { it.isUpperCase() }
        val hasLowercase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecialChar = password.any { !it.isLetterOrDigit() }

        return when {
            password.length >= 8 && hasUppercase && hasLowercase && hasDigit && hasSpecialChar -> 
                PasswordStrength.STRONG
            password.length >= 8 && (hasUppercase || hasDigit || hasLowercase) -> 
                PasswordStrength.MEDIUM
            else -> 
                PasswordStrength.WEAK
        }
    }

    /**
     * Get password strength message
     */
    fun getPasswordStrengthMessage(strength: PasswordStrength): String {
        return when (strength) {
            PasswordStrength.WEAK -> "Weak - Use at least 8 characters with upper, lower, numbers & symbols"
            PasswordStrength.MEDIUM -> "Medium - Add more variety (upper/lower/numbers/symbols)"
            PasswordStrength.STRONG -> "Strong password ✓"
        }
    }

    /**
     * Validate username with minimum length
     */
    fun validateUsername(username: String): ValidationResult {
        return when {
            username.isBlank() -> ValidationResult(false, "Username is required")
            username.length < 3 -> ValidationResult(false, "Username must be at least 3 characters")
            else -> ValidationResult(true)
        }
    }

    /**
     * Validate Tunisian phone number format: +216XXXXXXXX
     */
    fun validatePhone(phone: String): ValidationResult {
        if (phone.isBlank()) return ValidationResult(true) // Optional field
        
        val tunisianPhoneRegex = "^\\+216\\d{8}$".toRegex()
        return if (tunisianPhoneRegex.matches(phone)) {
            ValidationResult(true)
        } else {
            ValidationResult(
                false, 
                "Phone number must be in Tunisian format: +216 followed by 8 digits\nExample: +21612345678"
            )
        }
    }

    /**
     * Validate address with minimum length
     */
    fun validateAddress(address: String): ValidationResult {
        return when {
            address.isBlank() -> ValidationResult(false, "Address is required")
            address.length < 5 -> ValidationResult(false, "Address must be at least 5 characters")
            else -> ValidationResult(true)
        }
    }

    /**
     * Validate full name with minimum length
     */
    fun validateFullName(fullName: String): ValidationResult {
        return when {
            fullName.isBlank() -> ValidationResult(false, "Full name is required")
            fullName.length < 2 -> ValidationResult(false, "Full name must be at least 2 characters")
            else -> ValidationResult(true)
        }
    }

    /**
     * Validate password confirmation matches
     */
    fun validatePasswordMatch(password: String, confirmPassword: String): ValidationResult {
        return if (password == confirmPassword) {
            ValidationResult(true)
        } else {
            ValidationResult(false, "Passwords do not match")
        }
    }
}
