package com.leatherpunch.ble

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.core.content.ContextCompat
import com.leatherpunch.ble.ble.ConnStatus
import com.leatherpunch.ble.ui.screens.*
import com.leatherpunch.ble.ui.theme.*

class MainActivity : ComponentActivity() {

    private val viewModel: AppViewModel by viewModels()

    private val requiredPermissions: Array<String>
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.values.all { it }) viewModel.connect()
    }

    private fun hasAllPermissions() = requiredPermissions.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LeatherPunchTheme {
                Surface(color = BgBlack) {
                    AppRoot(
                        viewModel = viewModel,
                        onRequestConnect = {
                            val adapter = BluetoothAdapter.getDefaultAdapter()
                            if (adapter == null || !adapter.isEnabled) {
                                startActivity(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                            } else if (hasAllPermissions()) {
                                viewModel.connect()
                            } else {
                                permissionLauncher.launch(requiredPermissions)
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // اتصال رو باز نگه می‌داریم تا وقتی کاربر واقعاً از اپ خارج بشه؛
        // فقط اگه اسکن در حال انجامه متوقفش کن تا باتری هدر نره.
        viewModel.ble.stopScan()
    }
}

private enum class Tab(val id: String, val label: String) {
    HOME("home", "خانه"),
    MANUAL("manual", "دستی"),
    RATE("rate", "نرخ ثابت"),
}

@Composable
private fun AppRoot(viewModel: AppViewModel, onRequestConnect: () -> Unit) {
    val status by viewModel.status.collectAsState()
    val state by viewModel.deviceState.collectAsState()

    var tab by remember { mutableStateOf(Tab.HOME) }

    // هر وقت صفحه‌ی دستگاه از سمت خودش (لمس فیزیکی) عوض شد، تب موبایل هم سینک بشه
    LaunchedEffect(state.screen) {
        tab = when (state.screen) {
            "manual" -> Tab.MANUAL
            "rate" -> Tab.RATE
            else -> if (state.screen == "home") Tab.HOME else tab
        }
    }

    if (status != ConnStatus.CONNECTED) {
        ConnectScreen(status = status, onConnect = onRequestConnect)
        return
    }

    Scaffold(
        containerColor = BgBlack,
        topBar = {
            DeviceStatusBar(status = status, posMm = state.posMm, homeSwitch = state.homeSwitch, endstop = state.endstop)
        },
        bottomBar = {
            NavigationBar(containerColor = PanelNavy) {
                Tab.values().forEach { t ->
                    NavigationBarItem(
                        selected = tab == t,
                        onClick = {
                            tab = t
                            viewModel.navigate(t.id)
                        },
                        icon = {
                            Icon(
                                when (t) {
                                    Tab.HOME -> Icons.Filled.Home
                                    Tab.MANUAL -> Icons.Filled.PanTool
                                    Tab.RATE -> Icons.Filled.Speed
                                },
                                contentDescription = t.label
                            )
                        },
                        label = { Text(t.label, fontFamily = FontFamily.Monospace) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Amber,
                            selectedTextColor = Amber,
                            unselectedIconColor = TextDim,
                            unselectedTextColor = TextDim,
                            indicatorColor = PanelNavyLight
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).background(BgBlack)) {
            when (tab) {
                Tab.HOME -> HomeScreen(
                    state = state,
                    onOpenManual = { tab = Tab.MANUAL; viewModel.navigate("manual") },
                    onOpenRate = { tab = Tab.RATE; viewModel.navigate("rate") }
                )
                Tab.MANUAL -> ManualScreen(
                    state = state,
                    onJog = { viewModel.jog(it) },
                    onHome = { viewModel.homeDevice() },
                    onEndstop = { viewModel.moveToEndstop() },
                    onStop = { viewModel.stopMotor() }
                )
                Tab.RATE -> RateScreen(
                    state = state,
                    onSetPpm = { viewModel.rateSetPpm(it) },
                    onAdjustPpm = { viewModel.rateAdjustPpm(it) },
                    onStart = { viewModel.rateStart() },
                    onPause = { viewModel.ratePause() },
                    onStop = { viewModel.rateStop() }
                )
            }
        }
    }
}
