package com.vroomvroom.android.view.ui.orders.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.vroomvroom.android.R
import com.vroomvroom.android.data.model.order.Merchant
import com.vroomvroom.android.data.model.order.OrderDto
import com.vroomvroom.android.databinding.ItemOrderBinding
import com.vroomvroom.android.utils.Utils.toUppercase

class OrdersDiffUtil: DiffUtil.ItemCallback<OrderDto>() {
    override fun areItemsTheSame(
        oldItem: OrderDto,
        newItem: OrderDto
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: OrderDto,
        newItem: OrderDto
    ): Boolean {
        return oldItem == newItem
    }

}

class OrderAdapter: ListAdapter<OrderDto, OrderViewHolder>(OrdersDiffUtil()) {

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

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = getItem(position)
        holder.binding.order = order
        val subTotal = order.orderDetail.totalPrice + order.orderDetail.deliveryFee
        holder.binding.apply {
            statusTv.text = order.status.label
                .toUppercase()
                .replace(",", " ")
            subtotal.text = holder.itemView.context.getString(
                R.string.peso, "%.2f".format(subTotal))
            val orderProductAdapter = OrderProductAdapter(order.orderDetail.products)
            orderProductRv.adapter = orderProductAdapter

            orderMerchantLayout.setOnClickListener {
                onMerchantClicked?.invoke(order.merchant)
            }

            root.setOnClickListener {
                onOrderClicked?.invoke(order.id)
            }

            coverView.setOnClickListener {
                onOrderClicked?.invoke(order.id)
            }
        }
    }
}

class OrderViewHolder(val binding: ItemOrderBinding): RecyclerView.ViewHolder(binding.root)