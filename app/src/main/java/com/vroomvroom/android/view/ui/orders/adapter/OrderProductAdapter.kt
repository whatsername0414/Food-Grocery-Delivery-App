package com.vroomvroom.android.view.ui.orders.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.vroomvroom.android.OrdersByStatusQuery
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.ItemOrderProductBinding

class OrderProductAdapter(
    private val orderProduct: List<OrdersByStatusQuery.Product?>
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
        holder.binding.orderProductPrice.text = "₱${"%.2f".format(orderProduct?.price)}"

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

    override fun getItemCount(): Int {
        return orderProduct.size
    }
}

@BindingAdapter("orderProductImageUrl")
fun setOrderProductImageUrl(imageView: ImageView, url: String?) {
    imageView.load(url)
}