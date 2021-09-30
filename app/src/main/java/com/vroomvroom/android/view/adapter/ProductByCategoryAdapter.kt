package com.vroomvroom.android.view.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vroomvroom.android.MerchantQuery
import com.vroomvroom.android.R

class ProductByCategoryAdapter(
    private val product_by_category: List<MerchantQuery.Product_by_category?>?
    ): RecyclerView.Adapter<ProductByCategoryAdapter.ProductByCategoryViewHolder>() {

    class ProductByCategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productName: TextView = itemView.findViewById(R.id.product_name)
        val productImage: ImageView = itemView.findViewById(R.id.product_image)
        val productPrice: TextView = itemView.findViewById(R.id.product_price)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductByCategoryViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_product_by_category, parent, false)
        return ProductByCategoryViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ProductByCategoryViewHolder, position: Int) {
        holder.productName.text = product_by_category?.get(position)?.name
        holder.productPrice.text = "₱${product_by_category?.get(position)?.price?.toString()}"
        Glide.with(holder.productImage)
            .load(product_by_category?.get(position)?.product_img_url)
            .into(holder.productImage)
    }

    override fun getItemCount(): Int {
        return product_by_category?.size!!
    }

}