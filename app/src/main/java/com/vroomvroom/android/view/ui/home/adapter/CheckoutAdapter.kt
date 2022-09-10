package com.vroomvroom.android.view.ui.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.ItemCheckoutBinding
import com.vroomvroom.android.data.model.cart.CartItemWithOptions
import com.vroomvroom.android.utils.Utils.getImageUrl

class CheckoutAdapter: ListAdapter<CartItemWithOptions, CheckoutViewHolder>(CartDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckoutViewHolder {
        val binding: ItemCheckoutBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_checkout,
            parent,
            false
        )
        return CheckoutViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CheckoutViewHolder, position: Int) {
        val cartItemWithChoice = getItem(position)
        holder.binding.cartItem = cartItemWithChoice.cartItem
        Glide
            .with(holder.itemView.context)
            .load(getImageUrl(cartItemWithChoice.cartItem.productImgUrl.orEmpty()))
            .placeholder(R.drawable.ic_placeholder)
            .into(holder.binding.productImage)
        val optionList = cartItemWithChoice.cartItemOptions?.map { "${it.optionType}: ${it.name}" }
        holder.binding.productDescription.text = optionList?.joinToString(", ")
        holder.binding.productPrice.text = holder.itemView.context.getString(
            R.string.peso, "%.2f".format(cartItemWithChoice?.cartItem?.price))
    }
}

class CheckoutViewHolder(val binding: ItemCheckoutBinding): RecyclerView.ViewHolder(binding.root)