package com.leatherpunch.ble.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import com.leatherpunch.ble.model.DeviceState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import java.util.concurrent.ConcurrentLinkedQueue

// -----------------------------------------------------------------------
// این UUID ها باید دقیقاً با BleSync.cpp روی فرمور یکی باشن.
// -----------------------------------------------------------------------
private val SERVICE_UUID    = UUID.fromString("b7e1a000-4a3c-4a6e-9d0a-1a2b3c4d5e00")
private val STATE_CHAR_UUID = UUID.fromString("b7e1a001-4a3c-4a6e-9d0a-1a2b3c4d5e00")
private val CMD_CHAR_UUID   = UUID.fromString("b7e1a002-4a3c-4a6e-9d0a-1a2b3c4d5e00")
private val CCCD_UUID       = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

private const val DEVICE_NAME = "LeatherPunch"

enum class ConnStatus { DISCONNECTED, SCANNING, CONNECTING, CONNECTED, ERROR }

@SuppressLint("MissingPermission") // مجوزها قبل از فراخوانی این کلاس در UI چک میشن
class BleManager(private val context: Context) {

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val adapter get() = bluetoothManager.adapter

    private var gatt: BluetoothGatt? = null
    private var stateChar: BluetoothGattCharacteristic? = null
    private var cmdChar: BluetoothGattCharacteristic? = null

    private val _status = MutableStateFlow(ConnStatus.DISCONNECTED)
    val status: StateFlow<ConnStatus> = _status.asStateFlow()

    private val _deviceState = MutableStateFlow(DeviceState())
    val deviceState: StateFlow<DeviceState> = _deviceState.asStateFlow()

    private val _foundDeviceName = MutableStateFlow<String?>(null)
    val foundDeviceName: StateFlow<String?> = _foundDeviceName.asStateFlow()

    // نوشتن‌ها رو صف می‌کنیم چون BLE اجازه‌ی چند write هم‌زمان روی یک GATT رو نمیده
    private val writeQueue = ConcurrentLinkedQueue<ByteArray>()
    private var writeInFlight = false

    fun startScanAndConnect() {
        if (adapter == null || !adapter.isEnabled) {
            _status.value = ConnStatus.ERROR
            return
        }
        _status.value = ConnStatus.SCANNING
        val scanner = adapter.bluetoothLeScanner
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        scanner?.startScan(null, settings, scanCallback)
    }

    fun stopScan() {
        adapter?.bluetoothLeScanner?.stopScan(scanCallback)
        if (_status.value == ConnStatus.SCANNING) _status.value = ConnStatus.DISCONNECTED
    }

    fun disconnect() {
        gatt?.disconnect()
    }

    /** ارسال یک دستور JSON کوچیک به دستگاه، مثلاً {"c":"rate_start"} */
    fun sendCommand(json: String) {
        val bytes = json.toByteArray(Charsets.UTF_8)
        writeQueue.add(bytes)
        pumpWriteQueue()
    }

    private fun pumpWriteQueue() {
        if (writeInFlight) return
        val ch = cmdChar ?: return
        val g = gatt ?: return
        val next = writeQueue.poll() ?: return
        writeInFlight = true
        ch.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            g.writeCharacteristic(ch, next, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE)
        } else {
            @Suppress("DEPRECATION")
            ch.value = next
            @Suppress("DEPRECATION")
            g.writeCharacteristic(ch)
        }
        // WRITE_NO_RESPONSE هیچ callback تاییدی نداره، پس با کمی تاخیر صف رو ادامه می‌دیم
        writeInFlight = false
        if (writeQueue.isNotEmpty()) pumpWriteQueue()
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val name = result.device.name ?: result.scanRecord?.deviceName ?: return
            if (name != DEVICE_NAME) return
            _foundDeviceName.value = name
            adapter.bluetoothLeScanner?.stopScan(this)
            _status.value = ConnStatus.CONNECTING
            gatt = result.device.connectGatt(context, false, gattCallback)
        }

        override fun onScanFailed(errorCode: Int) {
            _status.value = ConnStatus.ERROR
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(g: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    g.requestMtu(185)
                    g.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    _status.value = ConnStatus.DISCONNECTED
                    stateChar = null
                    cmdChar = null
                    gatt = null
                }
            }
        }

        override fun onMtuChanged(g: BluetoothGatt, mtu: Int, status: Int) {
            g.discoverServices()
        }

        override fun onServicesDiscovered(g: BluetoothGatt, status: Int) {
            val service = g.getService(SERVICE_UUID)
            stateChar = service?.getCharacteristic(STATE_CHAR_UUID)
            cmdChar = service?.getCharacteristic(CMD_CHAR_UUID)

            stateChar?.let { ch ->
                g.setCharacteristicNotification(ch, true)
                val cccd = ch.getDescriptor(CCCD_UUID)
                if (cccd != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        g.writeDescriptor(cccd, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                    } else {
                        @Suppress("DEPRECATION")
                        cccd.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        @Suppress("DEPRECATION")
                        g.writeDescriptor(cccd)
                    }
                }
            }
            _status.value = ConnStatus.CONNECTED
        }

        override fun onCharacteristicChanged(g: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray) {
            if (characteristic.uuid == STATE_CHAR_UUID) {
                val json = String(value, Charsets.UTF_8)
                _deviceState.value = DeviceState.parse(json, _deviceState.value)
            }
        }

        // پشتیبانی از API قدیمی‌تر (زیر ۳۳) که value رو مستقیم از characteristic می‌گیره
        @Suppress("DEPRECATION")
        override fun onCharacteristicChanged(g: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU && characteristic.uuid == STATE_CHAR_UUID) {
                val json = String(characteristic.value ?: return, Charsets.UTF_8)
                _deviceState.value = DeviceState.parse(json, _deviceState.value)
            }
        }
    }
}
