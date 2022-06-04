package com.vroomvroom.android.di

import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.apollographql.apollo.ApolloClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.vroomvroom.android.domain.db.Database
import com.vroomvroom.android.repository.local.UserPreferences
import com.vroomvroom.android.utils.Constants.CART_ITEM_TABLE
import com.vroomvroom.android.utils.Constants.CHANNEL_ID
import com.vroomvroom.android.utils.Constants.PREFERENCES_STORE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepoModule {

    @Singleton
    @Provides
    fun provideFusedLocationProviderClient(
        @ApplicationContext app: Context
    ): FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(app)

    @Singleton
    @Provides
    fun providePreferences(@ApplicationContext app: Context): DataStore<Preferences> =
    PreferenceDataStoreFactory.create(
    produceFile = {
        app.preferencesDataStoreFile(PREFERENCES_STORE_NAME)
    })

    @Singleton
    @Provides
    fun provideNotificationBuilder(@ApplicationContext app: Context): NotificationCompat.Builder {
        return NotificationCompat.Builder(app, CHANNEL_ID)
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(preferences: UserPreferences): OkHttpClient {
        val interceptor = Interceptor { chain ->
            val token = runBlocking { preferences.token.first() }
            val request = chain.request().newBuilder()
            if (!token.isNullOrBlank()) {
                Log.d("Token", token)
                request.addHeader("Authorization", "Bearer $token")
            }
            chain.proceed(request.build())
        }
        return OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()
    }

    @Singleton
    @Provides
    fun provideApolloClient(okHttpClient: OkHttpClient): ApolloClient {
        return ApolloClient.builder()
            .serverUrl("http://192.168.1.7:5000/")
            .okHttpClient(okHttpClient)
            .build()
    }

    @Singleton
    @Provides
    fun provideCartItemDatabase(
        @ApplicationContext app: Context
    ) = Room.databaseBuilder(
        app,
        Database::class.java,
        CART_ITEM_TABLE
    ).build()

    @Singleton
    @Provides
    fun provideCartItemDao(db: Database) = db.cartItemDao()

    @Singleton
    @Provides
    fun provideUserDao(db: Database) = db.userDao()

    @Singleton
    @Provides
    fun provideSearchDao(db: Database) = db.searchDao()
}