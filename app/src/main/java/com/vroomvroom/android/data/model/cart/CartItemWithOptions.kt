package com.vroomvroom.android.data.model.cart

import androidx.room.Embedded
import androidx.room.Relation

class CartItemWithOptions(
    @Embedded
    val cartItem: CartItemEntity,
    @Relation(parentColumn = "id", entityColumn = "productId")
    val cartItemOptions: List<CartItemOptionEntity>?
)