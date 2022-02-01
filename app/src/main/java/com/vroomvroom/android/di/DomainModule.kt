package com.vroomvroom.android.di

import com.vroomvroom.android.domain.model.merchant.FavoriteMerchantsMapper
import com.vroomvroom.android.domain.model.merchant.MerchantMapper
import com.vroomvroom.android.domain.model.merchant.MerchantsMapper
import com.vroomvroom.android.domain.model.order.LocationInputMapper
import com.vroomvroom.android.domain.model.order.OrderInputBuilder
import com.vroomvroom.android.domain.model.order.OrderInputMapper
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
    fun provideMerchantMapper(): MerchantMapper {
        return MerchantMapper()
    }

    @Singleton
    @Provides
    fun provideFavoriteMerchantsMapper(): FavoriteMerchantsMapper {
        return FavoriteMerchantsMapper()
    }

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
    fun provideLocationInputMapper(): LocationInputMapper {
        return LocationInputMapper()
    }

    @Singleton
    @Provides
    fun provideOrderBuilder(): OrderInputBuilder {
        return OrderInputBuilder()
    }

}