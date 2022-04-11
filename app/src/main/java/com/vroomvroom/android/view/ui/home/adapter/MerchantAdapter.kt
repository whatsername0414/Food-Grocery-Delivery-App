package com.vroomvroom.android.view.ui.home.adapter

import android.annotation.SuppressLint
import android.text.format.DateUtils
import android.text.format.DateUtils.FORMAT_SHOW_TIME
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.ItemMerchantBinding
import com.vroomvroom.android.domain.db.user.UserEntity
import com.vroomvroom.android.domain.model.merchant.Merchant
import com.vroomvroom.android.utils.Constants.ADD_TO_FAVORITES
import com.vroomvroom.android.utils.Constants.REMOVE_FROM_FAVORITES
import com.vroomvroom.android.utils.Utils.setSafeOnClickListener
import com.vroomvroom.android.utils.Utils.stringBuilder
import com.vroomvroom.android.utils.Utils.timeFormatter

class MerchantDiffUtil(
    private val oldList: List<Merchant?>,
    private val newList: List<Merchant?>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition]?._id == newList[newItemPosition]?._id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }

}

class MerchantAdapter: RecyclerView.Adapter<MerchantViewHolder>() {

    private var currentUser: UserEntity? = null
    private var oldList = mutableListOf<Merchant?>()
    var onMerchantClicked: ((Merchant) -> Unit)? = null
    var onFavoriteClicked: ((
        Merchant,
        position: Int,
        direction: Int
    ) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MerchantViewHolder {
        val binding: ItemMerchantBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_merchant,
            parent,
            false,

        )
        return MerchantViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MerchantViewHolder, position: Int) {
        val merchant = oldList[position]
        merchant?.let { data ->
            holder.binding.tvOpening.text = timeFormatter(data.opening)
            holder.binding.merchant = data
            Glide
                .with(holder.itemView.context)
                .load(data.img_url)
                .placeholder(R.drawable.ic_placeholder)
                .into(holder.binding.merchantImg)

            if (currentUser != null) {
                holder.binding.favoriteLayout.visibility = View.VISIBLE
                holder.binding.checkboxFavorite.apply {
                    this.setSafeOnClickListener {
                        setOnFavoriteClick(data, position, isChecked)
                    }
                }
            } else {
                holder.binding.favoriteLayout.visibility = View.GONE
            }

            holder.binding.restaurantCategories.text = data.categories.stringBuilder()
            if (data.isOpen) {
                holder.binding.closedBg.visibility = View.GONE
                holder.binding.tvOpening.visibility = View.GONE
                holder.binding.preorderBtn.visibility = View.GONE
                holder.binding.cardView.isClickable = true
                holder.binding.cardView.setOnClickListener {
                    onMerchantClicked?.invoke(data)
                }
            } else {
                holder.binding.closedBg.visibility = View.VISIBLE
                holder.binding.tvOpening.visibility = View.VISIBLE
                holder.binding.preorderBtn.visibility = View.VISIBLE
                holder.binding.cardView.isClickable = false
                holder.binding.preorderBtn.setOnClickListener {
                    onMerchantClicked?.invoke(data)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return oldList.size
    }

    fun setUser(user: UserEntity?) {
        currentUser = user
    }

    fun submitList(newList: MutableList<Merchant?>) {
        val diffUtil = MerchantDiffUtil(oldList, newList)
        val diffResult = DiffUtil.calculateDiff(diffUtil)
        oldList = newList
        diffResult.dispatchUpdatesTo(this)
    }

    private fun setOnFavoriteClick(data: Merchant, position: Int, isChecked: Boolean) {
            if (!isChecked) {
                onFavoriteClicked?.invoke(data, position, REMOVE_FROM_FAVORITES)
            } else {
                onFavoriteClicked?.invoke(data, position, ADD_TO_FAVORITES)
            }
    }
}

class MerchantViewHolder(val binding: ItemMerchantBinding): RecyclerView.ViewHolder(binding.root)