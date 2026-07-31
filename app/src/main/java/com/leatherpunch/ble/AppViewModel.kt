package com.leatherpunch.ble

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.leatherpunch.ble.ble.BleManager
import com.leatherpunch.ble.ble.ConnStatus
import com.leatherpunch.ble.model.DeviceState

class AppViewModel(app: Application) : AndroidViewModel(app) {

    val ble = BleManager(app.applicationContext)

    val status get() = ble.status
    val deviceState get() = ble.deviceState

    fun connect() = ble.startScanAndConnect()
    fun disconnect() = ble.disconnect()

    // ---- ناوبری ----
    fun navigate(screen: String) = ble.sendCommand("""{"c":"nav","scr":"$screen"}""")

    // ---- Manual ----
    fun jog(mm: Float, speed: Float = 1200f) = ble.sendCommand("""{"c":"jog","mm":$mm,"spd":$speed}""")
    fun homeDevice() = ble.sendCommand("""{"c":"home"}""")
    fun moveToEndstop() = ble.sendCommand("""{"c":"endstop"}""")
    fun stopMotor() = ble.sendCommand("""{"c":"stop"}""")

    // ---- Rate ----
    fun rateSetPpm(ppm: Int) = ble.sendCommand("""{"c":"rate_set","ppm":$ppm}""")
    fun rateAdjustPpm(delta: Int) = ble.sendCommand("""{"c":"rate_adj","d":$delta}""")
    fun rateStart() = ble.sendCommand("""{"c":"rate_start"}""")
    fun ratePause() = ble.sendCommand("""{"c":"rate_pause"}""")
    fun rateStop() = ble.sendCommand("""{"c":"rate_stop"}""")
}
