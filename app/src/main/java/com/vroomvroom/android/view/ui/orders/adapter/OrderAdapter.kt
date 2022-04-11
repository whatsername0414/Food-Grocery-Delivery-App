package com.vroomvroom.android.view.ui.orders.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.vroomvroom.android.OrdersByStatusQuery
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.ItemOrderBinding

class OrdersDiffUtil: DiffUtil.ItemCallback<OrdersByStatusQuery.GetOrdersByStatus>() {
    override fun areItemsTheSame(
        oldItem: OrdersByStatusQuery.GetOrdersByStatus,
        newItem: OrdersByStatusQuery.GetOrdersByStatus
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: OrdersByStatusQuery.GetOrdersByStatus,
        newItem: OrdersByStatusQuery.GetOrdersByStatus
    ): Boolean {
        return oldItem == newItem
    }
}

class OrderAdapter: ListAdapter<OrdersByStatusQuery.GetOrdersByStatus, OrderViewHolder>(OrdersDiffUtil()) {

    var onMerchantClicked: ((OrdersByStatusQuery.Merchant) -> Unit)? = null
    var onOrderClicked: ((String) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding: ItemOrderBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_order,
            parent,
            false
        )
        return OrderViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = getItem(position)
        holder.binding.order = order
        val subTotal = order.order_detail.total_price + order.order_detail.delivery_fee
        holder.binding.subtotal.text = "₱${"%.2f".format(subTotal)}"
        val orderProductAdapter = OrderProductAdapter(order.order_detail.product)
        holder.binding.orderProductRv.adapter = orderProductAdapter

        holder.binding.orderMerchantLayout.setOnClickListener {
            onMerchantClicked?.invoke(order.merchant)
        }

        holder.binding.root.setOnClickListener {
            onOrderClicked?.invoke(order.id)
        }

        holder.binding.coverView.setOnClickListener {
            onOrderClicked?.invoke(order.id)
        }
    }
}

class OrderViewHolder(val binding: ItemOrderBinding): RecyclerView.ViewHolder(binding.root)