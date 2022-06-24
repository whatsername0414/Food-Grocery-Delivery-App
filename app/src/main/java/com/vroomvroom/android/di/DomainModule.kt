package com.vroomvroom.android.di

import com.vroomvroom.android.data.model.merchant.MerchantMapper
import com.vroomvroom.android.data.model.user.UserMapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DomainModule {

    @Singleton
    @Provides
    fun provideMerchantMapper(): MerchantMapper {
        return MerchantMapper()
    }

    @Singleton
    @Provides
    fun provideUserMapper(): UserMapper {
        return UserMapper()
    }

}