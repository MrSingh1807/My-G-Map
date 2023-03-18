package com.example.mygmap

import android.app.Activity
import android.location.Geocoder
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.mygmap.Constant.TAG
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions

class MainViewModel : ViewModel() {

    fun geocoding(
        context: Activity,
        mMap: GoogleMap,
        geocoder: Geocoder,
        locationName: String
    ) {
        try {
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU)
                geocoder.getFromLocationName(locationName, 2) { addressList ->
                    if (addressList.size > 0) {
                        val address = addressList[0]

                        gotoLocation(mMap, context, address.latitude, address.longitude)
                        mMap.addMarker(
                            MarkerOptions().position(
                                LatLng(
                                    address.latitude,
                                    address.longitude
                                )
                            )
                        )

                        Log.d(TAG, "Locality: ${address.locality}")
                    }
                    addressList.forEach { address ->
                        Log.d(
                            TAG,
                            "GeoLocate Address: ${address.getAddressLine(address.maxAddressLineIndex)}"
                        )
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun reverseGeocoding(
        context: Activity,
        mMap: GoogleMap,
        geocoder: Geocoder,
        locationName: String
    ) {

        val (latitude, longitude) = locationName.split(", ").map { it.toDouble() }.toDoubleArray()
        try {
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU)
                geocoder.getFromLocation(latitude, longitude, 3) { addressList ->
                    if (addressList.size > 0) {
                        val address = addressList[0]

                        gotoLocation(mMap, context, address.latitude, address.longitude)
                        mMap.addMarker(
                            MarkerOptions().position(
                                LatLng(
                                    address.latitude,
                                    address.longitude
                                )
                            )
                        )
                        Log.d(Constant.TAG, "Locality: ${address.locality}")
                    }
                    addressList.forEach { address ->
                        Log.d(
                            Constant.TAG,
                            "GeoLocate Address: ${address.getAddressLine(address.maxAddressLineIndex)}"
                        )
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun gotoLocation(mMap: GoogleMap, context: Activity, lat: Double, lng: Double) {

        val latLng = LatLng(lat, lng)
//        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 5f)

        context.runOnUiThread {
            mMap.addPolyline(PolylineOptions().add(latLng))
            mMap.addMarker(MarkerOptions().position(latLng).title("Marker in Delhi"))
            mMap.animateCamera(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.Builder().target(latLng).zoom(10f).build()
                )
            )
            mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        }

    }
}