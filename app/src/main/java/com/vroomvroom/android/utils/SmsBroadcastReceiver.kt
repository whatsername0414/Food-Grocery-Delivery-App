package com.vroomvroom.android.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import com.vroomvroom.android.view.resource.Resource
import javax.inject.Inject

class SmsBroadcastReceiver @Inject constructor(): BroadcastReceiver() {

    var smsBroadcastReceiverListener: SmsBroadcastReceiverListener? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        if (SmsRetriever.SMS_RETRIEVED_ACTION == intent?.action) {
            val extras = intent.extras
            val smsRetrieverStatus = extras?.get(SmsRetriever.EXTRA_STATUS) as Status

            when (smsRetrieverStatus.statusCode) {
                CommonStatusCodes.SUCCESS -> {
                    val messageIntent = extras.getParcelable<Intent>(SmsRetriever.EXTRA_CONSENT_INTENT)
                    messageIntent?.let {
                        smsBroadcastReceiverListener?.onIntent(Resource.Success(it))
                    }
                }
                CommonStatusCodes.TIMEOUT -> {
                    smsBroadcastReceiverListener?.onIntent(Resource.Error(Exception("Something went wrong")))
                }
            }
        }
    }
}