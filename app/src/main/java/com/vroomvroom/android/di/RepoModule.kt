package com.vroomvroom.android.di

import android.content.Context
import androidx.room.Room
import com.vroomvroom.android.db.CartItemDatabase
import com.vroomvroom.android.networking.GraphQLApi
import com.vroomvroom.android.view.ui.Constants.CART_ITEM_DATABASE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepoModule {

    @Singleton
    @Provides
    fun provideWebServices() = GraphQLApi()

    @Singleton
    @Provides
    fun provideCartItemDatabase(
        @ApplicationContext app: Context
    ) = Room.databaseBuilder(
        app,
        CartItemDatabase::class.java,
        CART_ITEM_DATABASE_NAME
    ).build()

    @Singleton
    @Provides
    fun provideCartItemDao(db: CartItemDatabase) = db.getCartItemDao()
}