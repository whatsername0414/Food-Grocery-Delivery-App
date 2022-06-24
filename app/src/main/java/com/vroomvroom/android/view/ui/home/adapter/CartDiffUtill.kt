package com.vroomvroom.android.view.ui.home.adapter

import androidx.recyclerview.widget.DiffUtil
import com.vroomvroom.android.data.model.cart.CartItemWithOptions

class CartDiffUtil: DiffUtil.ItemCallback<CartItemWithOptions>() {
    override fun areItemsTheSame(
        oldItem: CartItemWithOptions,
        newItem: CartItemWithOptions
    ): Boolean {
        return oldItem.cartItem.productId == newItem.cartItem.productId
    }

    override fun areContentsTheSame(
        oldItem: CartItemWithOptions,
        newItem: CartItemWithOptions
    ): Boolean {
        return oldItem.cartItem == newItem.cartItem
    }

}