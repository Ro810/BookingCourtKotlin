package com.example.bookingcourt.data.local.datastore

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.bookingcourt.core.utils.Constants
import com.example.bookingcourt.domain.model.User
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val gson: Gson,
) {
    private val accessTokenKey = stringPreferencesKey(Constants.PrefsKeys.ACCESS_TOKEN)
    private val refreshTokenKey = stringPreferencesKey(Constants.PrefsKeys.REFRESH_TOKEN)
    private val userIdKey = stringPreferencesKey(Constants.PrefsKeys.USER_ID)
    private val userJsonKey = stringPreferencesKey("user_json")
    private val isLoggedInKey = booleanPreferencesKey(Constants.PrefsKeys.IS_LOGGED_IN)
    private val isOnboardingCompleteKey = booleanPreferencesKey(Constants.PrefsKeys.IS_ONBOARDING_COMPLETE)
    private val themeModeKey = stringPreferencesKey(Constants.PrefsKeys.THEME_MODE)
    private val languageKey = stringPreferencesKey(Constants.PrefsKeys.LANGUAGE)

    val accessToken: Flow<String?> = dataStore.data.map { preferences ->
        val token = preferences[accessTokenKey]
        Log.d("UserPreferences", "========== GETTING TOKEN ==========")
        Log.d("UserPreferences", "Token exists: ${!token.isNullOrEmpty()}")
        Log.d("UserPreferences", "Token length: ${token?.length ?: 0}")
        Log.d("UserPreferences", "Token preview: ${token?.take(50)}...")
        Log.d("UserPreferences", "======================================")
        token
    }

    val refreshToken: Flow<String?> = dataStore.data.map { preferences ->
        preferences[refreshTokenKey]
    }

    val userId: Flow<String?> = dataStore.data.map { preferences ->
        preferences[userIdKey]
    }

    val isLoggedIn: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[isLoggedInKey] ?: false
    }

    val isOnboardingComplete: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[isOnboardingCompleteKey] ?: false
    }

    val themeMode: Flow<String> = dataStore.data.map { preferences ->
        preferences[themeModeKey] ?: "system"
    }

    val language: Flow<String> = dataStore.data.map { preferences ->
        preferences[languageKey] ?: "vi"
    }

    suspend fun saveAuthTokens(accessToken: String, refreshToken: String) {
        Log.d("UserPreferences", "========== SAVING TOKEN ==========")
        Log.d("UserPreferences", "Access Token received: ${accessToken.take(50)}...")
        Log.d("UserPreferences", "Access Token length: ${accessToken.length}")
        Log.d("UserPreferences", "Refresh Token length: ${refreshToken.length}")

        dataStore.edit { preferences ->
            preferences[accessTokenKey] = accessToken
            preferences[refreshTokenKey] = refreshToken
            preferences[isLoggedInKey] = true
        }

        Log.d("UserPreferences", "✓ Tokens saved successfully")
        Log.d("UserPreferences", "======================================")
    }

    suspend fun saveUserId(userId: String) {
        dataStore.edit { preferences ->
            preferences[userIdKey] = userId
        }
    }

    suspend fun setOnboardingComplete(complete: Boolean) {
        dataStore.edit { preferences ->
            preferences[isOnboardingCompleteKey] = complete
        }
    }

    suspend fun setThemeMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[themeModeKey] = mode
        }
    }

    suspend fun setLanguage(language: String) {
        dataStore.edit { preferences ->
            preferences[languageKey] = language
        }
    }

    suspend fun clearAuthData() {
        dataStore.edit { preferences ->
            preferences.remove(accessTokenKey)
            preferences.remove(refreshTokenKey)
            preferences.remove(userIdKey)
            preferences[isLoggedInKey] = false
        }
    }

    suspend fun clearAllData() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    suspend fun saveUser(user: User) {
        dataStore.edit { preferences ->
            preferences[userIdKey] = user.id
            preferences[userJsonKey] = gson.toJson(user)
            preferences[isLoggedInKey] = true
        }
    }

    suspend fun getUser(): User? {
        return try {
            val userJson = dataStore.data.first()[userJsonKey]
            userJson?.let { gson.fromJson(it, User::class.java) }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun clearUser() {
        dataStore.edit { preferences ->
            preferences.remove(userJsonKey)
            preferences.remove(userIdKey)
            preferences[isLoggedInKey] = false
        }
    }
}
