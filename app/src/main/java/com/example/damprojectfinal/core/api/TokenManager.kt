package com.example.damprojectfinal.core.api

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_auth")

data class AuthState(
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
    val role: String
)

class TokenManager(private val context: Context) {

    private val TAG = "TokenManager"

    private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
    private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
    private val USER_ID_KEY = stringPreferencesKey("user_id")
    private val USER_ROLE_KEY = stringPreferencesKey("user_role") // This holds the user's type

    suspend fun saveTokens(
        accessToken: String,
        refreshToken: String,
        userId: String,
        role: String
    ) {
        Log.d(TAG, "========== SAVING TOKENS ==========")
        Log.d(TAG, "AccessToken (first 30 chars): ${accessToken.take(30)}...")
        Log.d(TAG, "RefreshToken present: ${refreshToken.isNotEmpty()}")
        Log.d(TAG, "UserId: $userId")
        Log.d(TAG, "Role: $role")

        try {
            context.dataStore.edit { prefs ->
                prefs[ACCESS_TOKEN_KEY] = accessToken
                prefs[REFRESH_TOKEN_KEY] = refreshToken
                prefs[USER_ID_KEY] = userId
                prefs[USER_ROLE_KEY] = role
            }
            Log.d(TAG, "✅ Tokens saved successfully")

            // Vérification immédiate
            val savedToken = context.dataStore.data.map { it[ACCESS_TOKEN_KEY] }.first()
            Log.d(TAG, "Verification - Token in DataStore: ${savedToken?.take(30)}...")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error saving tokens: ${e.message}", e)
            throw e
        }
        Log.d(TAG, "===================================")
    }

    fun getAccessTokenSync(): String? = runBlocking {
        try {
            val token = context.dataStore.data.map { it[ACCESS_TOKEN_KEY] }.first()
            if (token.isNullOrEmpty()) {
                Log.w(TAG, "⚠️ getAccessToken() -> NULL or EMPTY")
            } else {
                Log.d(TAG, "🔑 getAccessToken() -> ${token.take(30)}...")
            }
            return@runBlocking token
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error getting access token: ${e.message}", e)
            return@runBlocking null
        }
    }

    suspend fun getAccessTokenAsync(): String? {
        return try {
            val token = context.dataStore.data.map { it[ACCESS_TOKEN_KEY] }.first()
            if (token.isNullOrEmpty()) {
                Log.w(TAG, "⚠️ getAccessTokenAsync() -> NULL or EMPTY")
            } else {
                Log.d(TAG, "🔑 getAccessTokenAsync() -> ${token.take(30)}...")
            }
            token
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error getting access token async: ${e.message}", e)
            null
        }
    }

    // Alias for getAccessTokenSync() for compatibility
    fun getAccessTokenBlocking(): String? = getAccessTokenSync()

    fun getAccessTokenFlow(): Flow<String?> = context.dataStore.data.map {
        val token = it[ACCESS_TOKEN_KEY]
        Log.d(TAG, "📡 getAccessTokenFlow() emitting: ${token?.take(30)}...")
        token
    }



    fun getRefreshToken(): String? = runBlocking {
        try {
            val token = context.dataStore.data.map { it[REFRESH_TOKEN_KEY] }.first()
            Log.d(
                TAG,
                "🔄 getRefreshToken() -> ${if (token.isNullOrEmpty()) "NULL" else "EXISTS"}"
            )
            return@runBlocking token
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error getting refresh token: ${e.message}", e)
            return@runBlocking null
        }
    }

    fun getUserIdBlocking(): String? = runBlocking {
        context.dataStore.data.map { it[USER_ID_KEY] }.first()
    }

    fun getUserId(): String? = runBlocking {
        try {
            val userId = context.dataStore.data.map { it[USER_ID_KEY] }.first()
            if (userId.isNullOrEmpty()) {
                Log.w(TAG, "⚠️ getUserId() -> NULL or EMPTY")
            } else {
                Log.d(TAG, "👤 getUserId() -> $userId")
            }
            return@runBlocking userId
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error getting user id: ${e.message}", e)
            return@runBlocking null
        }
    }

