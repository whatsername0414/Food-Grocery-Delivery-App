package com.vroomvroom.android.view.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.vroomvroom.android.HomeDataQuery
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.ItemCategoryBinding

class CategoryDiffUtil: DiffUtil.ItemCallback<HomeDataQuery.GetCategory>() {
    override fun areItemsTheSame(
        oldItem: HomeDataQuery.GetCategory,
        newItem: HomeDataQuery.GetCategory
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: HomeDataQuery.GetCategory,
        newItem: HomeDataQuery.GetCategory
    ): Boolean {
        return oldItem == newItem
    }

}

class CategoryAdapter: ListAdapter<HomeDataQuery.GetCategory, CategoryViewHolder>(CategoryDiffUtil()) {

    var onCategoryClicked: ((HomeDataQuery.GetCategory?) -> Unit)? = null
    var categoryName: String? = null

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
    override fun onBindViewHolder(holder: CategoryViewHolder, @SuppressLint("RecyclerView") position: Int) {
        holder.binding.category = getItem(position)
        val imgBg = holder.binding.root.findViewById<ConstraintLayout>(R.id.cl_bg)
        val category = getItem(position)
        holder.binding.root.setOnClickListener {

            if (categoryName != category.name) {
                onCategoryClicked?.invoke(category)
                holder.binding.categoryCardView.setCardBackgroundColor(Color.parseColor("#a30000"))
                holder.binding.txtCatName.setTextColor(Color.parseColor("#ffffff"))
                imgBg.background = AppCompatResources.getDrawable(holder.binding.imageView.context, R.drawable.cat_img_bg_white)
                categoryName = category.name
            } else {
                holder.binding.categoryCardView.setCardBackgroundColor(Color.parseColor("#ffffff"))
                holder.binding.txtCatName.setTextColor(Color.parseColor("#000000"))
                onCategoryClicked?.invoke(null)
                categoryName = null
            }
            notifyDataSetChanged()
        }
        if (categoryName != category.name) {
            holder.binding.categoryCardView.setCardBackgroundColor(Color.parseColor("#ffffff"))
            holder.binding.txtCatName.setTextColor(Color.parseColor("#000000"))
            imgBg.background = AppCompatResources.getDrawable(holder.binding.imageView.context, R.drawable.light_gray)
        }
    }
}

class CategoryViewHolder(val binding: ItemCategoryBinding): RecyclerView.ViewHolder(binding.root)

@BindingAdapter("categoryImageUrl")
fun setImageUrl(imageView: ImageView, url: String?) {
    imageView.load(url)
}