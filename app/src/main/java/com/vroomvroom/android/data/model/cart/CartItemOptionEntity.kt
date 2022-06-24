package com.vroomvroom.android.data.model.cart

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.vroomvroom.android.utils.Constants

@Entity(tableName = Constants.CART_ITEM_OPTION_TABLE)
data class CartItemOptionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val name: String,
    val additionalPrice: Double? = null,
    val optionType: String,
    val productId: String? = null
)