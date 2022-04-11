package com.vroomvroom.android.di

import android.content.BroadcastReceiver
import com.vroomvroom.android.repository.local.RoomRepository
import com.vroomvroom.android.repository.local.RoomRepositoryImpl
import com.vroomvroom.android.repository.remote.GraphQLRepository
import com.vroomvroom.android.repository.remote.GraphQLRepositoryImpl
import com.vroomvroom.android.repository.services.FirebaseAuthRepository
import com.vroomvroom.android.repository.services.FirebaseAuthRepositoryImpl
import com.vroomvroom.android.repository.services.LocationRepository
import com.vroomvroom.android.repository.services.LocationRepositoryImpl
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
    abstract fun binRepository(repo: GraphQLRepositoryImpl): GraphQLRepository

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