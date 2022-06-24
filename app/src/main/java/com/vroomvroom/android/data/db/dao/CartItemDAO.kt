package com.vroomvroom.android.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.vroomvroom.android.data.model.cart.CartItemOptionEntity
import com.vroomvroom.android.data.model.cart.CartItemEntity
import com.vroomvroom.android.data.model.cart.CartItemWithOptions

@Dao
interface CartItemDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(cartItemEntity: CartItemEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItemOptions(cartItemOptions: List<CartItemOptionEntity>)

    @Update
    suspend fun updateCartItem(cartItemEntity: CartItemEntity)

    @Delete
    suspend fun deleteCartItem(cartItemEntity: CartItemEntity)

    @Query("DELETE FROM cart_item_table")
    suspend fun deleteAllCartItem()
    @Query("DELETE FROM cart_item_option_table")
    suspend fun deleteAllCartItemOption()

    @Transaction
    @Query("SELECT * FROM cart_item_table")
    fun getAllCartItem(): LiveData<List<CartItemWithOptions>>
}