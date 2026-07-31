package com.leatherpunch.ble.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leatherpunch.ble.ble.ConnStatus
import com.leatherpunch.ble.ui.theme.*

@Composable
fun DeviceStatusBar(status: ConnStatus, posMm: Float, homeSwitch: Boolean, endstop: Boolean) {
    val connected = status == ConnStatus.CONNECTED
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PanelNavy)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (connected) OkGreen else DangerRed)
            )
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = if (connected) Icons.Filled.Bluetooth else Icons.Filled.BluetoothDisabled,
                contentDescription = null,
                tint = if (connected) Amber else TextDim,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = when (status) {
                    ConnStatus.CONNECTED -> "متصل"
                    ConnStatus.CONNECTING -> "در حال اتصال..."
                    ConnStatus.SCANNING -> "در حال جستجو..."
                    ConnStatus.ERROR -> "خطا در بلوتوث"
                    ConnStatus.DISCONNECTED -> "قطع"
                },
                color = if (connected) TextMid else TextDim,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        if (connected) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (homeSwitch) StatusChip("HOME", Amber)
                if (endstop) StatusChip("END", DangerRed)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "%.1f mm".format(posMm),
                    color = Amber,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun StatusChip(label: String, color: androidx.compose.ui.graphics.Color) {
    Box(
        modifier = Modifier
            .padding(end = 6.dp)
            .clip(MaterialTheme.shapes.small)
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(label, color = color, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
    }
}
