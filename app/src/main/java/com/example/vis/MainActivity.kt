package com.example.vis

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : Activity() {

    private lateinit var tvLocation: TextView
    private lateinit var tvSignalStrength: TextView
    private lateinit var btnUpdate: Button

    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            updateLocationData()
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
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
            }
            setPadding(16, 16, 16, 16)

            tvLocation = TextView(this@MainActivity).apply {
                textSize = 18f
            }
            addView(tvLocation)

            tvSignalStrength = TextView(this@MainActivity).apply {
                textSize = 18f
            }
            addView(tvSignalStrength)

            // Кнопка btnUpdate удалена

        }

        setContentView(linearLayout)

        LatLon.initialize(this) // Инициализация LatLon
        handler.post(updateRunnable) // Запускаем автоматическое обновление
    }



    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(updateRunnable)
    }


    private fun updateLocationData() {
        LatLon.getLocation(this) { location ->
            if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude
                tvLocation.text = "Широта: $latitude\nДолгота: $longitude"

                Power.getSignalStrength(this) { strength ->
                    tvSignalStrength.text = "Мощность сигнала: $strength"
                }
            } else {
                tvLocation.text = "Местоположение недоступно"
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        LatLon.stopLocationUpdates(this)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                updateLocationData()
            } else {
                tvLocation.text = "Нет разрешения на доступ к локации"
            }
        }
    }
}