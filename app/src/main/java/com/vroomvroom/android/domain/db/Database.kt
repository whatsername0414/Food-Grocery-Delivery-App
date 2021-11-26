package com.vroomvroom.android.domain.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [CartItemEntity::class, CartItemChoiceEntity::class, UserEntity::class, UserLocationEntity::class],
    version = 1,
    exportSchema = false
)
abstract class Database : RoomDatabase() {
    abstract fun cartItemDao(): CartItemDAO
    abstract fun userDao(): UserDao
}