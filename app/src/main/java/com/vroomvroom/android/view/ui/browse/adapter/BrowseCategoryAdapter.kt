package com.vroomvroom.android.view.ui.browse.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vroomvroom.android.CategoryQuery
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.ItemBrowseCategoryBinding

class BrowseCategoryDiffUtil: DiffUtil.ItemCallback<CategoryQuery.GetCategory>() {
    override fun areItemsTheSame(
        oldItem: CategoryQuery.GetCategory,
        newItem: CategoryQuery.GetCategory
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: CategoryQuery.GetCategory,
        newItem: CategoryQuery.GetCategory
    ): Boolean {
        return oldItem == newItem
    }

}

class BrowseCategoryAdapter:
    ListAdapter<CategoryQuery.GetCategory, BrowseCategoryViewHolder>(BrowseCategoryDiffUtil()) {

    var onCategoryClicked: ((String) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrowseCategoryViewHolder {
        val binding: ItemBrowseCategoryBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_browse_category,
            parent,
            false
        )
        return BrowseCategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BrowseCategoryViewHolder, position: Int) {
        val category = getItem(position)
        holder.binding.category = category
        Glide
            .with(holder.itemView.context)
            .load(category.img_url)
            .placeholder(R.drawable.ic_placeholder)
            .into(holder.binding.categoryImg)
        holder.binding.root.setOnClickListener {
            onCategoryClicked?.invoke(category.name)
        }
    }
}

class BrowseCategoryViewHolder(val binding: ItemBrowseCategoryBinding): RecyclerView.ViewHolder(binding.root)