package com.vroomvroom.android.di

import android.content.BroadcastReceiver
import com.vroomvroom.android.repository.auth.AuthRepository
import com.vroomvroom.android.repository.auth.AuthRepositoryImpl
import com.vroomvroom.android.repository.cart.CartRepository
import com.vroomvroom.android.repository.cart.CartRepositoryImpl
import com.vroomvroom.android.repository.local.RoomRepository
import com.vroomvroom.android.repository.local.RoomRepositoryImpl
import com.vroomvroom.android.repository.merchant.MerchantRepository
import com.vroomvroom.android.repository.merchant.MerchantRepositoryImpl
import com.vroomvroom.android.repository.order.OrderRepository
import com.vroomvroom.android.repository.order.OrderRepositoryImpl
import com.vroomvroom.android.repository.services.FirebaseAuthRepository
import com.vroomvroom.android.repository.services.FirebaseAuthRepositoryImpl
import com.vroomvroom.android.repository.services.LocationRepository
import com.vroomvroom.android.repository.services.LocationRepositoryImpl
import com.vroomvroom.android.repository.user.UserRepository
import com.vroomvroom.android.repository.user.UserRepositoryImpl
import com.vroomvroom.android.utils.SmsBroadcastReceiver
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
abstract class ViewModelModule {

    @Binds
    @ViewModelScoped
    abstract fun bindMerchantRepository(repo: MerchantRepositoryImpl): MerchantRepository

    @Binds
    @ViewModelScoped
    abstract fun bindOrderRepository(repo: OrderRepositoryImpl): OrderRepository

    @Binds
    @ViewModelScoped
    abstract fun bindAuthRepository(repo: AuthRepositoryImpl): AuthRepository

    @Binds
    @ViewModelScoped
    abstract fun bindUserRepository(repo: UserRepositoryImpl): UserRepository

    @Binds
    @ViewModelScoped
    abstract fun cartRepository(repo: CartRepositoryImpl) : CartRepository

    @Binds
    @ViewModelScoped
    abstract fun roomRepository(repo: RoomRepositoryImpl) : RoomRepository

    @Binds
    @ViewModelScoped
    abstract fun firebaseAuthRepository(repo: FirebaseAuthRepositoryImpl) : FirebaseAuthRepository

    @Binds
    @ViewModelScoped
    abstract fun smsBroadcastReceiver(broadcastReceiver: SmsBroadcastReceiver) : BroadcastReceiver

    @Binds
    @ViewModelScoped
    abstract fun locationRepository(repo: LocationRepositoryImpl) : LocationRepository
}