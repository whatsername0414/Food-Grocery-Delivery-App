package com.vroomvroom.android.view.ui.orders.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.ItemOrderBinding
import com.vroomvroom.android.domain.model.order.Merchant
import com.vroomvroom.android.domain.model.order.OrderResponse

class OrdersDiffUtil: DiffUtil.ItemCallback<OrderResponse?>() {
    override fun areItemsTheSame(
        oldItem: OrderResponse,
        newItem: OrderResponse
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: OrderResponse,
        newItem: OrderResponse
    ): Boolean {
        return oldItem == newItem
    }

}

class OrderAdapter: ListAdapter<OrderResponse?, OrderViewHolder>(OrdersDiffUtil()) {

    var onMerchantClicked: ((Merchant) -> Unit)? = null
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
        order?.let { data ->
            holder.binding.order = data
            val subTotal = data.orderDetail.totalPrice + order.orderDetail.deliveryFee
            holder.binding.subtotal.text = "₱${"%.2f".format(subTotal)}"
            val orderProductAdapter = OrderProductAdapter(data.orderDetail.product)
            holder.binding.orderProductRv.adapter = orderProductAdapter

            holder.binding.orderMerchantLayout.setOnClickListener {
                onMerchantClicked?.invoke(data.merchant)
            }

            holder.binding.root.setOnClickListener {
                onOrderClicked?.invoke(data.id)
            }

            holder.binding.coverView.setOnClickListener {
                onOrderClicked?.invoke(data.id)
            }
        }
    }
}

class OrderViewHolder(val binding: ItemOrderBinding): RecyclerView.ViewHolder(binding.root)