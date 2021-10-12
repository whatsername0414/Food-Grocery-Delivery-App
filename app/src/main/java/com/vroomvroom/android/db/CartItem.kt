package com.vroomvroom.android.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.vroomvroom.android.view.ui.Constants.CART_ITEM_DATABASE_NAME

@Entity(tableName = CART_ITEM_DATABASE_NAME)
data class CartItem(
    @PrimaryKey(autoGenerate = true)
    val cartItemId: Int? = null,
    val remote_id: String,
    val merchant: String,
    val name: String,
    val product_img_url: String?,
    val price: Int,
)