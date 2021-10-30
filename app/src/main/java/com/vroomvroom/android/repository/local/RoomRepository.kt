package com.vroomvroom.android.repository.local

import androidx.lifecycle.LiveData
import com.vroomvroom.android.db.CartItem
import com.vroomvroom.android.db.CartItemChoice
import com.vroomvroom.android.db.CartItemWithChoice

interface RoomRepository {
    suspend fun insertCartItem(cartItem: CartItem): Long
    suspend fun insertCartItemChoice(cartItemChoice: CartItemChoice)
    suspend fun updateCartItem(cartItem: CartItem)
    suspend fun deleteCartItem(cartItem: CartItem)
    suspend fun deleteCartItemChoice(cartItemChoice: CartItemChoice)
    fun getAllCartItem(): LiveData<List<CartItemWithChoice>>
}