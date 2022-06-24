package com.vroomvroom.android

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.vroomvroom.android.utils.NotificationManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class PushNotificationService: FirebaseMessagingService() {

    @Inject lateinit var notificationManager: NotificationManager

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val title = message.notification?.title.orEmpty()
        notificationManager.createNotificationChannel()
        notificationManager.createNotification(title)
    }

    companion object {
        const val TAG = "PushNotificationService"
    }
}