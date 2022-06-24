package com.vroomvroom.android.view.ui.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.ItemCartBinding
import com.vroomvroom.android.data.model.cart.CartItemEntity
import com.vroomvroom.android.data.model.cart.CartItemWithOptions
import com.vroomvroom.android.data.model.cart.CartMerchantEntity

class CartAdapter: ListAdapter<CartItemWithOptions, CartViewHolder>(CartDiffUtil()) {

    var onCartItemClicked: ((CartItemEntity) -> Unit)? = null
    var onDeleteCartItemClick: ((CartItemWithOptions) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding: ItemCartBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_cart,
            parent,
            false
        )
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val cartItemWithChoice = getItem(position)
        holder.binding.cartItemWithChoice = cartItemWithChoice
        Glide
            .with(holder.itemView.context)
            .load(cartItemWithChoice.cartItem.productImgUrl)
            .placeholder(R.drawable.ic_placeholder)
            .into(holder.binding.productImage)
        val choiceList = StringBuilder()
        cartItemWithChoice?.cartItemOptions?.forEach { choice ->
            choiceList.append("${choice.optionType}: ${choice.name} •\n")
        }

        holder.binding.productDescription.text = choiceList
        holder.binding.productPrice.text = holder.itemView.context.getString(
            R.string.peso, "%.2f".format(cartItemWithChoice.cartItem.price))

        val cartItem = getItem(position).cartItem
        val merchant = CartMerchantEntity(
            merchantId = cartItem.cartMerchant.merchantId,
            merchantName = cartItem.cartMerchant.merchantName
        )
        val increaseQuantityCartItem = CartItemEntity(
            productId = cartItem.productId,
            cartMerchant = merchant,
            name = cartItem.name,
            productImgUrl = cartItem.productImgUrl,
            price = cartItem.price + (cartItem.price / cartItem.quantity),
            quantity = cartItem.quantity + 1,
            specialInstructions = cartItem.specialInstructions
        )
        holder.binding.increaseQuantity.setOnClickListener {
            onCartItemClicked?.invoke(increaseQuantityCartItem)
        }

        val decreaseQuantityCartItem = CartItemEntity(
            productId = cartItem.productId,
            cartMerchant = merchant,
            name = cartItem.name,
            productImgUrl = cartItem.productImgUrl,
            price = cartItem.price - (cartItem.price / cartItem.quantity),
            quantity = cartItem.quantity - 1,
            specialInstructions = cartItem.specialInstructions
        )
        holder.binding.decreaseQuantity.setOnClickListener {
            if (cartItem.quantity > 1) {
                onCartItemClicked?.invoke(decreaseQuantityCartItem)
            }
        }

        holder.binding.btnDelete.setOnClickListener {
            onDeleteCartItemClick?.invoke(cartItemWithChoice)
        }
    }
}

class CartViewHolder(val binding: ItemCartBinding): RecyclerView.ViewHolder(binding.root)