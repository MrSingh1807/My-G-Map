package com.example.mygmap

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.mygmap.Constant.PLAY_SERVICES_ERROR_CODE
import com.example.mygmap.Constant.TAG
import com.example.mygmap.databinding.ActivityMapsBinding
import com.example.mygmap.databinding.BtmSheetDialogBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.material.bottomsheet.BottomSheetDialog

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private val requestMultiplePermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.entries.forEach {
            Log.d("test006", "${it.key} = ${it.value}")
        }

    }

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        /*  Show UI Controls on Google Map
        val gMapOptions = GoogleMapOptions()
           .apply {
                zoomControlsEnabled(true)
                compassEnabled(true)
            }
         */

        val supportMapFragment = SupportMapFragment.newInstance()

        supportFragmentManager.beginTransaction()
            .add(R.id.map_fragment_container, supportMapFragment)
            .commit()
        supportMapFragment.getMapAsync(this)

        binding.floatingActionButton.setOnClickListener {
            bottomSheetDialog()
        }
        binding.go.setOnClickListener {
            /*
            Draw a rectangle that you want to show in your Screen
               Approach - 1: Take
                either   NorthEast - Latitude & Longitude
                        SouthWest - Latitude & Longitude
               or      NorthWest - Latitude & Longitude
                     SouthEast - Latitude & Longitude

                Approach - 2: when You have user location
                        val Benglore = LatLng(13.0, 77.6)  : (assume this is user live location)

                   make a rectangle :-
                        topLeftLat or topRightLat = Benglore + 0.1f
                        topLeftLong or topRightLong = Benglore + 0.1f

                        bottomLeftLat or bottomRightLat = Benglore - 0.1f
                        bottomLeftLat or bottomRightLat = Benglore - 0.1f
             */


            val southWestLatitude = 12.843895
            val southWestLongitude = 77.410526
            val northEastLatitude = 13.101506
            val northEastLongitude = 77.766895

            val BENGLURU_BOUNDS = LatLngBounds(
                LatLng(southWestLatitude, southWestLongitude),
                LatLng(northEastLatitude, northEastLongitude)
            )

            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(BENGLURU_BOUNDS,1))
            mMap.addMarker(MarkerOptions().position(BENGLURU_BOUNDS.center))
        }
        initGoogleMap()
    }

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this@MapsActivity,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun isServicesOk(): Boolean {
        val googleApi = GoogleApiAvailability.getInstance()
        val result = googleApi.isGooglePlayServicesAvailable(this)

        if (result == ConnectionResult.SUCCESS) {
            return true
        } else if (googleApi.isUserResolvableError(result)) {
            val dialog = googleApi.getErrorDialog(this, result, PLAY_SERVICES_ERROR_CODE) {
                Toast.makeText(this, "Dialog is Cancelled By User", Toast.LENGTH_SHORT).show()
            }
            dialog?.show()
        } else {
            Toast.makeText(
                this,
                "Play Services are required by this Application",
                Toast.LENGTH_SHORT
            ).show()
        }
        return false
    }

    private fun initGoogleMap() {
        if (isServicesOk()) {
            if (checkLocationPermission()) {
                Toast.makeText(this, "Ready to Map", Toast.LENGTH_SHORT).show();
            } else {
                requestLocationPermission();
            }
        }
    }

    private fun requestLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestMultiplePermissionsLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun bottomSheetDialog() {
        val dialog = BottomSheetDialog(this)
        val bindingDialog = BtmSheetDialogBinding.inflate(layoutInflater)

        bindingDialog.none.setOnClickListener { mMap.mapType = GoogleMap.MAP_TYPE_NONE }
        bindingDialog.satellite.setOnClickListener { mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE }
        bindingDialog.terrain.setOnClickListener { mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN }
        bindingDialog.hybrid.setOnClickListener { mMap.mapType = GoogleMap.MAP_TYPE_HYBRID }
        bindingDialog.normal.setOnClickListener { mMap.mapType = GoogleMap.MAP_TYPE_NORMAL }
        dialog.apply {
            setCancelable(true)
            setContentView(bindingDialog.root)
            show()
        }


    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        Log.d(TAG, "OnMapReady: Showing Map")
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val delhi = LatLng(28.7041, 77.1025)
        mMap.addMarker(MarkerOptions().position(delhi).title("Marker in Delhi"))
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(delhi, 5F)
        mMap.moveCamera(cameraUpdate)

        // Show UI Controls on GoogleMap
        mMap.uiSettings.apply {
            isZoomControlsEnabled = true
            isCompassEnabled = true
            isMapToolbarEnabled = true
            isMyLocationButtonEnabled = true
        }

    }

}