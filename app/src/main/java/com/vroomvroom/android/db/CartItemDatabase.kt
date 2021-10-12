package com.vroomvroom.android.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [CartItem::class, CartItemChoice::class],
    version = 1,
    exportSchema = false
)
abstract class CartItemDatabase : RoomDatabase() {

    abstract fun getCartItemDao(): CartItemDAO
}