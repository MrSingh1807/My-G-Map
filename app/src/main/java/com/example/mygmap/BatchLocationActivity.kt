package com.example.mygmap

import android.annotation.SuppressLint
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.mygmap.databinding.ActivityBatchLocationBinding
import com.example.mygmap.databinding.ActivityMapsBinding
import com.google.android.gms.location.*

class BatchLocationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBatchLocationBinding
    private lateinit var mLocationClient: FusedLocationProviderClient
    private lateinit var mLocationCallback: LocationCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBatchLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mLocationCallback = object : LocationCallback() {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onLocationResult(locationResult: LocationResult) {

                val locations = locationResult.locations
                val locationHelper = LocationResultHelper(this@BatchLocationActivity, locations)
                locationHelper.showNotification()
                binding.tvOutput.text = locationHelper.getLocationResultText()

                Toast.makeText(
                    this@BatchLocationActivity,
                    "Location received: ${locations.size}",
                    Toast.LENGTH_SHORT
                ).show()

                Log.d(
                    Constant.TAG,
                    "BatchLocationActivity_Location is: ${locations.size}"
                )
            }
        }
        binding.locationRequestBTN.setOnClickListener {
            requestBatchLocationUpdates()
        }

    }

    @SuppressLint("MissingPermission")
    private fun requestBatchLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(2000)
            .setMaxUpdateDelayMillis(20 * 1000)
            .build()

        mLocationClient.requestLocationUpdates(locationRequest, mLocationCallback, null)
    }

    override fun onPause() {
        super.onPause()

        // if you don't want background location updates
        mLocationClient.removeLocationUpdates(mLocationCallback)
    }
}
