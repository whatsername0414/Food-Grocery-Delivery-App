package com.vroomvroom.android.repository.local

import com.vroomvroom.android.db.CartItem
import com.vroomvroom.android.db.CartItemChoice
import com.vroomvroom.android.db.CartItemDAO
import javax.inject.Inject

class RoomRepository @Inject constructor(
    private val cartItemDAO: CartItemDAO
)  {
    suspend fun insertCartItem(cartItem: CartItem): Long {
        return cartItemDAO.insertCartItem(cartItem)
    }

    suspend fun insertCartItemChoice(cartItemChoice: CartItemChoice) = cartItemDAO.insertChoice(cartItemChoice)

    suspend fun deleteCartItem(cartItem: CartItem) = cartItemDAO.deleteCartItem(cartItem)

    fun getAllCartItem() = cartItemDAO.getAllCartItem()
}