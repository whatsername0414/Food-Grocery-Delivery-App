package com.vroomvroom.android.domain.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface UserDao {
    //User
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(userEntity: UserEntity)
    @Update
    suspend fun updateUser(userEntity: UserEntity)
    @Delete
    suspend fun deleteUser(userEntity: UserEntity)
    @Transaction
    @Query("SELECT * FROM user_table")
    fun getUser(): LiveData<List<UserEntity>>

    //UserLocation
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(userLocationEntity: UserLocationEntity)
    @Update
    suspend fun updateLocation(userLocationEntity: UserLocationEntity)
    @Transaction
    @Query("SELECT * FROM location_table")
    fun getLocation(): LiveData<List<UserLocationEntity>>

}