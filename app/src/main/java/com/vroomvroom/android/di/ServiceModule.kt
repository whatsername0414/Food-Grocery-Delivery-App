package com.vroomvroom.android.di

import com.vroomvroom.android.api.MerchantService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {

    @Singleton
    @Provides
    fun provideMerchantService(retrofit: Retrofit): MerchantService {
        return retrofit.create(MerchantService::class.java)
    }

}