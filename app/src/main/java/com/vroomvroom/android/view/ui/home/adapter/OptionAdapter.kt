package com.vroomvroom.android.view.ui.home.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.vroomvroom.android.R
import com.vroomvroom.android.data.model.merchant.Option
import com.vroomvroom.android.databinding.ItemOptionBinding
import com.vroomvroom.android.utils.OnOptionClickListener

class OptionAdapter(
    private var option: List<Option?>,
    private val listener: OnOptionClickListener
) : RecyclerView.Adapter<OptionAdapter.OptionViewHolder>(){

    class OptionViewHolder(val binding: ItemOptionBinding): RecyclerView.ViewHolder(binding.root)

    private var selectedPosition = -1
    private var optionType: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionViewHolder {
        val binding: ItemOptionBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_option,
            parent,
            false
        )
        return OptionViewHolder(binding)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: OptionViewHolder, position: Int) {
        val option = option[position]
        holder.binding.option = option
        if (option?.additionalPrice == null) {
            holder.binding.additionalPriceTv.visibility = View.GONE
        }

        holder.binding.additionalPriceTv.text = holder.itemView.context.getString(
            R.string.peso, "%.2f".format(option?.additionalPrice))

        holder.binding.checkBox.setOnClickListener {
            if (selectedPosition == position) {
                holder.binding.checkBox.isChecked = false
                selectedPosition = -1
            } else {
                option?.let { listener.onClick(option, optionType.orEmpty()) }
                selectedPosition = holder.bindingAdapterPosition
                notifyDataSetChanged()
            }
        }

        holder.binding.checkBox.isChecked = selectedPosition == position
    }

    override fun getItemCount(): Int {
        return option.size
    }

    fun setProductOptionType(type: String) {
        optionType = type
    }
}
