package com.vroomvroom.android.view.ui.home.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.ItemChoiceBinding
import com.vroomvroom.android.domain.model.merchant.Choice

class ChoiceAdapter(private var choice: List<Choice?>) : RecyclerView.Adapter<ChoiceAdapter.ChoiceViewHolder>(){

    class ChoiceViewHolder(val binding: ItemChoiceBinding): RecyclerView.ViewHolder(binding.root)

    private var selectedPosition = -1
    var optionType: String? = null
    var onChoiceClicked: ((Choice) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChoiceViewHolder {
        val binding: ItemChoiceBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_choice,
            parent,
            false
        )
        return ChoiceViewHolder(binding)
    }

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    override fun onBindViewHolder(holder: ChoiceViewHolder, position: Int) {
        val choice = choice[position]
        holder.binding.choice = choice
        if (choice?.additionalPrice == null) {
            holder.binding.additionalPriceTv.visibility = View.GONE
        }

        holder.binding.additionalPriceTv.text = "₱${"%.2f".format(choice?.additionalPrice)}"

        holder.binding.checkBox.setOnClickListener {
            if (selectedPosition == position) {
                holder.binding.checkBox.isChecked = false
                selectedPosition = -1
            } else {
                choice?.let { onChoiceClicked?.invoke(it) }
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
