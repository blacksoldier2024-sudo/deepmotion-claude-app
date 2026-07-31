package com.leatherpunch.ble.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leatherpunch.ble.model.DeviceState
import com.leatherpunch.ble.ui.theme.*

@Composable
fun HomeScreen(state: DeviceState, onOpenManual: () -> Unit, onOpenRate: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBlack)
            .padding(16.dp)
    ) {
        Text(
            "پروفایل مورد نظر رو انتخاب کنید",
            color = TextDim,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        ProfileCard(
            icon = Icons.Filled.PanTool,
            title = "MANUAL",
            subtitle = "حرکت دستی، Home و کالیبراسیون",
            active = state.screen == "manual",
            onClick = onOpenManual
        )

        Spacer(Modifier.height(12.dp))

        ProfileCard(
            icon = Icons.Filled.Speed,
            title = "RATE PUNCH",
            subtitle = "پانچ پیوسته با نرخ ثابت (۱ تا ۶۰ در دقیقه)",
            active = state.screen == "rate",
            running = state.rateRunning,
            onClick = onOpenRate
        )
    }
}

@Composable
private fun ProfileCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    active: Boolean,
    running: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (active) PanelNavyLight else PanelNavy)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (running) OkGreenDim else AmberDim.copy(alpha = 0.25f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = if (running) OkGreen else Amber, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 15.sp)
                if (running) {
                    Spacer(Modifier.width(8.dp))
                    Text("در حال اجرا", color = OkGreen, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(subtitle, color = TextDim, fontSize = 12.sp)
        }
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextDim)
    }
}
