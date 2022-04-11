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
import com.vroomvroom.android.databinding.ItemCheckoutBinding
import com.vroomvroom.android.domain.db.cart.CartItemWithChoice

class CheckoutDiffUtil: DiffUtil.ItemCallback<CartItemWithChoice>() {
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

class CheckoutAdapter: ListAdapter<CartItemWithChoice, CheckoutViewHolder>(CheckoutDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckoutViewHolder {
        val binding: ItemCheckoutBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_checkout,
            parent,
            false
        )
        return CheckoutViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: CheckoutViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val cartItemWithChoice = getItem(position)
        holder.binding.cartItem = cartItemWithChoice.cartItemEntity
        Glide
            .with(holder.itemView.context)
            .load(cartItemWithChoice.cartItemEntity.productImgUrl)
            .placeholder(R.drawable.ic_placeholder)
            .into(holder.binding.productImage)
        val choiceList = StringBuilder()
        cartItemWithChoice?.choiceEntities?.forEach { choice ->
            choiceList.append("• ${choice.name}\n")
        }
        holder.binding.productDescription.text = choiceList
        holder.binding.productPrice.text = "₱${"%.2f".format(cartItemWithChoice?.cartItemEntity?.price)}"
    }
}

class CheckoutViewHolder(val binding: ItemCheckoutBinding): RecyclerView.ViewHolder(binding.root)