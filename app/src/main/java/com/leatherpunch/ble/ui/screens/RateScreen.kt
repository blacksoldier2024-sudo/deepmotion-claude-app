package com.leatherpunch.ble.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leatherpunch.ble.model.DeviceState
import com.leatherpunch.ble.ui.theme.*

private val PRESETS = listOf(5, 10, 20, 30, 60)

@Composable
fun RateScreen(
    state: DeviceState,
    onSetPpm: (Int) -> Unit,
    onAdjustPpm: (Int) -> Unit,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
) {
    val active = state.rateRunning || state.ratePausing

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBlack)
            .padding(16.dp)
    ) {
        // ---- کارت وضعیت ----
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(PanelNavy)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(8.dp).clip(CircleShape)
                        .background(if (active) OkGreen else Amber)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    when {
                        state.ratePausing -> "در حال بازگشت به HOME"
                        state.rateRunning -> "در حال اجرا"
                        else -> "آماده"
                    },
                    color = if (active) OkGreen else TextDim,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
            }
            Spacer(Modifier.height(10.dp))
            Text("${state.ratePpm}", color = Amber, fontSize = 56.sp, fontFamily = FontFamily.Monospace)
            Text("PUNCH / MIN", color = TextDim, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            Spacer(Modifier.height(10.dp))
            Text("%.1f mm از HOME".format(state.posMm), color = TextMid, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
        }

        Spacer(Modifier.height(18.dp))

        // ---- ± PPM ----
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            listOf(-5, -1, 1, 5).forEach { d ->
                AdjButton(d, Modifier.weight(1f)) { onAdjustPpm(d) }
            }
        }

        Spacer(Modifier.height(10.dp))

        // ---- پیش‌فرض‌ها ----
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            PRESETS.forEach { v ->
                PresetButton(v, active = state.ratePpm == v, modifier = Modifier.weight(1f)) { onSetPpm(v) }
            }
        }

        Spacer(Modifier.weight(1f))

        // ---- START / PAUSE ----
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = onStart,
                enabled = !active,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OkGreenDim, contentColor = OkGreen, disabledContainerColor = PanelNavyLight, disabledContentColor = TextDim)
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("START", fontFamily = FontFamily.Monospace)
            }
            Button(
                onClick = onPause,
                enabled = state.rateRunning,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DangerRedDim, contentColor = DangerRed, disabledContainerColor = PanelNavyLight, disabledContentColor = TextDim)
            ) {
                Icon(Icons.Filled.Pause, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text(if (state.ratePausing) "HOME..." else "PAUSE", fontFamily = FontFamily.Monospace)
            }
        }

        Spacer(Modifier.height(10.dp))

        OutlinedButton(
            onClick = onStop,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMid),
            border = androidx.compose.foundation.BorderStroke(1.dp, LineSubtle)
        ) {
            Icon(Icons.Filled.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("STOP فوری", fontFamily = FontFamily.Monospace, fontSize = 13.sp)
        }
    }
}

@Composable
private fun AdjButton(delta: Int, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PanelNavyLight,
            contentColor = if (delta > 0) Amber else TextMid
        )
    ) {
        Text(if (delta > 0) "+$delta" else "$delta", fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun PresetButton(value: Int, active: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (active) AmberDim.copy(alpha = 0.35f) else PanelNavy,
            contentColor = if (active) Amber else TextMid
        )
    ) {
        Text("$value", fontFamily = FontFamily.Monospace, fontSize = 13.sp)
    }
}
