package com.vroomvroom.android.di

import com.vroomvroom.android.domain.model.merchant.MerchantsMapper
import com.vroomvroom.android.domain.model.order.*
import com.vroomvroom.android.domain.model.product.ProductMapper
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
    fun provideMerchantsMapper(): MerchantsMapper {
        return MerchantsMapper()
    }

    @Singleton
    @Provides
    fun provideProductMapper(): ProductMapper {
        return ProductMapper()
    }

    @Singleton
    @Provides
    fun provideOrderInputMapper(): OrderInputMapper {
        return OrderInputMapper()
    }

    @Singleton
    @Provides
    fun provideOrdersResponseMapper(): OrdersResponseMapper {
        return OrdersResponseMapper()
    }
    @Singleton
    @Provides
    fun provideOrderResponseMapper(): OrderResponseMapper {
        return OrderResponseMapper()
    }

    @Singleton
    @Provides
    fun provideLocationInputMapper(): LocationInputMapper {
        return LocationInputMapper()
    }

    @Singleton
    @Provides
    fun provideOrderBuilder(): OrderInputBuilder {
        return OrderInputBuilder()
    }

}