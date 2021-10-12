package com.vroomvroom.android.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface CartItemDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(cartItem: CartItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChoice(cartItemChoice: CartItemChoice)

    @Delete
    suspend fun deleteCartItem(cartItem: CartItem)

    @Transaction
    @Query("SELECT * FROM cart_item_table")
    fun getAllCartItem(): LiveData<List<CartItemWithChoice>>
}