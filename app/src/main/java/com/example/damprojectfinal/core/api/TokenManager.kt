package com.example.damprojectfinal.core.api

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking


private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_auth")

class TokenManager(private val context: Context) {

    private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
    private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
    private val USER_ID_KEY = stringPreferencesKey("user_id")
    private val USER_ROLE_KEY = stringPreferencesKey("user_role") // This holds the user's type

    suspend fun saveTokens(accessToken: String, refreshToken: String, userId: String, role: String) {
        context.dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN_KEY] = accessToken
            prefs[REFRESH_TOKEN_KEY] = refreshToken
            prefs[USER_ID_KEY] = userId
            prefs[USER_ROLE_KEY] = role // Saving the role (which is our ownerType)
        }
    }

    fun getAccessToken(): String? = runBlocking {
        context.dataStore.data.map { it[ACCESS_TOKEN_KEY] }.first()
    }

    fun getRefreshToken(): String? = runBlocking {
        context.dataStore.data.map { it[REFRESH_TOKEN_KEY] }.first()
    }

    fun getUserId(): String? = runBlocking {
        context.dataStore.data.map { it[USER_ID_KEY] }.first()
    }

    // --- MODIFIED METHOD: Renamed to getUserType and made blocking ---
    fun getUserType(): String? = runBlocking {
        context.dataStore.data.map { it[USER_ROLE_KEY] }.first()
    }
    // --- END MODIFIED METHOD ---

    suspend fun clearTokens() {
        context.dataStore.edit { it.clear() }
    }
}
