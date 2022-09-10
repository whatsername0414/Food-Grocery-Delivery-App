package com.vroomvroom.android.data.model.cart

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.vroomvroom.android.utils.Constants.CART_ITEM_TABLE

@Entity(tableName = CART_ITEM_TABLE)
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val productId: String,
    @Embedded
    val cartMerchant: CartMerchantEntity,
    val name: String,
    val productImgUrl: String?,
    val price: Double,
    val quantity: Int,
    val specialInstructions: String? = null
)

data class CartMerchantEntity(
    val merchantId: String,
    val merchantName: String,
)
