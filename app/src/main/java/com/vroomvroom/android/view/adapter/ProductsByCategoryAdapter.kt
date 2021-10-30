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
import com.vroomvroom.android.databinding.ItemProductSectionBinding
import com.vroomvroom.android.utils.OnProductClickListener

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

class ProductsByCategoryAdapter(private val listenerProduct: OnProductClickListener): ListAdapter<MerchantQuery.Product, ProductsByCategoryViewHolder>(ProductsDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductsByCategoryViewHolder {
        val binding: ItemProductSectionBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_product_section,
            parent,
            false
        )
        return ProductsByCategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductsByCategoryViewHolder, @SuppressLint("RecyclerView") position: Int) {
        holder.binding.products = getItem(position)
        val productAdapter = ProductAdapter(getItem(position).product_by_category, listenerProduct)
        holder.binding.productSectionRv.adapter = productAdapter
    }
}

class ProductsByCategoryViewHolder(val binding: ItemProductSectionBinding): RecyclerView.ViewHolder(binding.root)
