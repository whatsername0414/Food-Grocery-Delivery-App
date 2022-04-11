package com.vroomvroom.android.domain.db.cart

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface CartItemDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(cartItemEntity: CartItemEntity): Long
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChoice(cartItemChoiceEntity: CartItemChoiceEntity)

    @Update
    suspend fun updateCartItem(cartItemEntity: CartItemEntity)

    @Delete
    suspend fun deleteCartItem(cartItemEntity: CartItemEntity)

    @Query("DELETE FROM cart_item_table")
    suspend fun deleteAllCartItem()
    @Query("DELETE FROM cart_item_choice_table")
    suspend fun deleteAllCartItemChoice()

    @Transaction
    @Query("SELECT * FROM cart_item_table")
    fun getAllCartItem(): LiveData<List<CartItemWithChoice>>
}