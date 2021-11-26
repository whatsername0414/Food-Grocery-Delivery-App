package com.vroomvroom.android.domain.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.vroomvroom.android.utils.Constants

@Entity(tableName = Constants.CART_ITEM_CHOICE_TABLE)
data class CartItemChoiceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val name: String,
    val additional_price: Int? = null,
    val optionType: String,
    val cartItemId: Int? = null
)