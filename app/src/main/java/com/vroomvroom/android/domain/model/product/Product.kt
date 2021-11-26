package com.vroomvroom.android.domain.model.product

import android.os.Parcelable
import com.vroomvroom.android.MerchantQuery
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class Product(
    val id: String,
    val name: String,
    val product_img_url: String?,
    val price: Int,
    val description: String?,
    val option: @RawValue List<Option?>?
) : Parcelable

data class Option(
    val name: String,
    val choice: List<MerchantQuery.Choice?>
)

