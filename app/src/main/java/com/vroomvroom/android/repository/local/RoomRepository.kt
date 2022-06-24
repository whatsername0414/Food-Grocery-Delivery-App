package com.vroomvroom.android.repository.local

import androidx.lifecycle.LiveData
import com.vroomvroom.android.data.model.cart.CartItemOptionEntity
import com.vroomvroom.android.data.model.cart.CartItemEntity
import com.vroomvroom.android.data.model.cart.CartItemWithOptions
import com.vroomvroom.android.data.model.merchant.Option
import com.vroomvroom.android.data.model.search.SearchEntity
import com.vroomvroom.android.data.model.user.LocationEntity

interface RoomRepository {
    //User


    //UserLocation
    suspend fun insertLocation(locationEntity: LocationEntity)
    suspend fun updateLocation(locationEntity: LocationEntity)
    suspend fun updateLocations(): Int
    suspend fun deleteLocation(locationEntity: LocationEntity)
    suspend fun deleteAllAddress()
    fun getUserLocation(): LiveData<List<LocationEntity>>

    //Cart


    //Search
    suspend fun insertSearch(searchEntity: SearchEntity)
    suspend fun deleteSearch(searchEntity: SearchEntity)
    suspend fun getAllSearch(): List<SearchEntity>
}