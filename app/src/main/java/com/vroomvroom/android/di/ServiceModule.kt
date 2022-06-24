package com.vroomvroom.android.di

import com.vroomvroom.android.data.api.AuthService
import com.vroomvroom.android.data.api.MerchantService
import com.vroomvroom.android.data.api.OrderService
import com.vroomvroom.android.data.api.UserService
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

    @Singleton
    @Provides
    fun provideOrderService(retrofit: Retrofit): OrderService {
        return retrofit.create(OrderService::class.java)
    }

    @Singleton
    @Provides
    fun provideAuthService(retrofit: Retrofit): AuthService {
        return retrofit.create(AuthService::class.java)
    }

    @Singleton
    @Provides
    fun provideUserService(retrofit: Retrofit): UserService {
        return retrofit.create(UserService::class.java)
    }

}