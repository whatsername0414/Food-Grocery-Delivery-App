package com.vroomvroom.android.view.ui.location.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.ItemAddressBinding
import com.vroomvroom.android.data.model.user.LocationEntity

class AddressDiffUtil: DiffUtil.ItemCallback<LocationEntity>() {
    override fun areItemsTheSame(
        oldItem: LocationEntity,
        newItem: LocationEntity
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: LocationEntity,
        newItem: LocationEntity
    ): Boolean {
        return oldItem == newItem
    }
}

class AddressAdapter: ListAdapter<LocationEntity, AddressViewHolder>(AddressDiffUtil()) {

    var currentUseAddress: ((LocationEntity) -> Unit)? = null
    var onAddressClicked: ((LocationEntity) -> Unit)? = null
    var onDeleteClicked: ((LocationEntity) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
        val binding: ItemAddressBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_address,
            parent,
            false
        )
        return AddressViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
        val address = getItem(position)
        holder.binding.address = address

        if (address.currentUse) {
            currentUseAddress?.invoke(address)
        }

        holder.binding.root.setOnClickListener {
            onAddressClicked?.invoke(address)
        }

        if (itemCount == 1) {
            holder.binding.delete.visibility = View.GONE
        }

        holder.binding.delete.setOnClickListener {
            onDeleteClicked?.invoke(address)
        }
    }
}

class AddressViewHolder(val binding: ItemAddressBinding): RecyclerView.ViewHolder(binding.root)
