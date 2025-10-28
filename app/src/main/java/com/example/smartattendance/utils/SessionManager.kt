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
    private val USER_ID_KEY = intPreferencesKey("user_id")
    private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
    private val USER_NAME_KEY = stringPreferencesKey("user_name")

    suspend fun saveUser(user: AuthApi.User) {
        context.dataStore.edit { prefs ->
            prefs[USER_ID_KEY] = user.id ?: 0
            prefs[USER_EMAIL_KEY] = user.email
            prefs[USER_NAME_KEY] = user.name ?: ""
        }
    }

    val userFlow: Flow<AuthApi.User?> = context.dataStore.data.map { prefs ->
        val id = prefs[USER_ID_KEY]
        val email = prefs[USER_EMAIL_KEY]
        val name = prefs[USER_NAME_KEY]
        if (id != null && email != null) AuthApi.User(id = id, email = email, name = name, password = "") else null
    }

    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }
}
