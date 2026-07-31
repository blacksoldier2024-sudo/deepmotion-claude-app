package com.leatherpunch.ble.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leatherpunch.ble.ble.ConnStatus
import com.leatherpunch.ble.ui.theme.*

@Composable
fun ConnectScreen(status: ConnStatus, onConnect: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBlack)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .background(PanelNavy, RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Bluetooth,
                contentDescription = null,
                tint = if (status == ConnStatus.SCANNING || status == ConnStatus.CONNECTING) Amber else TextDim,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(
            "LEATHER PUNCH",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(
            when (status) {
                ConnStatus.SCANNING -> "در حال جستجوی دستگاه..."
                ConnStatus.CONNECTING -> "در حال برقراری اتصال..."
                ConnStatus.ERROR -> "بلوتوث خاموشه یا مشکلی پیش اومده"
                else -> "برای اتصال به دستگاه، بلوتوثش رو روشن نگه دارید و نزدیک بمونید"
            },
            color = TextMid,
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(36.dp))

        if (status == ConnStatus.SCANNING || status == ConnStatus.CONNECTING) {
            CircularProgressIndicator(color = Amber, strokeWidth = 2.dp)
        } else {
            Button(
                onClick = onConnect,
                colors = ButtonDefaults.buttonColors(containerColor = Amber, contentColor = BgBlack),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("اتصال به دستگاه", fontFamily = FontFamily.Monospace)
            }
        }
    }
}
