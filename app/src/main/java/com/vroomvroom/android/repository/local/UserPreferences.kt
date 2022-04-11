package com.vroomvroom.android.repository.local

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class UserPreferences @Inject constructor (
    private val dataStore: DataStore<Preferences>
    ) {

    val token: Flow<String?>
        get() = dataStore.data.map { preferences ->
            preferences[TOKEN]
        }

    suspend fun saveToken(accessToken: String) {
        dataStore.edit { preferences ->
            preferences[TOKEN] = accessToken
        }
    }

    val location: Flow<String?> = dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    Log.e("DataStore", exception.message.toString())
                } else throw exception
            }
            .map { preferences ->
                preferences[LOCATION]
            }

    suspend fun saveLocation(newLocation: String) {
        dataStore.edit { preferences ->
            preferences[LOCATION] = newLocation
        }
    }

    suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    companion object {
        private val TOKEN = stringPreferencesKey("key_token")
        private val LOCATION = stringPreferencesKey("key_location")
    }

}