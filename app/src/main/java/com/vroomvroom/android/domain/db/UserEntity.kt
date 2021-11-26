package com.vroomvroom.android.domain.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.vroomvroom.android.utils.Constants.LOCATION_TABLE
import com.vroomvroom.android.utils.Constants.USER_TABLE

@Entity(tableName = USER_TABLE)
data class UserEntity(
    @PrimaryKey
    val id: String,
    val name: String? = null,
    val email: String,
    val phone_number: String? = null
)

@Entity(tableName = LOCATION_TABLE)
data class UserLocationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val address: String? = null,
    val city: String? = null,
    val latitude: Double,
    val longitude: Double
)