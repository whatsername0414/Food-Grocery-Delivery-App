package com.vroomvroom.android.repository.cart

import androidx.lifecycle.LiveData
import com.vroomvroom.android.data.model.cart.CartItemEntity
import com.vroomvroom.android.data.model.cart.CartItemWithOptions
import com.vroomvroom.android.data.model.merchant.Option

interface CartRepository {
    suspend fun insertCartItem(cartItemEntity: CartItemEntity)
    suspend fun insertCartItemOptions(options: Map<String, Option>)
    suspend fun updateCartItem(cartItemEntity: CartItemEntity)
    suspend fun deleteCartItem(cartItemEntity: CartItemEntity)
    suspend fun deleteAllCartItem()
    suspend fun deleteAllCartItemOption()
    fun getAllCartItem(): LiveData<List<CartItemWithOptions>>
}