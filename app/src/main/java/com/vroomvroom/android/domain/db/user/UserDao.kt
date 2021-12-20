package com.vroomvroom.android.domain.db.user

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
    suspend fun insertLocation(userLocationEntity: UserLocationEntity): Long
    @Update
    suspend fun updateLocation(userLocationEntity: UserLocationEntity)
    @Query("UPDATE location_table SET current_use = :current_use WHERE id != :id")
    suspend fun updateLocations(id: Int, current_use: Boolean = false)
    @Delete
    suspend fun deleteLocation(userLocationEntity: UserLocationEntity)
    @Transaction
    @Query("SELECT * FROM location_table")
    fun getLocation(): LiveData<List<UserLocationEntity>>

}