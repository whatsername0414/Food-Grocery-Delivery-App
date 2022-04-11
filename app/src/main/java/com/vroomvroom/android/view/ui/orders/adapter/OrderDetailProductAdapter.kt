package com.vroomvroom.android.view.ui.orders.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.vroomvroom.android.OrderQuery
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.ItemOrderDetailProductBinding

class OrderDetailProductDiffUtil: DiffUtil.ItemCallback<OrderQuery.Product>() {
    override fun areItemsTheSame(
        oldItem: OrderQuery.Product,
        newItem: OrderQuery.Product
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: OrderQuery.Product,
        newItem: OrderQuery.Product
    ): Boolean {
        return oldItem == newItem
    }
}

class OrderDetailProductAdapter: ListAdapter<OrderQuery.Product, OrderDetailProductViewHolder>(OrderDetailProductDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderDetailProductViewHolder {
        val binding: ItemOrderDetailProductBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_order_detail_product,
            parent,
            false
        )
        return OrderDetailProductViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: OrderDetailProductViewHolder, position: Int) {
        val orderProduct = getItem(position)
        holder.binding.orderProduct = orderProduct

        val optionList = StringBuilder()
        orderProduct?.option?.forEach { option ->
            val type = option?.option_type
            val name = option?.name
            optionList.append("$type: $name, ")
        }
        if (!orderProduct?.option.isNullOrEmpty()) {
            holder.binding.orderProductOption.text = optionList
        } else holder.binding.orderProductOption.visibility = View.GONE
    }
}

class OrderDetailProductViewHolder(val binding: ItemOrderDetailProductBinding): RecyclerView.ViewHolder(binding.root)

@BindingAdapter("orderDetailProductImageUrl")
fun setOrderDetailProductImageUrl(imageView: ImageView, url: String?) {
    imageView.load(url)
}