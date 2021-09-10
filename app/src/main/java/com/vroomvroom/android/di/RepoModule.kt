package com.vroomvroom.android.di

import com.vroomvroom.android.networking.GraphQLApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepoModule {

    @Singleton
    @Provides
    fun provideWebServices() = GraphQLApi()
}