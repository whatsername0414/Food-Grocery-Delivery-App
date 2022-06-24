package com.vroomvroom.android.view.ui.browse.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.ItemSearchBinding
import com.vroomvroom.android.data.model.search.SearchEntity
import com.vroomvroom.android.utils.ClickType

class SearchDiffUtil: DiffUtil.ItemCallback<SearchEntity>() {
    override fun areItemsTheSame(oldItem: SearchEntity, newItem: SearchEntity): Boolean {
        return oldItem.searchTerm == newItem.searchTerm
    }
    override fun areContentsTheSame(oldItem: SearchEntity, newItem: SearchEntity): Boolean {
        return oldItem == newItem
    }
}

class SearchAdapter : ListAdapter<SearchEntity, SearchViewHolder>(SearchDiffUtil()) {

    var listener: ((ClickType, SearchEntity, Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val binding: ItemSearchBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_search,
            parent,
            false
        )
        return SearchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val search = getItem(position)
        holder.binding.search = search
        if (search.fromLocal) {
            holder.binding.apply {
                historyIcon.setImageResource(R.drawable.ic_history)
                btnPositive.setImageResource( R.drawable.ic_close)
            }
        } else {
            holder.binding.apply {
                historyIcon.setImageResource(R.drawable.ic_search)
                btnPositive.setImageResource( R.drawable.ic_arrow_up_left_gray_7f7)
            }
        }

        holder.binding.root.setOnClickListener {
            listener?.invoke(ClickType.NEGATIVE, search, position)
        }
        holder.binding.btnPositive.setOnClickListener {
            listener?.invoke(ClickType.POSITIVE, search, position)
        }
    }
}

class SearchViewHolder(val binding: ItemSearchBinding): RecyclerView.ViewHolder(binding.root)
