package com.vroomvroom.android.view.ui.home.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.vroomvroom.android.CategoryQuery
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.ItemBrowseCategoryBinding
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

    var onCategoryClicked: ((String?) -> Unit)? = null
    var itemViewType = 0
    private var categoryName: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val layoutBinding1: ItemCategoryBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_category,
            parent,
            false
            )
        val layoutBinding2: ItemBrowseCategoryBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_browse_category,
            parent,
            false
        )
        if (itemViewType == 1) return CategoryViewHolder(layoutBinding2)
        return CategoryViewHolder(layoutBinding1)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = getItem(position)
        when (itemViewType) {
            0 -> {
                val view = holder.binding as ItemCategoryBinding
                view.category = category
                Glide
                    .with(holder.itemView.context)
                    .load(category.img_url)
                    .placeholder(R.drawable.ic_placeholder)
                    .into(view.categoryImg)

                view.root.setOnClickListener {
                    if (categoryName != category.name) {
                        onCategoryClicked?.invoke(category.name)
                        view.apply {
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
                    view.apply {
                        categoryCardView.setCardBackgroundColor(
                            ContextCompat.getColor(this.root.context, R.color.white))
                        nameTv.setTextColor(ContextCompat.getColor(this.root.context, R.color.black))
                        imageBg.background =
                            ContextCompat.getDrawable(this.root.context, R.drawable.bg_gray_f2f_rounded_100dp)
                    }
                }
            }
            1 -> {
                val view = holder.binding as ItemBrowseCategoryBinding
                view.category = category
                Glide
                    .with(holder.itemView.context)
                    .load(category.img_url)
                    .placeholder(R.drawable.ic_placeholder)
                    .into(view.categoryImg)
                view.root.setOnClickListener {
                    onCategoryClicked?.invoke(category.name)
                }
            }
        }
    }
}

class CategoryViewHolder(val binding: ViewBinding): RecyclerView.ViewHolder(binding.root)