package com.vroomvroom.android.view.ui.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.ItemProductSectionBinding
import com.vroomvroom.android.data.model.merchant.ProductSections
import com.vroomvroom.android.utils.OnProductClickListener

class ProductSectionDiffUtil: DiffUtil.ItemCallback<ProductSections>() {
    override fun areItemsTheSame(
        oldItem: ProductSections,
        newItem: ProductSections
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: ProductSections,
        newItem: ProductSections
    ): Boolean {
        return oldItem == newItem
    }
}

class ProductSectionAdapter(
    private val listener: OnProductClickListener
): ListAdapter<ProductSections, ProductsSectionViewHolder>(ProductSectionDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductsSectionViewHolder {
        val binding: ItemProductSectionBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_product_section,
            parent,
            false
        )
        return ProductsSectionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductsSectionViewHolder, position: Int) {
        holder.binding.productSection = getItem(position)
        val productAdapter = ProductAdapter(getItem(position).products, listener)
        holder.binding.productSectionRv.adapter = productAdapter
    }
}

class ProductsSectionViewHolder(val binding: ItemProductSectionBinding): RecyclerView.ViewHolder(binding.root)
