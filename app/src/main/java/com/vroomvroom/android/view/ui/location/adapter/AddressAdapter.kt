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
import com.vroomvroom.android.domain.db.user.UserLocationEntity

class AddressDiffUtil: DiffUtil.ItemCallback<UserLocationEntity>() {
    override fun areItemsTheSame(
        oldItem: UserLocationEntity,
        newItem: UserLocationEntity
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: UserLocationEntity,
        newItem: UserLocationEntity
    ): Boolean {
        return oldItem == newItem
    }
}

class AddressAdapter: ListAdapter<UserLocationEntity, AddressViewHolder>(AddressDiffUtil()) {

    var currentUseAddress: ((UserLocationEntity) -> Unit)? = null
    var onAddressClicked: ((UserLocationEntity) -> Unit)? = null
    var onDeleteClicked: ((UserLocationEntity) -> Unit)? = null

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
