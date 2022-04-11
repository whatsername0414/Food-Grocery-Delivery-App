package com.vroomvroom.android.repository.local

import androidx.lifecycle.LiveData
import com.vroomvroom.android.domain.db.cart.CartItemChoiceEntity
import com.vroomvroom.android.domain.db.cart.CartItemEntity
import com.vroomvroom.android.domain.db.cart.CartItemWithChoice
import com.vroomvroom.android.domain.db.user.UserEntity
import com.vroomvroom.android.domain.db.user.UserLocationEntity

interface RoomRepository {
    //User
    suspend fun insertUser(userEntity: UserEntity)
    suspend fun updateUser(userEntity: UserEntity)
    suspend fun deleteUser(userEntity: UserEntity)
    fun getUser(): LiveData<List<UserEntity>>

    //UserLocation
    suspend fun insertLocation(userLocationEntity: UserLocationEntity): Long
    suspend fun updateLocation(userLocationEntity: UserLocationEntity)
    suspend fun updateLocations(id: Int)
    suspend fun deleteLocation(userLocationEntity: UserLocationEntity)
    fun getUserLocation(): LiveData<List<UserLocationEntity>>

    //Cart
    suspend fun insertCartItem(cartItemEntity: CartItemEntity): Long
    suspend fun insertCartItemChoice(cartItemChoiceEntity: CartItemChoiceEntity)
    suspend fun updateCartItem(cartItemEntity: CartItemEntity)
    suspend fun deleteCartItem(cartItemEntity: CartItemEntity)
    suspend fun deleteCartItemChoice(cartItemChoiceEntity: CartItemChoiceEntity)
    suspend fun deleteAllCartItem()
    suspend fun deleteAllCartItemChoice()
    fun getAllCartItem(): LiveData<List<CartItemWithChoice>>
}