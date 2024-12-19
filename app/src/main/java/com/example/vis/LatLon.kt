package com.example.vis

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*

object LatLon {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    @SuppressLint("MissingPermission")
    fun getLocation(context: Context, callback: (Location?) -> Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)


            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    callback(location)

                }


        } else {
            callback(null)
        }

    }

    fun stopLocationUpdates(context: Context) {
        // fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    fun initialize(context: Context){}
}