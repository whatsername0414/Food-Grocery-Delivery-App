package com.vroomvroom.android.di

import com.vroomvroom.android.repository.DataRepository
import com.vroomvroom.android.repository.DataRepositoryImpl
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
    abstract fun binRepository(repo: DataRepositoryImpl): DataRepository
}