    suspend fun getUserIdAsync(): String? {
        return try {
            val userId = context.dataStore.data.map { it[USER_ID_KEY] }.first()
            if (userId.isNullOrEmpty()) {
                Log.w(TAG, "⚠️ getUserIdAsync() -> NULL or EMPTY")
            } else {
                Log.d(TAG, "👤 getUserIdAsync() -> $userId")
            }
            userId
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error getting user id async: ${e.message}", e)
            null
        }
    }

    fun getUserIdFlow(): Flow<String?> = context.dataStore.data.map {
        val userId = it[USER_ID_KEY]
        Log.d(TAG, "📡 getUserIdFlow() emitting: $userId")
        userId
    }

    fun getUserRole(): Flow<String?> = context.dataStore.data.map {
        val role = it[USER_ROLE_KEY]
        Log.d(TAG, "📡 getUserRole() emitting: $role")
        role
    }

    fun getUserRoleSync(): String? = runBlocking {
        try {
            val role = context.dataStore.data.map { it[USER_ROLE_KEY] }.first()
            Log.d(TAG, "🎭 getUserRoleSync() -> $role")
            return@runBlocking role
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error getting user role: ${e.message}", e)
            return@runBlocking null
        }
    }

    fun isLoggedIn(): Boolean = runBlocking {
        val token = context.dataStore.data.map { it[ACCESS_TOKEN_KEY] }.first()
        val isLogged = !token.isNullOrEmpty()
        Log.d(TAG, "🔐 isLoggedIn() -> $isLogged")
        isLogged
    }

    suspend fun isLoggedInAsync(): Boolean {
        val token = context.dataStore.data.map { it[ACCESS_TOKEN_KEY] }.first()
        val isLogged = !token.isNullOrEmpty()
        Log.d(TAG, "🔐 isLoggedInAsync() -> $isLogged")
        return isLogged
    }

    fun getUserType(): String? = runBlocking {
        context.dataStore.data.map { it[USER_ROLE_KEY] }.first()
    }

    suspend fun debugPrintAll() {
        Log.d(TAG, "========== DEBUG ALL TOKENS ==========")
        try {
            context.dataStore.data.first().asMap().forEach { (key, value) ->
                when (key.name) {
                    "access_token" -> Log.d(
                        TAG,
                        "AccessToken: ${(value as? String)?.take(30)}..."
                    )
                    "refresh_token" -> Log.d(
                        TAG,
                        "RefreshToken: ${if ((value as? String)?.isNotEmpty() == true) "EXISTS" else "NULL"}"
                    )
                    "user_id" -> Log.d(TAG, "UserId: $value")
                    "user_role" -> Log.d(TAG, "Role: $value")
                    else -> Log.d(TAG, "$key: $value")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error printing debug info: ${e.message}", e)
        }
        Log.d(TAG, "======================================")
    }

    suspend fun clearTokens() {
        Log.d(TAG, "🗑️ Clearing all tokens...")
        try {
            context.dataStore.edit { it.clear() }
            Log.d(TAG, "✅ Tokens cleared successfully")

            // Vérification
            val token = context.dataStore.data.map { it[ACCESS_TOKEN_KEY] }.first()
            if (token == null) {
                Log.d(TAG, "✅ Verification: DataStore is empty")
            } else {
                Log.w(TAG, "⚠️ Warning: Token still exists after clear!")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error clearing tokens: ${e.message}", e)
            throw e
        }
    }

    suspend fun updateAccessToken(newAccessToken: String) {
        Log.d(TAG, "🔄 Updating access token...")
        Log.d(TAG, "New token (first 30 chars): ${newAccessToken.take(30)}...")
        try {
            context.dataStore.edit { prefs ->
                prefs[ACCESS_TOKEN_KEY] = newAccessToken
            }
            Log.d(TAG, "✅ Access token updated successfully")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error updating access token: ${e.message}", e)
            throw e
        }
    }

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
}


