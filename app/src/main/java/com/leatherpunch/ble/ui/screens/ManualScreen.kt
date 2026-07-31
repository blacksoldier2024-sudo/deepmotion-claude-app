package com.leatherpunch.ble.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leatherpunch.ble.model.DeviceState
import com.leatherpunch.ble.ui.theme.*

@Composable
fun ManualScreen(
    state: DeviceState,
    onJog: (Float) -> Unit,
    onHome: () -> Unit,
    onEndstop: () -> Unit,
    onStop: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBlack)
            .padding(16.dp)
    ) {
        // ---- کارت موقعیت فعلی ----
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(PanelNavy)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("موقعیت فعلی", color = TextDim, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            Spacer(Modifier.height(4.dp))
            Text(
                "%.1f mm".format(state.posMm),
                color = Amber,
                fontSize = 40.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(Modifier.height(10.dp))
            Row {
                if (state.homeSwitch) SwitchPill("HOME SW", Amber)
                if (state.endstop) SwitchPill("END SW", DangerRed)
                if (state.motorRunning) SwitchPill("در حال حرکت", OkGreen)
            }
        }

        Spacer(Modifier.height(20.dp))

        Text("حرکت دستی (jog)", color = TextDim, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
        Spacer(Modifier.height(10.dp))

        // ---- دکمه‌های جاگ ----
        val jogSteps = listOf(-10f, -1f, 1f, 10f)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            jogSteps.forEach { d ->
                JogButton(delta = d, modifier = Modifier.weight(1f)) { onJog(d) }
            }
        }

        Spacer(Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = onHome,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Amber),
                border = androidx.compose.foundation.BorderStroke(1.dp, Amber)
            ) {
                Icon(Icons.Filled.Home, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("HOME", fontFamily = FontFamily.Monospace)
            }
            OutlinedButton(
                onClick = onEndstop,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMid),
                border = androidx.compose.foundation.BorderStroke(1.dp, LineSubtle)
            ) {
                Icon(Icons.Filled.LastPage, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("END STOP", fontFamily = FontFamily.Monospace, fontSize = 13.sp)
            }
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onStop,
            colors = ButtonDefaults.buttonColors(containerColor = DangerRedDim, contentColor = DangerRed),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(Icons.Filled.Stop, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("STOP", fontFamily = FontFamily.Monospace, fontSize = 16.sp)
        }
    }
}

@Composable
private fun JogButton(delta: Float, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val label = if (delta > 0) "+${delta.toInt()}" else "${delta.toInt()}"
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PanelNavyLight,
            contentColor = if (delta > 0) Amber else TextMid
        )
    ) {
        Text(label, fontFamily = FontFamily.Monospace, fontSize = 16.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun SwitchPill(label: String, color: Color) {
    Box(
        modifier = Modifier
            .padding(end = 6.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(label, color = color, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
    }
}
