package com.example.damprojectfinal.core.api

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking


// Use the delegation to manage DataStore instance
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_auth")

data class AuthState(
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
    val role: String
)

class TokenManager(private val context: Context) {

    private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
    private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
    private val USER_ID_KEY = stringPreferencesKey("user_id")
    private val USER_ROLE_KEY = stringPreferencesKey("user_role")

    /**
     * Saves all authentication tokens and user details in DataStore.
     */
    suspend fun saveTokens(accessToken: String, refreshToken: String, userId: String, role: String) {
        context.dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN_KEY] = accessToken
            prefs[REFRESH_TOKEN_KEY] = refreshToken
            prefs[USER_ID_KEY] = userId
            prefs[USER_ROLE_KEY] = role
        }
    }

    /**
     * Returns the access token as a non-blocking Flow.
     */
    fun getAccessToken(): Flow<String?> = context.dataStore.data.map { it[ACCESS_TOKEN_KEY] }

    /**
     * Returns the refresh token as a non-blocking Flow.
     */
    fun getRefreshToken(): Flow<String?> = context.dataStore.data.map { it[REFRESH_TOKEN_KEY] }


     fun getAccessTokenBlocking(): String? = runBlocking {
         context.dataStore.data.map { it[ACCESS_TOKEN_KEY] }.first()
    }

    // fun getRefreshTokenBlocking(): String? = runBlocking {
    //     context.dataStore.data.map { it[REFRESH_TOKEN_KEY] }.first()
    // }

    /**
     * Returns the User ID by blocking the current thread until the value is available.
     * Use this cautiously and typically only outside of normal composable flows.
     */
    fun getUserIdBlocking(): String? = runBlocking {
        context.dataStore.data.map { it[USER_ID_KEY] }.first()
    }

    fun getUserId(): String? = runBlocking {
        context.dataStore.data.map { it[USER_ID_KEY] }.first()
    }

    fun getUserIdFlow(): Flow<String?> = context.dataStore.data.map { it[USER_ID_KEY] }

    // Returning a Flow is the standard, non-blocking way to get DataStore values.
    fun getUserRole() = context.dataStore.data.map { it[USER_ROLE_KEY] }

    /**
     * Clears all saved tokens and user details from DataStore.
     */
    suspend fun getAuthState(): AuthState? {
        val prefs = context.dataStore.data.first()
        val access = prefs[ACCESS_TOKEN_KEY]
        val refresh = prefs[REFRESH_TOKEN_KEY]
        val userId = prefs[USER_ID_KEY]
        val role = prefs[USER_ROLE_KEY]
        return if (!access.isNullOrBlank() && !refresh.isNullOrBlank() && !userId.isNullOrBlank() && !role.isNullOrBlank()) {
            AuthState(
                accessToken = access,
                refreshToken = refresh,
                userId = userId,
                role = role
            )
        } else {
            null
        }
    }

    suspend fun clearTokens() {
        context.dataStore.edit { prefs ->
            prefs.remove(ACCESS_TOKEN_KEY)
            prefs.remove(REFRESH_TOKEN_KEY)
            prefs.remove(USER_ID_KEY)
            prefs.remove(USER_ROLE_KEY)
        }
    }
}
