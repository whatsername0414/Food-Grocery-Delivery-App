package com.vroomvroom.android.repository.local

import androidx.lifecycle.LiveData
import com.vroomvroom.android.data.model.cart.CartItemOptionEntity
import com.vroomvroom.android.data.db.dao.CartItemDAO
import com.vroomvroom.android.data.model.cart.CartItemEntity
import com.vroomvroom.android.data.model.search.SearchEntity
import com.vroomvroom.android.data.db.dao.SearchDao
import com.vroomvroom.android.data.db.dao.UserDao
import com.vroomvroom.android.data.model.merchant.Option
import com.vroomvroom.android.data.model.user.LocationEntity
import javax.inject.Inject

class RoomRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val searchDao: SearchDao
) : RoomRepository  {

    //UserLocation
    override suspend fun insertLocation(locationEntity: LocationEntity) = userDao.insertLocation(locationEntity)
    override suspend fun updateLocation(locationEntity: LocationEntity) = userDao.updateLocation(locationEntity)
    override suspend fun updateLocations() = userDao.updateLocations()
    override suspend fun deleteLocation(locationEntity: LocationEntity) = userDao.deleteLocation(locationEntity)
    override suspend fun deleteAllAddress() = userDao.deleteAllAddress()
    override fun getUserLocation(): LiveData<List<LocationEntity>> = userDao.getLocation()

    //Cart


    //Search
    override suspend fun insertSearch(searchEntity: SearchEntity) = searchDao.insertSearch(searchEntity)
    override suspend fun deleteSearch(searchEntity: SearchEntity) = searchDao.deleteSearch(searchEntity)
    override suspend fun getAllSearch(): List<SearchEntity> = searchDao.getAllSearch()
}