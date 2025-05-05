package com.example.app.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {

    companion object {
        val EMAIL_KEY = stringPreferencesKey("email")
        val PASSWORD_KEY = stringPreferencesKey("password")
        val REMEMBER_ME_KEY = booleanPreferencesKey("remember_me")
    }

    suspend fun saveCredentials(email: String, password: String) {
        context.dataStore.edit { prefs ->
            prefs[EMAIL_KEY] = email
            prefs[PASSWORD_KEY] = password
            prefs[REMEMBER_ME_KEY] = true
        }
    }

    suspend fun clearCredentials() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }

    suspend fun loadCredentials(): Triple<String?, String?, Boolean> {
        val prefs = context.dataStore.data.first()
        return Triple(
            prefs[EMAIL_KEY],
            prefs[PASSWORD_KEY],
            prefs[REMEMBER_ME_KEY] ?: false
        )
    }
}
