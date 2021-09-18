package com.vroomvroom.android.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vroomvroom.android.R

class HomeAdapter(private val context: Context, private val groupList: MutableList<String>): RecyclerView.Adapter<HomeAdapter.HomeViewHolder>() {

    val categoryAdapter by lazy { CategoryAdapter() }
    val merchantAdapter by lazy { MerchantAdapter() }
    lateinit var merchantRv: RecyclerView

    class HomeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val groupTitle: TextView = itemView.findViewById(R.id.group_title)
        val childRecyclerView: RecyclerView = itemView.findViewById(R.id.childRv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_home, parent, false)
        return HomeViewHolder(view)
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        val currentItem = groupList[position]
        holder.childRecyclerView.layoutManager = if (position == 0) LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        else LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        if (position == 0) {
            holder.groupTitle.text = currentItem
            holder.childRecyclerView.isNestedScrollingEnabled = true
        } else {
            holder.groupTitle.isVisible = false
            val params = holder.childRecyclerView.layoutParams as ConstraintLayout.LayoutParams
            params.topToTop = R.layout.item_home
        }
        if (position == 0) {
            holder.childRecyclerView.adapter = categoryAdapter
        } else {
            holder.childRecyclerView.adapter = merchantAdapter
            merchantRv = holder.childRecyclerView

        }
    }

    override fun getItemCount(): Int {
        return groupList.size
    }
}