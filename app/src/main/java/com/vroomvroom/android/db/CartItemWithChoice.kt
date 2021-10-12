package com.vroomvroom.android.db

import androidx.room.Embedded
import androidx.room.Relation

class CartItemWithChoice(
    @Embedded
    val cartItem: CartItem,
    @Relation(
        parentColumn = "cartItemId",
        entityColumn = "cartItemId"
    )
    val choices: List<CartItemChoice>?
)