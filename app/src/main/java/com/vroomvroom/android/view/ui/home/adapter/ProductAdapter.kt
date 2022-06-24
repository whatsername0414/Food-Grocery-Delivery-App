package com.vroomvroom.android.view.ui.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.ItemProductBinding
import com.vroomvroom.android.data.model.merchant.Product
import com.vroomvroom.android.utils.OnProductClickListener

class ProductAdapter(
    private val product: List<Product?>,
    private val listener: OnProductClickListener
    ) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>(){

    class ProductViewHolder(val binding: ItemProductBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding: ItemProductBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_product,
            parent,
            false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = product[position]
        holder.binding.product = product

        Glide
            .with(holder.itemView.context)
            .load(product?.productImgUrl)
            .placeholder(R.drawable.ic_placeholder)
            .into(holder.binding.orderProductImage)

        holder.binding.productPrice.text = holder.itemView.context.getString(
            R.string.peso, "%.2f".format(product?.price))

        holder.binding.root.setOnClickListener {
            product?.let {
                listener.onClick(it)
            }
        }
    }

    override fun getItemCount(): Int {
        return product.size
    }
}
