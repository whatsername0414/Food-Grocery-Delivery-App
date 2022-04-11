package com.vroomvroom.android.view.ui.orders.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.ItemOrderProductBinding
import com.vroomvroom.android.domain.model.order.OrderProduct

class OrderProductAdapter(
    private val orderProduct: List<OrderProduct>
) : RecyclerView.Adapter<OrderProductAdapter.OrderProductViewHolder>(){

    class OrderProductViewHolder(val binding: ItemOrderProductBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderProductViewHolder {
        val binding: ItemOrderProductBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_order_product,
            parent,
            false
        )
        return OrderProductViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: OrderProductViewHolder, position: Int) {
        val orderProduct = orderProduct[position]
        holder.binding.orderProduct = orderProduct
        holder.binding.orderProductPrice.text = "₱${"%.2f".format(orderProduct.price)}"
        Glide
            .with(holder.itemView.context)
            .load(orderProduct.product_img_url)
            .placeholder(R.drawable.ic_placeholder)
            .into(holder.binding.orderProductImage)

        val optionList = StringBuilder()
        orderProduct.option?.forEach { option ->
            val type = option.option_type
            val name = option.name
            optionList.append("$type: $name •\n")
        }

        if (!orderProduct.option.isNullOrEmpty()) {
            holder.binding.orderProductOption.text = optionList
        } else holder.binding.orderProductOption.visibility = View.GONE
    }

    override fun getItemCount(): Int {
        return orderProduct.size
    }
}