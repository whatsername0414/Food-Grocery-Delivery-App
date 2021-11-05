package com.vroomvroom.android.utils

import android.content.Intent
import com.vroomvroom.android.MerchantQuery
import com.vroomvroom.android.view.state.ViewState


interface OnProductClickListener {
    fun onClick(product: MerchantQuery.Product_by_category?)
}

interface SmsBroadcastReceiverListener {
    fun onIntent(intent: ViewState<Intent>)
}