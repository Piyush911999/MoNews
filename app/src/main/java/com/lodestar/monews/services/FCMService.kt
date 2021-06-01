package com.lodestar.monews.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.lodestar.monews.R
import com.lodestar.monews.ui.MainActivity


class FCMService: FirebaseMessagingService() {
    val TAG = "FCMService"
    override fun onNewToken(p0: String) {
        Log.d(TAG, p0)
        super.onNewToken(p0)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "FCM Payload: ${remoteMessage.data}")
        val extras = Bundle()
        for ((key, value) in remoteMessage.data.entries) {
            extras.putString(key, value)
        }
        if (!extras.getString("title").isNullOrEmpty()) {
//            val title = remoteMessage.notification!!.title
//            val body = remoteMessage.notification!!.body
            val title = extras.getString("title")
            val body = extras.getString("body")
            val url = extras.getString("url")

            // Show notification
            Log.d(TAG, "Title: $title, Body: $body")
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("title", title)
            intent.putExtra("body", body)
            intent.putExtra("url", url)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            val pendingIntent =
                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
            val channelId = "MoNews"
            val builder: NotificationCompat.Builder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setDefaults(NotificationCompat.DEFAULT_SOUND)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "MoNews channel",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                manager.createNotificationChannel(channel)
            }
            manager.notify(0, builder.build())
        }
    }
}