package com.vroomvroom.android.view.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.vroomvroom.android.MerchantQuery
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.ItemChoiceBinding

class ChoiceAdapter(private var choice: List<MerchantQuery.Choice?>) : RecyclerView.Adapter<ChoiceAdapter.ChoiceViewHolder>(){

    class ChoiceViewHolder(val binding: ItemChoiceBinding): RecyclerView.ViewHolder(binding.root)

    private var selectedPosition = -1
    var optionType: String? = null
    var onChoiceClicked: ((MerchantQuery.Choice) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChoiceViewHolder {
        val binding: ItemChoiceBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_choice,
            parent,
            false
        )
        return ChoiceViewHolder(binding)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ChoiceViewHolder, position: Int) {
        holder.binding.choice = choice[position]
        if (choice[position]?.additional_price == null) {
            holder.binding.additionalPriceTv.visibility = View.GONE
        }

        val choice = choice[position]
        holder.binding.checkBox.setOnClickListener {
            if (selectedPosition == position) {
                holder.binding.checkBox.isChecked = false
                selectedPosition = -1
            } else {
                onChoiceClicked?.invoke(choice!!)
                selectedPosition = holder.bindingAdapterPosition
                notifyDataSetChanged()
            }
        }

        holder.binding.checkBox.isChecked = selectedPosition == position
    }

    override fun getItemCount(): Int {
        return choice.size
    }
}
