package com.vroomvroom.android

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.os.bundleOf
import androidx.navigation.NavDeepLinkBuilder
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.vroomvroom.android.utils.Constants.CHANNEL_ID
import com.vroomvroom.android.utils.Constants.CHANNEL_NAME
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class PushNotificationService: FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val data = message.data
        createNotificationChannel()
        createNotification(data["title"].orEmpty(), data["body"].orEmpty())
    }

    private fun createNotification(status: String, body: String) {
        val notificationId = (0..1000).random()
        setupNotification(
            status,
            body,
            notificationId
        )

    }

    private fun setupNotification(title: String, body: String, id: Int) {
        val bundle = bundleOf("status" to title)
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
        val notification = builder
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(setupPendingIntent(bundle))
            .setAutoCancel(true)
            .setSmallIcon(R.drawable.ic_logo)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        NotificationManagerCompat.from(this).notify(id, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                lightColor = Color.GREEN
                enableLights(true)
            }
            val manager =
                this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun setupPendingIntent(bundle: Bundle): PendingIntent {
        return NavDeepLinkBuilder(this)
            .setGraph(R.navigation.nav_main)
            .setDestination(R.id.ordersFragment)
            .setComponentName(MainActivity::class.java)
            .setArguments(bundle)
            .createPendingIntent()
    }

    companion object {
        const val TAG = "PushNotificationService"
    }
}