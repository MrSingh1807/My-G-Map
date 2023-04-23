package com.example.mygmap.ui

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.mygmap.ACTION_PROCESS_UPDATES
import com.example.mygmap.KEY_LOCATION_REQUEST
import com.example.mygmap.KEY_LOCATION_RESULTS
import com.example.mygmap.LocationResultHelper
import com.example.mygmap.services.MyBackgroundLocationService
import com.example.mygmap.TAG
import com.example.mygmap.databinding.ActivityBatchLocationBinding
import com.google.android.gms.location.*

class BatchLocationActivity : AppCompatActivity(),
    SharedPreferences.OnSharedPreferenceChangeListener {

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
            /*
 val intent = Intent(this, MyLocationService::class.java)
 ContextCompat.startForegroundService(this,intent)
 Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show()
 */

            requestBatchLocationUpdates()
            LocationResultHelper.setLocationRequestStatus(this, true)
        }

        binding.stopLocationRequestServiceBTN.setOnClickListener {
            /*
val intent = Intent(this, MyLocationService::class.java)
stopService(intent)
Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show()
*/

            mLocationClient.removeLocationUpdates(getPendingIntent())
            LocationResultHelper.setLocationRequestStatus(this, false)
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun getPendingIntent(): PendingIntent {
        val intent = Intent(this, MyBackgroundLocationService::class.java)
        intent.action = ACTION_PROCESS_UPDATES
        /*
        Targeting S+ (version 31 and above) requires that one of FLAG_IMMUTABLE or FLAG_MUTABLE be specified when creating a PendingIntent.
           Strongly consider using FLAG_IMMUTABLE, only use FLAG_MUTABLE if some functionality depends on the PendingIntent being mutable,
           e.g. if it needs to be used with inline replies or bubbles
         */
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent.getForegroundService(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        } else {
            PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)        }
    }

    @SuppressLint("MissingPermission")
    private fun requestBatchLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(2000)
            .setMaxUpdateDelayMillis(20 * 1000)
            .build()

        mLocationClient.requestLocationUpdates(locationRequest, getPendingIntent())
    }

    override fun onSharedPreferenceChanged(sP: SharedPreferences?, key: String?) {
        if (key == KEY_LOCATION_RESULTS) {
            binding.tvOutput.text = LocationResultHelper.getSavedLocationResult(this)
        } else if (key == KEY_LOCATION_REQUEST) {
            setButtonEnableState(LocationResultHelper.getLocationRequestStatus(this))
        }
    }

    private fun setButtonEnableState(value: Boolean) {
        if (value) {
            binding.startLocationRequestServiceBTN.isEnabled = false
            binding.stopLocationRequestServiceBTN.isEnabled = true
        } else {
            binding.startLocationRequestServiceBTN.isEnabled = true
            binding.stopLocationRequestServiceBTN.isEnabled = false
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
        setButtonEnableState(LocationResultHelper.getLocationRequestStatus(this))
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
