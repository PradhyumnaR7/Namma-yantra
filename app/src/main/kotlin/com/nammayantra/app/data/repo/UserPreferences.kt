package com.nammayantra.app.data.repo

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {

    companion object {
        private val KEY_USER_ID = stringPreferencesKey("user_id")
        private val KEY_USER_TYPE = stringPreferencesKey("user_type")
        private val KEY_USER_NAME = stringPreferencesKey("user_name")
        private val KEY_USER_PHONE = stringPreferencesKey("user_phone")
    }

    val userId: Flow<String> = context.dataStore.data.map { it[KEY_USER_ID] ?: "" }
    val userType: Flow<String> = context.dataStore.data.map { it[KEY_USER_TYPE] ?: "" }
    val userName: Flow<String> = context.dataStore.data.map { it[KEY_USER_NAME] ?: "" }
    val userPhone: Flow<String> = context.dataStore.data.map { it[KEY_USER_PHONE] ?: "" }

    suspend fun saveUser(uid: String, userType: String, name: String, phone: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USER_ID] = uid
            prefs[KEY_USER_TYPE] = userType
            prefs[KEY_USER_NAME] = name
            prefs[KEY_USER_PHONE] = phone
        }
    }

    suspend fun clearUser() {
        context.dataStore.edit { it.clear() }
    }
}
