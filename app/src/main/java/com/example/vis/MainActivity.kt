package com.example.vis

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MainActivity : Activity(), OnMapReadyCallback {

    private lateinit var tvLocation: TextView
    private lateinit var tvSignalStrength: TextView
    private var mapFragment: MapFragment? = null
    private var googleMap: GoogleMap? = null
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val cellInfoList = mutableListOf<CellInfo>()

    private var cameraPosition: CameraPosition? = null
    private val updateRunnable = object : Runnable {
        override fun run() {
            updateData()
            handler.postDelayed(this, 30000)
        }
    }

    private companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val linearLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            setPadding(16, 16, 16, 16)

            tvLocation = TextView(this@MainActivity).apply {
                textSize = 18f
            }
            addView(tvLocation)

            tvSignalStrength = TextView(this@MainActivity).apply {
                textSize = 18f
            }
            addView(tvSignalStrength)

            val frameLayout = FrameLayout(this@MainActivity).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0
                ).apply {
                    weight = 1f
                }
                id = View.generateViewId()
            }
            addView(frameLayout)


            mapFragment = MapFragment.newInstance()
            fragmentManager.beginTransaction()
                .replace(frameLayout.id, mapFragment!!)
                .commit()

            mapFragment?.getMapAsync(this@MainActivity)

            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this@MainActivity)

        }


        setContentView(linearLayout)
        LatLon.initialize(this)
        handler.post(updateRunnable)


    }



    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        updateData()

    }


    @SuppressLint("MissingPermission")
    private fun updateData() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            LatLon.getLocation(this) { location ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    val latLng = LatLng(latitude, longitude)

                    tvLocation.text = "Широта: $latitude\nДолгота: $longitude"

                    Power.getSignalStrength(this@MainActivity) { strength, rsrp, pci ->
                        val rsrpValue = rsrp ?: 0

                        val currentCellInfo = CellInfo(pci, rsrpValue, latLng)
                        cellInfoList.add(currentCellInfo)
                        tvSignalStrength.text = "Мощность сигнала: $strength, RSRP: $rsrpValue, PCI: $pci"

                        googleMap?.let { googleMap ->
                            currentCellInfo.marker = googleMap.addMarker(
                                MarkerOptions()
                                    .position(latLng)
                                    .title("PCI: $pci, RSRP: $rsrpValue")
                            )

                            if (cameraPosition == null) {
                                cameraPosition = CameraPosition.builder().target(latLng).zoom(15f).build()
                                googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition!!))
                            } else {
                                cameraPosition = CameraPosition.builder().target(latLng).zoom(googleMap.cameraPosition.zoom).build()
                                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition!!))
                            }

                            googleMap.isMyLocationEnabled = true


                        }
                    }
                }
                else
                {
                    tvLocation.text = "Местоположение недоступно"
                }
            }

        }
        else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )

        }


    }


    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(updateRunnable)

    }


    override fun onDestroy() {
        super.onDestroy()
        LatLon.stopLocationUpdates(this)
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                updateData()
            } else {
                tvLocation.text = "Нет разрешения на доступ к локации"
            }
        }
    }
}