package com.example.mygmap

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*

class MyLocationService : Service() {
    lateinit var mLocationClient: FusedLocationProviderClient
    lateinit var mLocationCallback: LocationCallback

    override fun onCreate() {
        super.onCreate()
        mLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)
        mLocationCallback =  object : LocationCallback() {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onLocationResult(locationResult: LocationResult) {

                val locations = locationResult.locations

                val locationHelper = LocationResultHelper(applicationContext, locations)
                locationHelper.showNotification()
                locationHelper.saveLocationResult()

                Toast.makeText(
                    applicationContext,
                    "Location is: ${locations.size}",
                    Toast.LENGTH_SHORT
                ).show()

                Log.d(
                    TAG,
                    "Location is: ${locations.size}"
                )
            }
        }
    }
    // Using Started Service
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service: OnStartCommand: Called")
        startForeground(101, getNotification())
        getLocationUpdated()
        return START_STICKY
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

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    fun getLocationUpdated() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(2000)
            .setMaxUpdateDelayMillis(20 * 1000)
            .build()


        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            stopSelf()
            return
        }
        mLocationClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.myLooper())
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service: OnDestroyed Called")
        stopForeground(STOP_FOREGROUND_DETACH)
        mLocationClient.removeLocationUpdates(mLocationCallback)
    }
}