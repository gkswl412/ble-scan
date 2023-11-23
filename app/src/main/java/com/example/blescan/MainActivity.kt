package com.example.blescan

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import android.util.Log
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import java.nio.ByteBuffer

class MainActivity : Activity() {
    private lateinit var bluetoothLeScanner : BluetoothLeScanner
    private var scanning = false
    private lateinit var mScanButton: Button
    private lateinit var mStopScanButton: Button

    companion object {
        const val TAG = "scan result"
        const val STRING_UUID: String = "CDB7950D-73F1-4D4D-8E47-C090502DBD63"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setBleScanner(this)
        mScanButton = findViewById(R.id.scan)
        mStopScanButton = findViewById(R.id.stopscan)

        mScanButton.setOnClickListener {
            scanLeDevice()
        }

        mStopScanButton.setOnClickListener {
            stopScan()
        }
    }

    private fun setBleScanner(context: Context) {
        bluetoothLeScanner = (context.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager).adapter.bluetoothLeScanner
    }

    private val leScanCallback: ScanCallback = object : ScanCallback() {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            if (result.scanRecord != null && result.scanRecord!!.serviceData != null && result.scanRecord?.serviceData?.get(
                    ParcelUuid.fromString(STRING_UUID)) != null) {
                var receiveTime = System.currentTimeMillis()
                var sendTime = byteArrayToLong(
                    result.scanRecord?.serviceData?.get(
                        ParcelUuid.fromString(STRING_UUID))!!
                )
                Log.d(TAG, "송신 시간 : $sendTime\n수신 시간 : $receiveTime")
                Log.d(TAG, "시간차(ms) : ${receiveTime - sendTime}")
            }
        }
    }


    private fun scanLeDevice() {
        scanning = true
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.ACCESS_FINE_LOCATION), 100)
            return
        }
        val filters: List<ScanFilter> = listOf(
            ScanFilter.Builder()
                .setServiceData(ParcelUuid.fromString(STRING_UUID.lowercase()), byteArrayOf())
                .build(),
        )

        val settings: ScanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .build()

        bluetoothLeScanner.startScan(filters, settings, leScanCallback)
    }

    private fun stopScan() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        bluetoothLeScanner.stopScan(leScanCallback)
    }

    fun byteArrayToLong(byteArray: ByteArray): Long {
        require(byteArray.size == java.lang.Long.BYTES) { "배열 길이가 올바르지 않습니다." }
        val buffer = ByteBuffer.wrap(byteArray)
        return buffer.long
    }
}
