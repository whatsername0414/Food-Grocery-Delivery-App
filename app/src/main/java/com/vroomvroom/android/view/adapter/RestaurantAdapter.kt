package com.vroomvroom.android.view.adapter

import android.view.LayoutInflater
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
import com.vroomvroom.android.databinding.ItemRestaurantBinding

class RestaurantDiffUtil: DiffUtil.ItemCallback<HomeDataQuery.GetRestaurant>() {

    override fun areItemsTheSame(
        oldItem: HomeDataQuery.GetRestaurant,
        newItem: HomeDataQuery.GetRestaurant
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: HomeDataQuery.GetRestaurant,
        newItem: HomeDataQuery.GetRestaurant
    ): Boolean {
        return oldItem == newItem
    }
}

class RestaurantAdapter:
    ListAdapter<HomeDataQuery.GetRestaurant, RestaurantViewHolder>(RestaurantDiffUtil()) {

    var onRestaurantClicked: ((HomeDataQuery.GetRestaurant) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantViewHolder {
        val binding: ItemRestaurantBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_restaurant,
            parent,
            false,

        )
        return RestaurantViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RestaurantViewHolder, position: Int) {
        holder.binding.restaurant = getItem(position)

        val restaurant = getItem(position)
        holder.binding.root.setOnClickListener {
            onRestaurantClicked?.invoke(restaurant)
        }
    }

}

class RestaurantViewHolder(val binding: ItemRestaurantBinding): RecyclerView.ViewHolder(binding.root)

@BindingAdapter("restaurantImageUrl")
fun setImageUrl(imageView: ImageView, url: String) {
    imageView.load(url){crossfade(true)}
}