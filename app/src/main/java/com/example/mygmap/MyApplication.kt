package com.example.mygmap

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.mygmap.Constant.CHANNEL_ID

class MyApplication : Application() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()

        val notificationChannel =
            NotificationChannel(CHANNEL_ID, "LocationChanel", NotificationManager.IMPORTANCE_HIGH)

        val nManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nManager.createNotificationChannel(notificationChannel)
    }
}