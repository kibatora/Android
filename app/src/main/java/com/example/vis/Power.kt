package com.example.vis
import android.annotation.SuppressLint
import android.content.Context
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.telephony.SignalStrength
import android.telephony.PhoneStateListener

object Power {

    @SuppressLint("MissingPermission")
    fun getSignalStrength(context: Context, callback: (Int) -> Unit) {
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        // Проверка версии SDK
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val telephonyCallback = object : TelephonyCallback(), TelephonyCallback.SignalStrengthsListener {
                override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
                    val strength = signalStrength.level
                    callback(strength)
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
                    callback(strength)
                    // Отключаем слушатель после получения данных
                    telephonyManager.listen(this, PhoneStateListener.LISTEN_NONE)
                }
            }

            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS)
        }
    }
}
