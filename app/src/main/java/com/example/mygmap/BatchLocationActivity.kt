package com.example.mygmap

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.mygmap.databinding.ActivityBatchLocationBinding
import com.google.android.gms.location.*

class BatchLocationActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

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
                locationHelper.saveLocationResult()
                binding.tvOutput.text = locationHelper.getLocationResultText()

                Toast.makeText(
                    this@BatchLocationActivity,
                    "Location received: ${locations.size}",
                    Toast.LENGTH_SHORT
                ).show()

                Log.d(
                    TAG,
                    "BatchLocationActivity_Location is: ${locations.size}"
                )
            }
        }
        binding.locationRequestBTN.setOnClickListener {
            requestBatchLocationUpdates()
        }
        binding.startLocationRequestServiceBTN.setOnClickListener {
            val intent = Intent(this, MyLocationService::class.java)
            ContextCompat.startForegroundService(this,intent)
            Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show()
        }
        binding.stopLocationRequestServiceBTN.setOnClickListener {
            val intent = Intent(this, MyLocationService::class.java)
            stopService(intent)
            Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show()
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

    override fun onSharedPreferenceChanged(sP: SharedPreferences?, key: String?) {
        if (key == KEY_LOCATION_RESULTS) {
            binding.tvOutput.text = LocationResultHelper.getSavedLocationResult(this)
        }
    }

    override fun onStart() {
        super.onStart()
        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(this)
    }

    override fun onResume() {
        super.onResume()
        binding.tvOutput.text = LocationResultHelper.getSavedLocationResult(this)
    }

    override fun onStop() {
        super.onStop()
        PreferenceManager.getDefaultSharedPreferences(this)
            .unregisterOnSharedPreferenceChangeListener(this)
    }
    override fun onPause() {
        super.onPause()

        // if you don't want background location updates
        mLocationClient.removeLocationUpdates(mLocationCallback)
    }
}
