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

    // -------------------------------------------------------------------------
    // --- Non-Blocking Getters (Recommended for Compose/Async use) ---
    // -------------------------------------------------------------------------

    /**
     * Returns the access token as a non-blocking Flow.
     */
    fun getAccessToken(): Flow<String?> = context.dataStore.data.map { it[ACCESS_TOKEN_KEY] }

    /**
     * Returns the refresh token as a non-blocking Flow.
     */
    fun getRefreshToken(): Flow<String?> = context.dataStore.data.map { it[REFRESH_TOKEN_KEY] }

    /**
     * Returns the User ID as a non-blocking Flow.
     */
    fun getUserId(): Flow<String?> = context.dataStore.data.map { it[USER_ID_KEY] }

    /**
     * Returns the User Role as a non-blocking Flow.
     */
    fun getUserRole(): Flow<String?> = context.dataStore.data.map { it[USER_ROLE_KEY] }


    // -------------------------------------------------------------------------
    // --- Blocking Getters (Used for synchronous access, like in remember {}) ---
    // -------------------------------------------------------------------------

    // fun getAccessTokenBlocking(): String? = runBlocking {
    //     context.dataStore.data.map { it[ACCESS_TOKEN_KEY] }.first()
    // }

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


    /**
     * Clears all saved tokens and user details from DataStore.
     */
    suspend fun clearTokens() {
        context.dataStore.edit { prefs ->
            prefs.remove(ACCESS_TOKEN_KEY)
            prefs.remove(REFRESH_TOKEN_KEY)
            prefs.remove(USER_ID_KEY)
            prefs.remove(USER_ROLE_KEY)
        }
    }
}