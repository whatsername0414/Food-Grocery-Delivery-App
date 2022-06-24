package com.vroomvroom.android.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.vroomvroom.android.data.model.cart.CartItemOptionEntity
import com.vroomvroom.android.data.db.dao.CartItemDAO
import com.vroomvroom.android.data.model.cart.CartItemEntity
import com.vroomvroom.android.data.db.dao.SearchDao
import com.vroomvroom.android.data.model.search.SearchEntity
import com.vroomvroom.android.data.db.dao.UserDao
import com.vroomvroom.android.data.model.user.UserEntity
import com.vroomvroom.android.data.model.user.LocationEntity

@Database(
    entities = [
        CartItemEntity::class,
        CartItemOptionEntity::class,
        UserEntity::class,
        LocationEntity::class,
        SearchEntity::class
               ],
    version = 1,
    exportSchema = false
)
abstract class Database : RoomDatabase() {
    abstract fun cartItemDao(): CartItemDAO
    abstract fun userDao(): UserDao
    abstract fun searchDao(): SearchDao

}