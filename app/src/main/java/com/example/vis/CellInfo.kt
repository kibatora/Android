package com.example.vis

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker

data class CellInfo(
    val pci: Int?,
    val rsrp: Int,
    val location: LatLng,
    var marker: Marker? = null,
    var previousCellInfo : CellInfo? = null // Хранит предыдущий объект CellInfo

)