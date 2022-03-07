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
    @Query("DELETE FROM user_table")
    suspend fun deleteUser()
    @Transaction
    @Query("SELECT * FROM user_table")
    fun getUser(): LiveData<UserEntity>

    //UserLocation
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(userLocationEntity: UserLocationEntity)
    @Update
    suspend fun updateLocation(userLocationEntity: UserLocationEntity)
    @Query("UPDATE location_table SET currentUse = :current_use")
    suspend fun updateLocations(current_use: Boolean = false)
    @Delete
    suspend fun deleteLocation(userLocationEntity: UserLocationEntity)
    @Query("DELETE FROM location_table")
    suspend fun deleteAllAddress()
    @Transaction
    @Query("SELECT * FROM location_table")
    fun getLocation(): LiveData<List<UserLocationEntity>>

}