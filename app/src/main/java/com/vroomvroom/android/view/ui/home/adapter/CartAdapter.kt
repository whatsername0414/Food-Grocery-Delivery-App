package com.vroomvroom.android.view.ui.home.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.ItemCartBinding
import com.vroomvroom.android.domain.db.cart.CartItemEntity
import com.vroomvroom.android.domain.db.cart.CartItemWithChoice
import com.vroomvroom.android.domain.db.cart.CartMerchantEntity

class CartDiffUtil: DiffUtil.ItemCallback<CartItemWithChoice>() {
    override fun areItemsTheSame(
        oldItem: CartItemWithChoice,
        newItem: CartItemWithChoice
    ): Boolean {
        return oldItem.cartItemEntity.cartItemId == newItem.cartItemEntity.cartItemId
    }

    override fun areContentsTheSame(
        oldItem: CartItemWithChoice,
        newItem: CartItemWithChoice
    ): Boolean {
        return oldItem.cartItemEntity == newItem.cartItemEntity
    }

}

class CartAdapter: ListAdapter<CartItemWithChoice, CartViewHolder>(CartDiffUtil()) {

    var onCartItemClicked: ((CartItemEntity) -> Unit)? = null
    var onDeleteCartItemClick: ((CartItemWithChoice) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding: ItemCartBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_cart,
            parent,
            false
        )
        return CartViewHolder(binding)
    }

    @SuppressLint("RecyclerView, SetTextI18n")
    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val cartItemWithChoice = getItem(position)
        holder.binding.cartItemWithChoice = cartItemWithChoice
        Glide
            .with(holder.itemView.context)
            .load(cartItemWithChoice.cartItemEntity.productImgUrl)
            .placeholder(R.drawable.ic_placeholder)
            .into(holder.binding.productImage)
        val choiceList = StringBuilder()
        cartItemWithChoice?.choiceEntities?.forEach { choice ->
            choiceList.append("${choice.optionType}: ${choice.name} •\n")
        }

        holder.binding.productDescription.text = choiceList
        holder.binding.productPrice.text = "₱${"%.2f".format(cartItemWithChoice.cartItemEntity.price)}"

        val cartItem = getItem(position).cartItemEntity
        val merchant = CartMerchantEntity(
            merchantId = cartItem.cartMerchant.merchantId,
            merchantName = cartItem.cartMerchant.merchantName
        )
        val increaseQuantityCartItem = CartItemEntity(
            cartItemId = cartItem.cartItemId,
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
            cartItemId = cartItem.cartItemId,
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