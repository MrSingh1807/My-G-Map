package com.example.mygmap.services

import android.app.IntentService
import android.app.Notification
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.mygmap.ACTION_PROCESS_UPDATES
import com.example.mygmap.CHANNEL_ID
import com.example.mygmap.LocationResultHelper
import com.example.mygmap.R
import com.example.mygmap.TAG
import com.google.android.gms.location.LocationResult

class MyBackgroundLocationService : IntentService("MyBackgroundLocationService"){

    @RequiresApi(Build.VERSION_CODES.O)
    @Deprecated("Deprecated in Java")
    override fun onHandleIntent(intent: Intent?) {
        Log.d(TAG, "On Handel Called: ")
        startForeground(102, getNotification())
        if (intent != null) {
            if (ACTION_PROCESS_UPDATES == intent.action) {
                val locationResult = LocationResult.extractResult(intent)
                if (locationResult != null) {
                    val locations = locationResult.locations
                    val locationHelper = LocationResultHelper(applicationContext, locations)
                    locationHelper.saveLocationResult()
                    locationHelper.showNotification()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getNotification(): Notification? {

        val notificationBuilder = Notification.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle("Location Notification")
            .setContentText("Location Service is running in the background")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setAutoCancel(true)
        return notificationBuilder.build()
    }
    @Deprecated("Deprecated in Java")
    override fun onDestroy() {
        super.onDestroy()
        stopForeground(STOP_FOREGROUND_DETACH)
    }
}