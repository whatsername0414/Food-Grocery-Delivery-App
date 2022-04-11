package com.vroomvroom.android.view.ui.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.ItemMerchantBinding
import com.vroomvroom.android.domain.db.user.UserEntity
import com.vroomvroom.android.domain.model.merchant.MerchantData
import com.vroomvroom.android.utils.Utils.setSafeOnClickListener

class MerchantDiffUtil(
    private val oldList: List<MerchantData?>,
    private val newList: List<MerchantData?>
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

class MerchantAdapter(private var inFavorites: Boolean):
    RecyclerView.Adapter<MerchantViewHolder>() {

    private var currentUser: UserEntity? = null
    var oldList = mutableListOf<MerchantData?>()
    var onMerchantClicked: ((MerchantData) -> Unit)? = null
    var onFavoriteClicked: ((
        MerchantData,
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

    override fun onBindViewHolder(holder: MerchantViewHolder, position: Int) {
        val merchant = oldList[position]
        merchant?.let { data ->
            holder.binding.merchant = data
            if (inFavorites) {
                holder.binding.favoriteLayout.visibility = View.VISIBLE
                holder.binding.checkboxFavorite.apply {
                    this.setSafeOnClickListener {
                        setOnFavoriteClick(data, isChecked)
                    }
                }
            } else {
                if (currentUser != null) {
                    holder.binding.favoriteLayout.visibility = View.VISIBLE
                    holder.binding.checkboxFavorite.apply {
                        this.setSafeOnClickListener {
                            setOnFavoriteClick(data, isChecked)
                        }
                    }
                } else {
                    holder.binding.favoriteLayout.visibility = View.GONE
                }
            }

            val categoryList = StringBuilder()
            data.categories.forEach { category ->
                categoryList.append("$category . ")
            }
            holder.binding.restaurantCategories.text = categoryList
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

    fun setData(newList: MutableList<MerchantData?>) {
        val diffUtil = MerchantDiffUtil(oldList, newList)
        val diffResult = DiffUtil.calculateDiff(diffUtil)
        oldList = newList
        diffResult.dispatchUpdatesTo(this)
    }

    private fun setOnFavoriteClick(data: MerchantData, isChecked: Boolean) {
            if (!isChecked) {
                onFavoriteClicked?.invoke(data, 0)
            } else {
                onFavoriteClicked?.invoke(data, 1)
            }
    }
}

class MerchantViewHolder(val binding: ItemMerchantBinding): RecyclerView.ViewHolder(binding.root)

@BindingAdapter("merchantImageUrl")
fun setMerchantImageUrl(imageView: ImageView, url: String) {
    imageView.load(url)
}