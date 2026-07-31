package com.leatherpunch.ble.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LeatherPunchColorScheme = darkColorScheme(
    primary = Amber,
    onPrimary = BgBlack,
    secondary = OkGreen,
    error = DangerRed,
    background = BgBlack,
    onBackground = TextMid,
    surface = PanelNavy,
    onSurface = TextMid,
    surfaceVariant = PanelNavyLight,
    outline = LineSubtle,
)

@Composable
fun LeatherPunchTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = BgBlack.toArgb()
            window.navigationBarColor = BgBlack.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }
    MaterialTheme(
        colorScheme = LeatherPunchColorScheme,
        typography = Typography,
        content = content
    )
}
