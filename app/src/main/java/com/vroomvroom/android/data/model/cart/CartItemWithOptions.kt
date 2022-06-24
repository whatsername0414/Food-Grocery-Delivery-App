package com.vroomvroom.android.data.model.cart

import androidx.room.Embedded
import androidx.room.Relation

class CartItemWithOptions(
    @Embedded
    val cartItem: CartItemEntity,
    @Relation(parentColumn = "productId", entityColumn = "productId")
    val cartItemOptions: List<CartItemOptionEntity>?
)