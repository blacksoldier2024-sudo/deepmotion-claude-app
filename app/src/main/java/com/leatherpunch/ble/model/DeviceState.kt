package com.leatherpunch.ble.model

import org.json.JSONObject

/**
 * وضعیت لحظه‌ای دستگاه - دقیقاً منعکس‌کننده‌ی JSON کوچیکی که BleSync.cpp
 * روی فرمور می‌سازه. کلیدها عمداً کوتاه‌ان (scr, pos, run, ...) تا حجم
 * پیام بلوتوث کم بمونه.
 */
data class DeviceState(
    val screen: String = "home",     // "home" | "manual" | "rate" | "settings" | "other"
    val posMm: Float = 0f,
    val motorRunning: Boolean = false,
    val homeSwitch: Boolean = false,
    val endstop: Boolean = false,
    val ratePpm: Int = 30,
    val rateRunning: Boolean = false,
    val ratePausing: Boolean = false,
) {
    companion object {
        fun parse(json: String, previous: DeviceState): DeviceState {
            return try {
                val o = JSONObject(json)
                DeviceState(
                    screen = o.optString("scr", previous.screen),
                    posMm = o.optInt("pos", (previous.posMm * 10).toInt()) / 10f,
                    motorRunning = o.optBoolean("run", previous.motorRunning),
                    homeSwitch = o.optBoolean("hs", previous.homeSwitch),
                    endstop = o.optBoolean("es", previous.endstop),
                    ratePpm = o.optInt("ppm", previous.ratePpm),
                    rateRunning = o.optBoolean("rrun", previous.rateRunning),
                    ratePausing = o.optBoolean("rpau", previous.ratePausing),
                )
            } catch (e: Exception) {
                previous
            }
        }
    }
}
