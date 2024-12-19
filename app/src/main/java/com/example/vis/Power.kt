package com.example.vis

import android.annotation.SuppressLint
import android.content.Context
import android.telephony.*

object Power {

    @SuppressLint("MissingPermission")
    fun getSignalStrength(context: Context, callback: (Int, Int?, Int?) -> Unit) {
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        val telephonyCallback = object : TelephonyCallback(), TelephonyCallback.SignalStrengthsListener {
            override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
                val strength = signalStrength.level
                var rsrp: Int? = null
                var pci: Int? = null

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    val cellSignalStrengths = signalStrength.cellSignalStrengths
                    for (cellSignalStrength in cellSignalStrengths) {
                        when (cellSignalStrength) {
                            is CellSignalStrengthLte -> {
                                rsrp = cellSignalStrength.rsrp
                            }
                            is CellSignalStrengthNr -> {
                                rsrp = cellSignalStrength.csiRsrp
                            }
                        }
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




                callback(strength, rsrp, pci)
            }
        }
        telephonyManager.registerTelephonyCallback(context.mainExecutor, telephonyCallback)


    }

}