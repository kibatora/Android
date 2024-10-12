package com.example.vis
import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.widget.LinearLayout
import android.view.Gravity

class MainActivity : Activity() {

    private lateinit var tvLocation: TextView
    private lateinit var btnUpdate: Button
    private lateinit var tvSignalStrength: TextView // Объявлено как свойство класса

    private companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Создаем LinearLayout
        val linearLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
            }
            setPadding(16, 16, 16, 16)

            // Создаем TextView
            tvLocation = TextView(this@MainActivity).apply {
                textSize = 18f
            }
            addView(tvLocation)

            // Создаем TextView для мощности сигнала (добавлено здесь)
            tvSignalStrength = TextView(this@MainActivity).apply {
                textSize = 18f
            }
            addView(tvSignalStrength)

            // Создаем кнопку "Обновить"
            btnUpdate = Button(this@MainActivity).apply {
                // ... (остальной код создания кнопки) ...
            }
            addView(btnUpdate)
        }

        // Устанавливаем LinearLayout как контент
        setContentView(linearLayout)

        requestLocationUpdates()
    }

    // Функция для запроса разрешений и обновления локации
    private fun requestLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            updateLocationData()
        }
    }

    // Функция для обновления данных о местоположении
    private fun updateLocationData() {
        val location = LatLon.getLocation(this)
        if (location != null) {
            val latitude = location.latitude
            val longitude = location.longitude
            tvLocation.text = "Широта: $latitude\nДолгота: $longitude"
            Power.getSignalStrength(this) { strength ->
                tvSignalStrength.text = "Мощность сигнала: $strength"
            }


            // TODO: Получение и отображение мощности сигнала
        } else {
            tvLocation.text = "Местоположение недоступно"
        }
    }

    // Обработка результатов запроса разрешений
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                updateLocationData()
            } else {
                tvLocation.text = "Нет разрешения на доступ к локации"  // <-- Прямая строка
            }
        }
    }
}