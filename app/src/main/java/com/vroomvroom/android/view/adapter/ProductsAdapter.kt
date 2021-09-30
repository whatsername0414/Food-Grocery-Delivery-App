package com.vroomvroom.android.view.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.vroomvroom.android.MerchantQuery
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.ItemProductsBinding

class ProductsDiffUtil: DiffUtil.ItemCallback<MerchantQuery.Product>() {
    override fun areItemsTheSame(
        oldItem: MerchantQuery.Product,
        newItem: MerchantQuery.Product
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: MerchantQuery.Product,
        newItem: MerchantQuery.Product
    ): Boolean {
        return oldItem == newItem
    }
}

class ProductsAdapter: ListAdapter<MerchantQuery.Product, ProductsViewHolder>(ProductsDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductsViewHolder {
        val binding: ItemProductsBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_products,
            parent,
            false
        )
        return ProductsViewHolder(binding)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ProductsViewHolder, @SuppressLint("RecyclerView") position: Int) {
        holder.binding.products = getItem(position)
        val productByCategoryAdapter = ProductByCategoryAdapter(holder.binding.products?.product_by_category)
        holder.binding.productRv.adapter = productByCategoryAdapter
    }
}

class ProductsViewHolder(val binding: ItemProductsBinding): RecyclerView.ViewHolder(binding.root)
