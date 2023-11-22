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
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import com.example.blescan.ui.theme.BleScanTheme

class MainActivity : Activity() {
    private lateinit var bluetoothLeScanner : BluetoothLeScanner
    private var scanning = false
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var mScanButton: Button
    private lateinit var mStopScanButton: Button

    companion object {
        const val TAG = "scan result"
        const val SCAN_PERIOD: Long = 10000
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
            Log.d(TAG, result.scanRecord.toString())
            Log.d(TAG, result.device.address.toString())
            Log.d(TAG, result.dataStatus.toString())
            Log.d(TAG, result.advertisingSid.toString())
        }
    }

    private fun scanLeDevice() {
        scanning = true
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.ACCESS_FINE_LOCATION), 100)
            return
        }
        val filters: List<ScanFilter> = listOf(
            ScanFilter.Builder()
                .setServiceUuid(ParcelUuid.fromString("CDB7950D-73F1-4D4D-8E47-C090502DBD63".lowercase())) // 원하는 기기의 이름으로 필터링
                .build(),
            // 또는 다른 필터를 추가할 수 있음
        )

        val settings: ScanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .build()

        bluetoothLeScanner.startScan(leScanCallback)
    }

    private fun stopScan() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        bluetoothLeScanner.stopScan(leScanCallback)
    }
}
