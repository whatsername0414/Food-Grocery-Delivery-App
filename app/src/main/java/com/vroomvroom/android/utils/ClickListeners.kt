package com.vroomvroom.android.utils

import android.content.Intent
import com.vroomvroom.android.data.model.merchant.Option
import com.vroomvroom.android.data.model.merchant.Product
import com.vroomvroom.android.view.resource.Resource


interface OnProductClickListener {
    fun onClick(product: Product)
}

interface OnOptionClickListener {
    fun onClick(option: Option, optionType: String)
}

interface SmsBroadcastReceiverListener {
    fun onIntent(intent: Resource<Intent>)
}