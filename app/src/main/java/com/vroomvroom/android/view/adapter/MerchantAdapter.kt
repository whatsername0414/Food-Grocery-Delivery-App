package com.vroomvroom.android.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.vroomvroom.android.HomeDataQuery
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.ItemMerchantBinding

class MerchantDiffUtil: DiffUtil.ItemCallback<HomeDataQuery.GetMerchant>() {

    override fun areItemsTheSame(
        oldItem: HomeDataQuery.GetMerchant,
        newItem: HomeDataQuery.GetMerchant
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: HomeDataQuery.GetMerchant,
        newItem: HomeDataQuery.GetMerchant
    ): Boolean {
        return oldItem == newItem
    }
}

class MerchantAdapter:
    ListAdapter<HomeDataQuery.GetMerchant, MerchantViewHolder>(MerchantDiffUtil()) {

    var onMerchantClicked: ((HomeDataQuery.GetMerchant) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MerchantViewHolder {
        val binding: ItemMerchantBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_merchant,
            parent,
            false,

        )
        return MerchantViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MerchantViewHolder, position: Int) {
        holder.binding.merchant = getItem(position)

        val categoryList = StringBuilder()
        holder.binding.merchant?.categories?.forEach { category ->
            categoryList.append("$category . ")
        }
        holder.binding.restaurantCategories.text = categoryList
        if (holder.binding.merchant?.isOpen == false) {
            holder.binding.closedBg.visibility = View.VISIBLE
            holder.binding.tvOpening.visibility = View.VISIBLE
            holder.binding.preorderBtn.visibility = View.VISIBLE
            holder.binding.cardView.isClickable = false
        } else {
            holder.binding.closedBg.visibility = View.GONE
            holder.binding.closedBg.visibility = View.GONE
            holder.binding.tvOpening.visibility = View.GONE
            holder.binding.preorderBtn.visibility = View.GONE
            holder.binding.cardView.isClickable = false
        }

        val merchant = getItem(position)
        holder.binding.cardView.setOnClickListener {
            onMerchantClicked?.invoke(merchant)
        }
    }

}

class MerchantViewHolder(val binding: ItemMerchantBinding): RecyclerView.ViewHolder(binding.root)

@BindingAdapter("restaurantImageUrl")
fun setImageUrl(imageView: ImageView, url: String) {
    imageView.load(url)
}