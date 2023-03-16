package com.example.mygmap

import android.Manifest
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.mygmap.Constant.PLAY_SERVICES_ERROR_CODE
import com.example.mygmap.Constant.TAG
import com.example.mygmap.databinding.ActivityMapsBinding
import com.example.mygmap.databinding.BtmSheetDialogBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private lateinit var mainViewModel: MainViewModel

    private val requestMultiplePermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.entries.forEach {
            Log.d("test006", "${it.key} = ${it.value}")
        }
    }

    private val enableLocationLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result :ActivityResult ->
        if (result.resultCode == RESULT_OK){
            val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
            val providerEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

            if (providerEnabled){
                Toast.makeText(this, "GPS is enabled", Toast.LENGTH_SHORT).show()
            }
        } else if (result.resultCode == RESULT_CANCELED){
            Toast.makeText(this, "GPS is not enabled", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.floatingActionButton.setOnClickListener {
            bottomSheetDialog()
        }

        binding.searchIV.setOnClickListener {
            hideKeyboard(binding.root)

            val locationName = binding.searchAddressHereEdtTxt.text.toString()
            val geoCoder = Geocoder(this, Locale.getDefault())

            if (locationName.toDoubleOrNull() != null){
                mainViewModel.reverseGeocoding(this, mMap, geoCoder, locationName)
            } else {
                mainViewModel.geocoding(this, mMap,geoCoder, locationName)
            }
        }

        initGoogleMap()
    }
    private fun hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }


    private fun initGoogleMap() {
        if (isServicesOk()) {
//            if (requestGPSEnabled()) {
                if (checkLocationPermission()) {
                    Toast.makeText(this, "Ready to Map", Toast.LENGTH_SHORT).show()

                    val supportMapFragment = SupportMapFragment.newInstance()

                    supportFragmentManager.beginTransaction()
                        .add(R.id.map_fragment_container, supportMapFragment)
                        .commit()
                    supportMapFragment.getMapAsync(this)
                } else {
                    requestLocationPermission();
                }
            }
//        }
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
    private fun requestGPSEnabled(): Boolean{
        // Enable GPS
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        val providerEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        if (providerEnabled){
            return true
        } else {
            val dialog = AlertDialog.Builder(this)
                .setTitle("GPS Permission")
                .setMessage("GPS is required, to access current location")
                .setPositiveButton("Yes"){ dialogInterface, i ->
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    enableLocationLauncher.launch(intent)
                }
            dialog.show()
        }

        return false
    }
    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this@MapsActivity,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
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