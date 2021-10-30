package com.vroomvroom.android.repository.local

import com.vroomvroom.android.db.CartItem
import com.vroomvroom.android.db.CartItemChoice
import com.vroomvroom.android.db.CartItemDAO
import javax.inject.Inject

class RoomRepositoryImpl @Inject constructor(
    private val cartItemDAO: CartItemDAO
) : RoomRepository  {

    override suspend fun insertCartItem(cartItem: CartItem): Long = cartItemDAO.insertCartItem(cartItem)

    override suspend fun insertCartItemChoice(cartItemChoice: CartItemChoice) = cartItemDAO.insertChoice(cartItemChoice)

    override suspend fun updateCartItem(cartItem: CartItem) = cartItemDAO.updateCartItem(cartItem)

    override suspend fun deleteCartItem(cartItem: CartItem) = cartItemDAO.deleteCartItem(cartItem)
    override suspend fun deleteCartItemChoice(cartItemChoice: CartItemChoice) = cartItemDAO.deleteCartItemChoice(cartItemChoice)

    override fun getAllCartItem() = cartItemDAO.getAllCartItem()

}