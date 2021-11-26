package com.vroomvroom.android.domain.db

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.vroomvroom.android.utils.Constants.CART_ITEM_TABLE

@Entity(tableName = CART_ITEM_TABLE)
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true)
    val cartItemId: Int? = null,
    @Embedded
    val merchant: MerchantEntity,
    val remote_id: String,
    val name: String,
    val product_img_url: String?,
    val price: Int,
    val quantity: Int,
    val special_instructions: String? = null
)

data class MerchantEntity(
    val merchant_id: String,
    val merchant_name: String,
)
