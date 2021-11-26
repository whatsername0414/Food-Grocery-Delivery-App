package com.vroomvroom.android.view.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.ItemCartBinding
import com.vroomvroom.android.domain.db.CartItemEntity
import com.vroomvroom.android.domain.db.CartItemWithChoice
import com.vroomvroom.android.domain.db.MerchantEntity

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

    override fun onBindViewHolder(holder: CartViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val cartItemWithChoice = getItem(position)
        holder.binding.cartItemWithChoice = cartItemWithChoice
        val choiceList = StringBuilder()
        cartItemWithChoice?.choiceEntities?.forEach { choice ->
            choiceList.append("• ${choice.name}\n")
        }

        holder.binding.productDescription.text = choiceList

        val cartItem = getItem(position).cartItemEntity
        val merchant = MerchantEntity(
            merchant_id = cartItem.merchant.merchant_id,
            merchant_name = cartItem.merchant.merchant_name
        )
        val increaseQuantityCartItem = CartItemEntity(
            cartItemId = cartItem.cartItemId,
            remote_id = cartItem.remote_id,
            merchant = merchant,
            name = cartItem.name,
            product_img_url = cartItem.product_img_url,
            price = cartItem.price + (cartItem.price / cartItem.quantity),
            quantity = cartItem.quantity + 1,
            special_instructions = cartItem.special_instructions
        )
        holder.binding.increaseQuantity.setOnClickListener {
            onCartItemClicked?.invoke(increaseQuantityCartItem)
        }

        val decreaseQuantityCartItem = CartItemEntity(
            cartItemId = cartItem.cartItemId,
            remote_id = cartItem.remote_id,
            merchant = merchant,
            name = cartItem.name,
            product_img_url = cartItem.product_img_url,
            price = cartItem.price - (cartItem.price / cartItem.quantity),
            quantity = cartItem.quantity - 1,
            special_instructions = cartItem.special_instructions
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

@BindingAdapter("cartItemImageUrl")
fun setCartItemImageUrl(imageView: ImageView, url: String?) {
    imageView.load(url)
}