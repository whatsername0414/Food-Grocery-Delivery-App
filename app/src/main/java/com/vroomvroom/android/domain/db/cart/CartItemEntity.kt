package com.vroomvroom.android.domain.db.cart

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.vroomvroom.android.utils.Constants.CART_ITEM_TABLE

@Entity(tableName = CART_ITEM_TABLE)
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true)
    val cartItemId: Int? = null,
    @Embedded
    val cartMerchant: CartMerchantEntity,
    val productId: String,
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
