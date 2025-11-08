package com.example.smartattendance.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.smartattendance.api.AuthApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class SessionManager(private val context: Context) {
    private val USER_ID_KEY = stringPreferencesKey("user_id")
    private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
    private val USER_FULL_NAME_KEY = stringPreferencesKey("user_full_name")
    private val USER_NIM_KEY = stringPreferencesKey("user_nim")
    private val USER_ROLE_KEY = stringPreferencesKey("user_role")

    suspend fun saveUser(user: AuthApi.User) {
        context.dataStore.edit { prefs ->
            prefs[USER_ID_KEY] = user.user_id ?: ""
            prefs[USER_EMAIL_KEY] = user.email
            prefs[USER_FULL_NAME_KEY] = user.full_name ?: ""
            prefs[USER_NIM_KEY] = user.nim ?: ""
            prefs[USER_ROLE_KEY] = user.role
        }
    }

    val userFlow: Flow<AuthApi.User?> = context.dataStore.data.map { prefs ->
        val userId = prefs[USER_ID_KEY]
        val email = prefs[USER_EMAIL_KEY]
        val fullName = prefs[USER_FULL_NAME_KEY]
        val nim = prefs[USER_NIM_KEY]
        val role = prefs[USER_ROLE_KEY]

        if (userId != null && email != null) {
            AuthApi.User(
                user_id = userId,
                email = email,
                full_name = fullName,
                nim = nim,
                password_hash = "", // We don't store password hash in preferences for security
                role = role ?: "student"
            )
        } else null
    }

    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }
}
