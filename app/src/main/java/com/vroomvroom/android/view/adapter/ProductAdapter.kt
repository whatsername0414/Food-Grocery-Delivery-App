package com.vroomvroom.android.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.vroomvroom.android.MerchantQuery
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.ItemProductBinding
import com.vroomvroom.android.utils.OnProductClickListener

class ProductAdapter(
    private val product: List<MerchantQuery.Product_by_category?>,
    private val listenerProduct: OnProductClickListener
    ) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>(){

    class ProductViewHolder(val binding: ItemProductBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding: ItemProductBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_product,
            parent,
            false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = product[position]
        holder.binding.product = product
        if (product?.description != null) {
            holder.binding.productDescription.text = product.description
        } else holder.binding.productDescription.visibility = View.GONE

        holder.binding.root.setOnClickListener {
            listenerProduct.onClick(product)
        }
    }

    override fun getItemCount(): Int {
        return product.size
    }
}

@BindingAdapter("productImageUrl")
fun setProductImageUrl(imageView: ImageView, url: String?) {
    imageView.load(url)
}
