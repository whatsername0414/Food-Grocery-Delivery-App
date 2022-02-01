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
    @Query("UPDATE user_table SET name = :name WHERE id = :id")
    suspend fun updateUserName(id: String, name: String)
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
    @Query("UPDATE location_table SET current_use = :current_use")
    suspend fun updateLocations(current_use: Boolean = false)
    @Delete
    suspend fun deleteLocation(userLocationEntity: UserLocationEntity)
    @Transaction
    @Query("SELECT * FROM location_table")
    fun getLocation(): LiveData<List<UserLocationEntity>>

}