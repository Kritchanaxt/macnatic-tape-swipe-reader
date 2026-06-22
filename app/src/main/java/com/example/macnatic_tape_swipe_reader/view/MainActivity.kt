package com.example.macnatic_tape_swipe_reader.view

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.BroadcastReceiver
import android.hardware.usb.UsbManager
import android.os.Bundle
import com.example.macnatic_tape_swipe_reader.features.monitor_logging.AppLog as Log
import com.gemalto.jp2.JP2Decoder
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.macnatic_tape_swipe_reader.features.msr.models.ThaiDrivingLicense
import com.example.macnatic_tape_swipe_reader.features.msr.parsers.ThaiDrivingLicenseParser
import com.example.macnatic_tape_swipe_reader.services.SunmiPaySdkManager
import com.example.macnatic_tape_swipe_reader.view.components.MacnaticTheme

class MainActivity : ComponentActivity() {

    private companion object {
        private const val TAG = "MainActivity"
    }

    private var connectionStatus by mutableStateOf("Initializing")
    private var licenseData by mutableStateOf<ThaiDrivingLicense?>(null)

    private var isUsbConnected by mutableStateOf(false)
    private var usbReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize App Log
        Log.init(applicationContext)

        setContent {
            MacnaticTheme {
                MsrScannerScreen(
                    connectionStatus = connectionStatus,
                    licenseData = licenseData,
                    onStartPolling = { startCardPolling() },
                    onCancelPolling = { cancelCardPolling() },
                    onReset = { licenseData = null },
                    onUpdateLicenseData = { licenseData = it },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Attach Log Floating Overlay Button after setContent to prevent Compose content replacement
        com.example.macnatic_tape_swipe_reader.features.monitor_logging.LogOverlayHelper.attach(this)

        // Register USB attach/detach BroadcastReceiver
        val filter = IntentFilter().apply {
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                checkUsbConnection()
            }
        }
        usbReceiver = receiver
        try {
            androidx.core.content.ContextCompat.registerReceiver(
                this,
                receiver,
                filter,
                androidx.core.content.ContextCompat.RECEIVER_EXPORTED
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register USB BroadcastReceiver safely", e)
        }

        // Perform initial USB check
        checkUsbConnection()

        // Initialize Sunmi Pay SDK V2
        initSdk()
    }

    private val keyBuffer = StringBuilder()
    private var lastKeyTime = 0L

    private var tempTrack1 = ""
    private var tempTrack2 = ""
    private var tempTrack3 = ""

    private val resetTracksRunnable = Runnable {
        tempTrack1 = ""
        tempTrack2 = ""
        tempTrack3 = ""
    }
    private val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())

    override fun dispatchKeyEvent(event: android.view.KeyEvent): Boolean {
        if (event.action == android.view.KeyEvent.ACTION_DOWN) {
            val unicodeChar = event.unicodeChar
            val currentTime = System.currentTimeMillis()

            // Reset keyboard buffer if there's a long delay between keypresses
            // to separate manual typing from high-speed MSR card swiping
            if (currentTime - lastKeyTime > 600) {
                keyBuffer.setLength(0)
            }
            lastKeyTime = currentTime

            if (event.keyCode == android.view.KeyEvent.KEYCODE_ENTER) {
                val rawData = keyBuffer.toString().trim()
                keyBuffer.setLength(0)
                if (rawData.isNotEmpty()) {
                    handleRawMsrInput(rawData)
                }
                return true
            }

            if (unicodeChar != 0) {
                keyBuffer.append(unicodeChar.toChar())
            }
        }
        return super.dispatchKeyEvent(event)
    }

    private fun handleRawMsrInput(line: String) {
        Log.d(TAG, "Raw MSR Line Received: $line")

        val isMSRLine = line.startsWith("%") || line.contains("%") ||
                        line.startsWith(";") || line.contains(";") ||
                        line.startsWith("+") || line.contains("+") ||
                        (line.length >= 13 && line.all { it.isDigit() })

        if (!isMSRLine) {
            // Not MSR data (likely manual typing), ignore it to prevent accidental transition
            return
        }
        
        mainHandler.removeCallbacks(resetTracksRunnable)

        // Identify which track was swiped by looking at track start sentinels
        if (line.startsWith("%") || line.contains("%")) {
            tempTrack1 = line
        } else if (line.startsWith(";") || line.contains(";")) {
            tempTrack2 = line
        } else if (line.startsWith("+") || line.contains("+")) {
            tempTrack3 = line
        } else {
            // Fallback for MSRs that only type numeric values
            if (line.length >= 13 && line.all { it.isDigit() }) {
                tempTrack2 = ";$line="
            }
        }

        // Parse and check if card reading is successful
        val parsed = ThaiDrivingLicenseParser.parse(
            if (tempTrack1.isNotEmpty()) tempTrack1 else null,
            if (tempTrack2.isNotEmpty()) tempTrack2 else null,
            if (tempTrack3.isNotEmpty()) tempTrack3 else null
        )

        val photoBitmap = if (parsed.citizenId.isNotEmpty()) loadLicensePhoto(parsed.citizenId) else null
        licenseData = parsed.copy(photo = photoBitmap)
        if (parsed.isParsedSuccessfully) {
            Toast.makeText(this, "Card swiped successfully", Toast.LENGTH_SHORT).show()
        }

        // Auto-clear buffered tracks after 2.5 seconds of inactivity to prepare for the next swipe
        mainHandler.postDelayed(resetTracksRunnable, 2500)
    }

    private fun initSdk() {
        connectionStatus = "Initializing"
        SunmiPaySdkManager.init(this) { opt ->
            runOnUiThread {
                updateConnectionStatus()
                startCardPolling()
            }
        }

        // Configure disconnect listener
        SunmiPaySdkManager.onDisconnectedCallback = {
            runOnUiThread {
                updateConnectionStatus()
            }
        }

        // Fallback or initialization status check
        mainHandler.postDelayed({
            updateConnectionStatus()
        }, 1500)
    }

    private fun checkUsbConnection() {
        try {
            isUsbConnected = isUsbMsrConnected(this)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking USB connection status", e)
            isUsbConnected = false
        }
        updateConnectionStatus()
    }

    private fun updateConnectionStatus() {
        runOnUiThread {
            if (SunmiPaySdkManager.isConnected) {
                connectionStatus = "Sunmi PaySDK Connected (Built-in MSR)"
            } else {
                connectionStatus = if (isUsbConnected) {
                    "USB Reader Connected (Ready)"
                } else {
                    "USB Reader Disconnected (Please connect USB)"
                }
            }
        }
    }

    private fun isUsbMsrConnected(context: Context): Boolean {
        try {
            val usbManager = context.getSystemService(Context.USB_SERVICE) as? UsbManager ?: return false
            val deviceList = usbManager.deviceList ?: return false
            Log.d(TAG, "Found ${deviceList.size} connected USB devices:")
            for (device in deviceList.values) {
                if (device == null) continue
                Log.d(
                    TAG,
                    "USB Device: Name=${device.deviceName}, VID=${device.vendorId} (0x${Integer.toHexString(device.vendorId)}), PID=${device.productId} (0x${Integer.toHexString(device.productId)}), Manufacturer=${device.manufacturerName}, Product=${device.productName}"
                )
                val name = ((device.manufacturerName ?: "") + " " + (device.productName ?: "")).lowercase()
                if (name.contains("msr") || 
                    name.contains("magnetic") || 
                    name.contains("reader") || 
                    name.contains("keyboard") ||
                    name.contains("card")) {
                    return true
                }
                
                // Check interface class for HID (Class 3 is Human Interface Device, which MSR keyboards are)
                if (device.deviceClass == 3) {
                    return true
                }
                for (i in 0 until device.interfaceCount) {
                    val inter = device.getInterface(i)
                    if (inter != null && inter.interfaceClass == 3) {
                        return true
                    }
                }
                
                // Common MSR Reader VIDs (e.g. MSR90, MagTek, USBFever)
                val vid = device.vendorId
                if (vid == 0x0801 || vid == 0x0ACD || vid == 0x1130 || vid == 0x04B4 || vid == 0x03EB || vid == 0x4643 || vid == 0x0590 || vid == 0x0c2e) {
                    return true
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in isUsbMsrConnected", e)
        }
        return false
    }

    private fun loadLicensePhoto(citizenId: String): android.graphics.Bitmap? {
        val fileNames = listOf("$citizenId.jp2", "photo.jp2")
        
        // 1. App-specific external storage: /sdcard/Android/data/com.example.macnatic_tape_swipe_reader/files/
        val externalDir = getExternalFilesDir(null)
        if (externalDir != null && externalDir.exists()) {
            for (fileName in fileNames) {
                val file = java.io.File(externalDir, fileName)
                if (file.exists()) {
                    Log.d(TAG, "Found JP2 in external files dir: ${file.absolutePath}")
                    try {
                        val bytes = file.readBytes()
                        val bitmap = JP2Decoder(bytes).decode()
                        if (bitmap != null) return bitmap
                    } catch (e: Exception) {
                        Log.e(TAG, "Error decoding JP2 from external files", e)
                    }
                }
            }
        }

        // 2. Public Download folder: /sdcard/Download/ or /storage/emulated/0/Download/
        val downloadDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
        if (downloadDir != null && downloadDir.exists()) {
            for (fileName in fileNames) {
                val file = java.io.File(downloadDir, fileName)
                if (file.exists()) {
                    Log.d(TAG, "Found JP2 in Download dir: ${file.absolutePath}")
                    try {
                        val bytes = file.readBytes()
                        val bitmap = JP2Decoder(bytes).decode()
                        if (bitmap != null) return bitmap
                    } catch (e: Exception) {
                        Log.e(TAG, "Error decoding JP2 from Download dir", e)
                    }
                }
            }
        }

        // 3. App Assets folder
        for (fileName in fileNames) {
            try {
                assets.open(fileName).use { inputStream ->
                    Log.d(TAG, "Found JP2 in assets: $fileName")
                    val bytes = inputStream.readBytes()
                    val bitmap = JP2Decoder(bytes).decode()
                    if (bitmap != null) return bitmap
                }
            } catch (e: java.io.FileNotFoundException) {
                // normal if file doesn't exist in assets
            } catch (e: Exception) {
                Log.e(TAG, "Error decoding JP2 from assets: $fileName", e)
            }
        }

        Log.w(TAG, "No JP2 file found for CitizenID: $citizenId")
        return null
    }

    private fun startCardPolling() {
        if (!SunmiPaySdkManager.isConnected) {
            Log.w(TAG, "Cannot start polling — SDK not connected")
            return
        }

        SunmiPaySdkManager.startMsrPolling(
            timeoutSec = 60,
            onCardFound = { bundle ->
                runOnUiThread {
                    // Extract tracks from built-in SDK
                    val t1 = bundle.getString("TRACK1")
                    val t2 = bundle.getString("TRACK2")
                    val t3 = bundle.getString("TRACK3")
                    
                    Log.d(TAG, "MSR data received via SDK: TRACK1=$t1, TRACK2=$t2")
                    
                    // Parse license
                    val parsedLicense = ThaiDrivingLicenseParser.parse(t1, t2, t3)
                    val photoBitmap = if (parsedLicense.citizenId.isNotEmpty()) loadLicensePhoto(parsedLicense.citizenId) else null
                    licenseData = parsedLicense.copy(photo = photoBitmap)
                    
                    // Re-start polling after delay
                    android.os.Handler(mainLooper).postDelayed({
                        startCardPolling()
                    }, 1500)
                }
            },
            onError = { code, message ->
                runOnUiThread {
                    Log.e(TAG, "MSR error received: code=$code, msg=$message")
                    if (code == -1 && message.contains("timeout", ignoreCase = true)) {
                        startCardPolling()
                    } else {
                        Toast.makeText(this, "SDK MSR Error ($code): $message", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    private fun cancelCardPolling() {
        SunmiPaySdkManager.cancelMsrPolling()
    }

    override fun onStart() {
        super.onStart()
        if (SunmiPaySdkManager.isConnected) {
            startCardPolling()
        }
    }

    override fun onStop() {
        super.onStop()
        cancelCardPolling()
    }

    override fun onDestroy() {
        usbReceiver?.let {
            try {
                unregisterReceiver(it)
            } catch (e: Exception) {
                Log.e(TAG, "Error unregistering USB receiver", e)
            }
        }
        SunmiPaySdkManager.destroy()
        super.onDestroy()
    }
}
