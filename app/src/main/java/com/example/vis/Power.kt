package com.example.vis

import android.annotation.SuppressLint
import android.content.Context
import android.telephony.*
import android.telephony.SignalStrength


object Power {

    @SuppressLint("MissingPermission")
    fun getSignalStrength(context: Context, callback: (Int, Int?) -> Unit) {
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val telephonyCallback = object : TelephonyCallback(), TelephonyCallback.SignalStrengthsListener {
                override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
                    val strength = signalStrength.level
                    val rsrp = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                        signalStrength.getCellSignalStrengths(CellSignalStrengthLte::class.java).firstOrNull()?.rsrp
                    } else {
                        null
                    }
                    callback(strength, rsrp)

                }
            }

            telephonyManager.registerTelephonyCallback(context.mainExecutor, telephonyCallback)

        } else {
            // Код для старых версий Android (до Android 12)

            val phoneStateListener = object : PhoneStateListener() {
                @Deprecated("Deprecated in Java")
                override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
                    super.onSignalStrengthsChanged(signalStrength)

                    val strength = signalStrength.level

                    val rsrp = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                        signalStrength.getCellSignalStrengths(CellSignalStrengthLte::class.java).firstOrNull()?.rsrp
                    } else {
                        null
                    }
                    callback(strength, rsrp)

                    telephonyManager.listen(this, PhoneStateListener.LISTEN_NONE)
                }
            }


            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS)
        }
    }
}