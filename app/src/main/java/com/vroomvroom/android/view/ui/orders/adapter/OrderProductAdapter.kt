package com.vroomvroom.android.view.ui.orders.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.ItemOrderProductBinding
import com.vroomvroom.android.data.model.order.OrderProductDto

class OrderProductAdapter(
    private val orderProduct: List<OrderProductDto>
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

    override fun onBindViewHolder(holder: OrderProductViewHolder, position: Int) {
        val orderProduct = orderProduct[position]
        holder.binding.orderProduct = orderProduct
        holder.binding.orderProductPrice.text = holder.itemView.context.getString(
            R.string.peso, "%.2f".format(orderProduct.price))
        Glide
            .with(holder.itemView.context)
            .load(orderProduct.productImgUrl)
            .placeholder(R.drawable.ic_placeholder)
            .into(holder.binding.orderProductImage)

        val optionList = StringBuilder()
        orderProduct.options?.forEach { option ->
            val type = option.optionType
            val name = option.name
            optionList.append("$type: $name •\n")
        }

        if (!orderProduct.options.isNullOrEmpty()) {
            holder.binding.orderProductOption.text = optionList
        } else holder.binding.orderProductOption.visibility = View.GONE
    }

    override fun getItemCount(): Int {
        return orderProduct.size
    }
}