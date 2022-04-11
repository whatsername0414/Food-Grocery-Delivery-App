package com.vroomvroom.android.view.ui.home.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
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
        holder.binding.root.setOnClickListener {
            if (categoryName != category.name) {
                onCategoryClicked?.invoke(category)
                holder.binding.categoryCardView.setCardBackgroundColor(Color.parseColor("#a30000"))
                holder.binding.nameTv.setTextColor(Color.parseColor("#ffffff"))
                holder.binding.imageBg.background = ContextCompat.getDrawable(holder.binding.root.context, R.drawable.white)
                categoryName = category.name
            } else {
                holder.binding.categoryCardView.setCardBackgroundColor(Color.parseColor("#ffffff"))
                holder.binding.nameTv.setTextColor(Color.parseColor("#000000"))
                onCategoryClicked?.invoke(null)
                categoryName = null
            }
            notifyDataSetChanged()
        }
        if (categoryName != category.name) {
            holder.binding.categoryCardView.setCardBackgroundColor(Color.parseColor("#ffffff"))
            holder.binding.nameTv.setTextColor(Color.parseColor("#000000"))
            holder.binding.imageBg.background = ContextCompat.getDrawable(holder.binding.root.context, R.drawable.light_gray)
        }
    }
}

class CategoryViewHolder(val binding: ItemCategoryBinding): RecyclerView.ViewHolder(binding.root)

@BindingAdapter("categoryImageUrl")
fun setImageUrl(imageView: ImageView, url: String?) {
    imageView.load(url)
}