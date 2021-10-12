package com.vroomvroom.android.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CartItemChoice(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val name: String,
    val additional_price: Int? = null,
    val optionType: String,
    val cartItemId: Int? = null
)