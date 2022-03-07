package com.vroomvroom.android.domain.db.cart

import androidx.room.Embedded
import androidx.room.Relation

class CartItemWithChoice(
    @Embedded
    val cartItemEntity: CartItemEntity,
    @Relation(parentColumn = "cartItemId", entityColumn = "cartItemId")
    val choiceEntities: List<CartItemChoiceEntity>?
)