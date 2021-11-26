package com.vroomvroom.android.repository.local

import androidx.lifecycle.LiveData
import com.vroomvroom.android.domain.db.*

interface RoomRepository {
    //User
    suspend fun insertUser(userEntity: UserEntity)
    suspend fun updateUser(userEntity: UserEntity)
    suspend fun deleteUser(userEntity: UserEntity)
    fun getUser(): LiveData<List<UserEntity>>

    //UserLocation
    suspend fun insertLocation(userLocationEntity: UserLocationEntity)
    suspend fun updateLocation(userLocationEntity: UserLocationEntity)
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