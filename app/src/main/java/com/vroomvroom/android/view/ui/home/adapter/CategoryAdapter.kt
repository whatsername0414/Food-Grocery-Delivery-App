package com.vroomvroom.android.view.ui.home.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vroomvroom.android.CategoryQuery
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.ItemCategoryBinding

class CategoryDiffUtil: DiffUtil.ItemCallback<CategoryQuery.GetCategory>() {
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

class CategoryAdapter: ListAdapter<CategoryQuery.GetCategory, CategoryViewHolder>(CategoryDiffUtil()) {

    var onCategoryClicked: ((CategoryQuery.GetCategory?) -> Unit)? = null
    private var categoryName: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding: ItemCategoryBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_category,
            parent,
            false
            )
        return CategoryViewHolder(binding)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = getItem(position)
        holder.binding.category = category
        Glide
            .with(holder.itemView.context)
            .load(category.img_url)
            .placeholder(R.drawable.ic_placeholder)
            .into(holder.binding.categoryImg)

        holder.binding.root.setOnClickListener {
            if (categoryName != category.name) {
                onCategoryClicked?.invoke(category)
                holder.binding.apply {
                    categoryCardView.setCardBackgroundColor(
                        ContextCompat.getColor(it.context, R.color.red_a30))
                    nameTv.setTextColor(ContextCompat.getColor(it.context, R.color.white))
                    imageBg.background = ContextCompat.getDrawable(
                            it.context, R.drawable.bg_white_fff_rounded_100dp)
                }
                categoryName = category.name
            } else {
                onCategoryClicked?.invoke(null)
                categoryName = null
            }
            notifyDataSetChanged()
        }
        if (categoryName != category.name) {
            holder.binding.apply {
                categoryCardView.setCardBackgroundColor(
                    ContextCompat.getColor(this.root.context, R.color.gray_f2f))
                nameTv.setTextColor(ContextCompat.getColor(this.root.context, R.color.black))
                imageBg.background =
                    ContextCompat.getDrawable(this.root.context, R.drawable.bg_gray_f2f_rounded_100dp)
            }
        }
    }
}

class CategoryViewHolder(val binding: ItemCategoryBinding): RecyclerView.ViewHolder(binding.root)