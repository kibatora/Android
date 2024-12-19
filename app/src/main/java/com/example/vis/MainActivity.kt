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
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import android.graphics.Color
import com.google.android.gms.maps.model.PolylineOptions
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


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

    private var previousCellInfo: CellInfo? = null


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

    }


    @SuppressLint("MissingPermission")
    private fun updateData() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            LatLon.getLocation(this) { location ->

                if (location != null) {

                    val latitude = location.latitude
                    val longitude = location.longitude
                    val latLng = LatLng(latitude, longitude)

                    var strength : Int?
                    var rsrp: Int?
                    var pci : Int?

                    tvLocation.text = "Широта: $latitude\nДолгота: $longitude"

                    val triple = Power.getSignalStrength(this@MainActivity) // Получаем triple
                    strength = triple.first
                    rsrp = triple.second
                    pci = triple.third


                    val rsrpValue = rsrp ?: 0
                    val currentCellInfo = CellInfo(pci, rsrpValue, latLng)

                    tvSignalStrength.text = "Мощность сигнала: $strength, RSRP: $rsrpValue, PCI: $pci"


                    googleMap?.let { googleMap ->
                        if (previousCellInfo != null && currentCellInfo.pci != previousCellInfo?.pci) {
                            val previousLatLng = previousCellInfo!!.location

                            googleMap.addPolyline(
                                PolylineOptions()
                                    .add(previousLatLng, latLng)
                                    .width(5f)
                                    .color(Color.BLUE)
                            )


                            currentCellInfo.marker = googleMap.addMarker(
                                MarkerOptions()
                                    .position(latLng)
                                    .title("PCI: $pci, RSRP: $rsrpValue (Handover)")
                            )
                        }
                        else if (cellInfoList.isEmpty()){
                            currentCellInfo.marker = googleMap.addMarker(
                                MarkerOptions()
                                    .position(latLng)
                                    .title("PCI: $pci, RSRP: $rsrpValue")
                            )
                        }

                        if (cameraPosition == null) {
                            cameraPosition = CameraPosition.builder().target(latLng).zoom(15f).build()
                            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition!!))
                        } else {
                            cameraPosition = CameraPosition.builder().target(latLng).zoom(googleMap.cameraPosition.zoom).build()
                            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition!!))
                        }


                        googleMap.isMyLocationEnabled = true
                    }


                    previousCellInfo = currentCellInfo
                    cellInfoList.add(currentCellInfo)

                    val handover = if (previousCellInfo != null && currentCellInfo.pci != previousCellInfo?.pci) "true" else "false"

                    val logMessage = "${getCurrentTimestamp()}, " +
                            "Широта: $latitude, " +
                            "Долгота: $longitude, " +
                            "Мощность сигнала: $strength, " +
                            "RSRP: $rsrpValue, " +
                            "PCI: $pci, " +
                            "Handover: $handover"

                    saveLogToFile(logMessage)

                }
                else {
                    tvLocation.text = "Местоположение недоступно"
                }
            }



        } else {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )


        }

    }

    private fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }



    private fun saveLogToFile(logMessage: String) {
        try {
            val file = File(getExternalFilesDir(null), "location_log.csv")
            val fos = FileOutputStream(file, true) // true для добавления в конец файла
            fos.bufferedWriter().use { it.write("$logMessage\n") }
            fos.close()
        } catch (e: IOException) {
            e.printStackTrace()

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