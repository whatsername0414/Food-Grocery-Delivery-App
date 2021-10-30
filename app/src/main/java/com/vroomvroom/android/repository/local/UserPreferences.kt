package com.vroomvroom.android.repository.local

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.vroomvroom.android.utils.Constants.PREFERENCES_STORE_NAME
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PREFERENCES_STORE_NAME)

class UserPreferences @Inject constructor (@ApplicationContext app: Context) {

    private val appContext = app.applicationContext

    val token: Flow<String?>
        get() = appContext.dataStore.data.map { preferences ->
            preferences[TOKEN]
        }

    val refreshToken: Flow<String?>
        get() = appContext.dataStore.data.map { preferences ->
            preferences[REFRESH_TOKEN]
        }

    suspend fun saveToken(accessToken: String) {
        appContext.dataStore.edit { preferences ->
            preferences[TOKEN] = accessToken
//            preferences[REFRESH_TOKEN] = refreshToken
        }
    }

    val location: Flow<String?> = appContext.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    Log.e("DataStore", exception.message.toString())
                } else throw exception
            }
            .map { preferences ->
                preferences[LOCATION]
            }

    suspend fun saveLocation(newLocation: String) {
        appContext.dataStore.edit { preferences ->
            preferences[LOCATION] = newLocation
        }
    }

    suspend fun clear() {
        appContext.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    companion object {
        private val TOKEN = stringPreferencesKey("key_token")
        private val REFRESH_TOKEN = stringPreferencesKey("key_refresh_token")
        private val LOCATION = stringPreferencesKey("key_location")
    }

}