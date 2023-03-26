package com.example.mygmap

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.annotation.RequiresApi

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel(
                    CHANNEL_ID,
                    "LocationChanel",
                    NotificationManager.IMPORTANCE_HIGH
                )

            val nManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            nManager.createNotificationChannel(notificationChannel)

        }
    }
}