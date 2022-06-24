package com.vroomvroom.android.di

import android.content.Context
import android.location.Geocoder
import androidx.core.app.NotificationCompat
import com.vroomvroom.android.PushNotificationService
import com.vroomvroom.android.utils.NotificationManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
@ExperimentalCoroutinesApi
object UtilModule {


    @Singleton
    @Provides
    fun provideNotificationBuilder(
        @ApplicationContext app: Context,
        builder: NotificationCompat.Builder
    ): NotificationManager {
        return NotificationManager(app, builder)
    }

    @Singleton
    @Provides
    fun providePushNotificationService(): PushNotificationService {
        return PushNotificationService()
    }

    @Singleton
    @Provides
    fun provideGeocoder(@ApplicationContext app: Context): Geocoder {
        return Geocoder(app)
    }

}