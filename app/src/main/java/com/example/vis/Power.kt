package com.example.vis

import android.annotation.SuppressLint
import android.content.Context
import android.telephony.*

object Power {

    @SuppressLint("MissingPermission")
    fun getSignalStrength(context: Context): Triple<Int?, Int?, Int?> {
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val signalStrength = telephonyManager.signalStrength

        var rsrp: Int? = null
        var pci: Int? = null
        var strength: Int? = null



        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            signalStrength?.cellSignalStrengths?.forEach { cellSignalStrength ->
                when (cellSignalStrength) {
                    is CellSignalStrengthLte -> {
                        rsrp = cellSignalStrength.rsrp
                    }
                    is CellSignalStrengthNr -> {
                        rsrp = cellSignalStrength.csiRsrp
                    }
                    // Добавьте другие типы сетей по мере необходимости
                }

            }
        }
        else
        {
            val cellSignalStrength = signalStrength?.cellSignalStrengths?.firstOrNull()
            if (cellSignalStrength is CellSignalStrengthLte) {
                rsrp = cellSignalStrength.rsrp
            }
        }


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {

            for (info in telephonyManager.allCellInfo) {
                if (info is CellInfoLte) {
                    pci = (info.cellIdentity as CellIdentityLte).pci
                    break
                }
            }
        }

        strength = signalStrength?.level
        return Triple(strength, rsrp, pci)
    }
}