package com.vroomvroom.android.repository.local

import androidx.lifecycle.LiveData
import com.vroomvroom.android.domain.db.cart.CartItemChoiceEntity
import com.vroomvroom.android.domain.db.cart.CartItemDAO
import com.vroomvroom.android.domain.db.cart.CartItemEntity
import com.vroomvroom.android.domain.db.search.SearchEntity
import com.vroomvroom.android.domain.db.search.SearchDao
import com.vroomvroom.android.domain.db.user.UserDao
import com.vroomvroom.android.domain.db.user.UserEntity
import com.vroomvroom.android.domain.db.user.UserLocationEntity
import java.util.ArrayList
import javax.inject.Inject

class RoomRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val cartItemDAO: CartItemDAO,
    private val searchDao: SearchDao
) : RoomRepository  {
    //User
    override suspend fun insertUser(userEntity: UserEntity) = userDao.insertUser(userEntity)
    override suspend fun updateUser(userEntity: UserEntity) = userDao.updateUser(userEntity)
    override suspend fun updateUserName(id: String, name: String) = userDao.updateUserName(id, name)
    override suspend fun deleteUser() = userDao.deleteUser()
    override fun getUser(): LiveData<UserEntity> = userDao.getUser()

    //UserLocation
    override suspend fun insertLocation(userLocationEntity: UserLocationEntity) = userDao.insertLocation(userLocationEntity)
    override suspend fun updateLocation(userLocationEntity: UserLocationEntity) = userDao.updateLocation(userLocationEntity)
    override suspend fun updateLocations() = userDao.updateLocations()
    override suspend fun deleteLocation(userLocationEntity: UserLocationEntity) = userDao.deleteLocation(userLocationEntity)
    override suspend fun deleteAllAddress() = userDao.deleteAllAddress()
    override fun getUserLocation(): LiveData<List<UserLocationEntity>> = userDao.getLocation()

    //Cart
    override suspend fun insertCartItem(cartItemEntity: CartItemEntity): Long = cartItemDAO.insertCartItem(cartItemEntity)
    override suspend fun insertCartItemChoice(cartItemChoiceEntity: CartItemChoiceEntity) = cartItemDAO.insertChoice(cartItemChoiceEntity)
    override suspend fun updateCartItem(cartItemEntity: CartItemEntity) = cartItemDAO.updateCartItem(cartItemEntity)
    override suspend fun deleteCartItem(cartItemEntity: CartItemEntity) = cartItemDAO.deleteCartItem(cartItemEntity)
    override suspend fun deleteAllCartItem() = cartItemDAO.deleteAllCartItem()
    override suspend fun deleteAllCartItemChoice() = cartItemDAO.deleteAllCartItemChoice()
    override fun getAllCartItem() = cartItemDAO.getAllCartItem()

    //Search
    override suspend fun insertSearch(searchEntity: SearchEntity) = searchDao.insertSearch(searchEntity)
    override suspend fun deleteSearch(searchEntity: SearchEntity) = searchDao.deleteSearch(searchEntity)
    override suspend fun getAllSearch(): List<SearchEntity> = searchDao.getAllSearch()
}