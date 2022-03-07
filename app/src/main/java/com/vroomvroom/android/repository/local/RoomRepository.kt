package com.vroomvroom.android.repository.local

import androidx.lifecycle.LiveData
import com.vroomvroom.android.domain.db.cart.CartItemChoiceEntity
import com.vroomvroom.android.domain.db.cart.CartItemEntity
import com.vroomvroom.android.domain.db.cart.CartItemWithChoice
import com.vroomvroom.android.domain.db.search.SearchEntity
import com.vroomvroom.android.domain.db.user.UserEntity
import com.vroomvroom.android.domain.db.user.UserLocationEntity
import java.util.ArrayList

interface RoomRepository {
    //User
    suspend fun insertUser(userEntity: UserEntity)
    suspend fun updateUser(userEntity: UserEntity)
    suspend fun updateUserName(id: String, name: String)
    suspend fun deleteUser()
    fun getUser(): LiveData<UserEntity>

    //UserLocation
    suspend fun insertLocation(userLocationEntity: UserLocationEntity)
    suspend fun updateLocation(userLocationEntity: UserLocationEntity)
    suspend fun updateLocations()
    suspend fun deleteLocation(userLocationEntity: UserLocationEntity)
    suspend fun deleteAllAddress()
    fun getUserLocation(): LiveData<List<UserLocationEntity>>

    //Cart
    suspend fun insertCartItem(cartItemEntity: CartItemEntity): Long
    suspend fun insertCartItemChoice(cartItemChoiceEntity: CartItemChoiceEntity)
    suspend fun updateCartItem(cartItemEntity: CartItemEntity)
    suspend fun deleteCartItem(cartItemEntity: CartItemEntity)
    suspend fun deleteAllCartItem()
    suspend fun deleteAllCartItemChoice()
    fun getAllCartItem(): LiveData<List<CartItemWithChoice>>

    //Search
    suspend fun insertSearch(searchEntity: SearchEntity)
    suspend fun deleteSearch(searchEntity: SearchEntity)
    suspend fun getAllSearch(): List<SearchEntity>
}