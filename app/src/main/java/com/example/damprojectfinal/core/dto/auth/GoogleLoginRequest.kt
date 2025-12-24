package com.example.damprojectfinal.core.dto.auth

import kotlinx.serialization.Serializable

/**
 * Request body for Google Sign-In authentication
 * Matches backend GoogleLoginDto - only sends idToken
 * Backend extracts email, name, and picture from the token itself
 */
@Serializable
data class GoogleLoginRequest(
    val idToken: String
)
