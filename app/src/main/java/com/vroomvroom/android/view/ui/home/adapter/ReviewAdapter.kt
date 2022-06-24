package com.vroomvroom.android.view.ui.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.ItemReviewBinding
import com.vroomvroom.android.data.model.merchant.Review
import com.vroomvroom.android.utils.Constants.FORMAT_DD_MMM_YYYY_HH_MM_SS
import com.vroomvroom.android.utils.Utils.parseTimeToString

class ReviewDiffUtil: DiffUtil.ItemCallback<Review>() {
    override fun areItemsTheSame(
        oldItem: Review,
        newItem: Review
    ): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(
        oldItem: Review,
        newItem: Review
    ): Boolean {
        return oldItem == newItem
    }

}

class ReviewAdapter: ListAdapter<Review, ReviewViewHolder>(ReviewDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding: ItemReviewBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_review,
            parent,
            false
        )
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = getItem(position)
        holder.binding.review = review
        val createdAt = parseTimeToString(review.createdAt, FORMAT_DD_MMM_YYYY_HH_MM_SS)
        holder.binding.tvCreatedAt.text = createdAt
    }
}

class ReviewViewHolder(val binding: ItemReviewBinding): RecyclerView.ViewHolder(binding.root)
