package com.example.macnatic_tape_swipe_reader.services

import android.content.Context
import android.os.Bundle
import com.example.macnatic_tape_swipe_reader.features.monitor_logging.AppLog as Log
import com.sunmi.pay.hardware.aidlv2.readcard.CheckCardCallbackV2
import com.sunmi.pay.hardware.aidlv2.readcard.ReadCardOptV2
import sunmi.paylib.SunmiPayKernel

object SunmiPaySdkManager {
    private const val TAG = "SunmiPaySdkManager"

    // ──── State ────
    var isConnected = false
        private set

    var readCardOpt: ReadCardOptV2? = null
        private set

    var initStatus = "Idle"
        private set

    var initError: String? = null
        private set

    private var onConnectedCallback: ((ReadCardOptV2) -> Unit)? = null
    var onDisconnectedCallback: (() -> Unit)? = null

    /**
     * Bind to the Sunmi PaySDK service.
     */
    fun init(context: Context, onConnected: ((ReadCardOptV2) -> Unit)? = null) {
        if (isConnected) {
            Log.i(TAG, "PaySDK already connected ✓")
            readCardOpt?.let { onConnected?.invoke(it) }
            return
        }
        if (initStatus == "Initializing") {
            Log.i(TAG, "PaySDK is already initializing, chaining callbacks.")
            onConnected?.let { newCallback ->
                val existing = onConnectedCallback
                onConnectedCallback = { opt ->
                    existing?.invoke(opt)
                    newCallback(opt)
                }
            }
            return
        }

        initStatus = "Initializing"
        initError = null
        onConnectedCallback = onConnected

        try {
            val kernel = SunmiPayKernel.getInstance()
            val bound = kernel.initPaySDK(context.applicationContext, object : SunmiPayKernel.ConnectCallback {
                override fun onConnectPaySDK() {
                    Log.i(TAG, "PaySDK connected successfully")
                    val opt = kernel.mReadCardOptV2
                    if (opt != null) {
                        readCardOpt = opt
                        isConnected = true
                        initStatus = "Connected"
                        onConnectedCallback?.invoke(opt)
                    } else {
                        val err = "PaySDK connected but ReadCardOptV2 is null"
                        Log.e(TAG, err)
                        readCardOpt = null
                        isConnected = false
                        initStatus = "No Card Reader"
                        initError = err
                    }
                }

                override fun onDisconnectPaySDK() {
                    Log.w(TAG, "PaySDK disconnected")
                    readCardOpt = null
                    isConnected = false
                    initStatus = "Disconnected"
                    onDisconnectedCallback?.invoke()
                }
            })

            if (!bound) {
                val err = "PaySDK service not found — is com.sunmi.pay.hardware_v3 installed?"
                Log.e(TAG, err)
                initStatus = "Service Not Found"
                initError = err
            }
        } catch (e: Exception) {
            val err = "Failed to initialise PaySDK: ${e.message}"
            Log.e(TAG, err, e)
            initStatus = "Init Failed"
            initError = err
        }
    }

    /**
     * Start polling for a magnetic card.
     */
    fun startMsrPolling(
        timeoutSec: Int = 60,
        onCardFound: (bundle: Bundle) -> Unit,
        onError: (code: Int, message: String) -> Unit
    ) {
        cancelMsrPolling()
        val opt = readCardOpt
        if (opt == null) {
            val msg = "ReadCardOptV2 not available — SDK not connected"
            Log.e(TAG, msg)
            onError(-1, msg)
            return
        }

        try {
            val cardType = 1 // AidlConstantsV2.CardType.MAGNETIC.getValue()
            Log.i(TAG, "Starting MSR polling (timeout=${timeoutSec}s)")

            opt.checkCard(cardType, object : CheckCardCallbackV2.Stub() {
                override fun findMagCard(info: Bundle?) {
                    Log.i(TAG, "findMagCard event received")
                    if (info != null) {
                        onCardFound(info)
                    } else {
                        onError(-1, "Received empty card bundle")
                    }
                }

                override fun findICCard(atr: String?) {}
                override fun findRFCard(uuid: String?) {}
                override fun findICCardEx(info: Bundle?) {}
                override fun findRFCardEx(info: Bundle?) {}

                override fun onError(code: Int, message: String?) {
                    Log.e(TAG, "checkCard error: code=$code, msg=$message")
                    onError(code, message ?: "Unknown error")
                }

                override fun onErrorEx(info: Bundle?) {
                    val code = info?.getInt("code") ?: -1
                    val message = info?.getString("message") ?: "Unknown error"
                    Log.e(TAG, "checkCard errorEx: code=$code, msg=$message")
                    onError(code, message)
                }
            }, timeoutSec)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start checkCard MSR polling", e)
            onError(-1, "Exception: ${e.message}")
        }
    }

    /**
     * Cancel checking for a card.
     */
    fun cancelMsrPolling() {
        try {
            readCardOpt?.cancelCheckCard()
        } catch (e: Exception) {
            Log.w(TAG, "Error cancelling MSR polling: ${e.message}")
        }
    }

    /**
     * Destroy binding.
     */
    fun destroy() {
        try {
            cancelMsrPolling()
            SunmiPayKernel.getInstance().destroyPaySDK()
            readCardOpt = null
            isConnected = false
            initStatus = "Destroyed"
            Log.i(TAG, "PaySDK destroyed")
        } catch (e: Exception) {
            Log.w(TAG, "Error destroying PaySDK: ${e.message}")
        }
    }
}
