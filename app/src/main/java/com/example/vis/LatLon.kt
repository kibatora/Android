package com.example.vis

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*

object LatLon {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocation: Location? = null
    private lateinit var locationCallback: LocationCallback
    private var locationUpdateCallback: ((Location?) -> Unit)? = null




    fun initialize(context: Context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                for (location in locationResult.locations){
                    currentLocation = location
                }
                locationUpdateCallback?.invoke(currentLocation)

            }
        }
    }




    @SuppressLint("MissingPermission")
    fun getLocation(context: Context, callback: (Location?) -> Unit) {
        locationUpdateCallback = callback

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val locationRequest = LocationRequest.create().apply {
                interval = 30000
                fastestInterval = 10000
                priority = Priority.PRIORITY_HIGH_ACCURACY
            }


            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())


            fusedLocationClient.lastLocation
                .addOnSuccessListener { location : Location? ->
                    if (location != null) {
                        callback(location)
                        currentLocation = location
                    }

                }


        }
    }

    fun stopLocationUpdates(context: Context) {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

}