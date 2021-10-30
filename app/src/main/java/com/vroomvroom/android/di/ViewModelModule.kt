package com.vroomvroom.android.di

import com.vroomvroom.android.repository.local.RoomRepository
import com.vroomvroom.android.repository.local.RoomRepositoryImpl
import com.vroomvroom.android.repository.remote.GraphQLRepository
import com.vroomvroom.android.repository.remote.GraphQLRepositoryImpl
import com.vroomvroom.android.repository.services.FirebaseAuthRepository
import com.vroomvroom.android.repository.services.FirebaseAuthRepositoryImpl
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
}