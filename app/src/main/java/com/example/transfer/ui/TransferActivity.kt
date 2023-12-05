package com.example.transfer.ui

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.os.Bundle
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.example.mod_p2p.ext.onClick
import com.example.mydemo.R
import com.example.mydemo.databinding.ActivityTransferBinding
import com.example.transfer.P2pconn.P2pConnection
import com.example.transfer.core.BaseActivity
import com.example.transfer.utils.WifiActionResult
import com.example.transfer.utils.connectSuspend
import com.example.transfer.utils.discoverPeersSuspend
import com.example.transfer.utils.requestConnectionInfoSuspend
import com.example.transfer.utils.requestPeersSuspend
import com.permissionx.guolindev.PermissionX
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.await
import java.net.InetAddress
import java.util.Optional
import kotlin.jvm.optionals.getOrNull


class TransferActivity :
    BaseActivity<ActivityTransferBinding, TransferActivity.Companion.WifiP2pConnectionState>(
        R.layout.activity_transfer, WifiP2pConnectionState()
    ) {
    private val wifiP2pManager: WifiP2pManager by lazy {
        getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
    }
    private val wifiChannel: WifiP2pManager.Channel by lazy {
        wifiP2pManager.initialize(
            this, this.mainLooper, null
        )
    }

    fun String.addNextLine(append: String): String {
        return this + "\n" + append
    }

    private val wifiReceiver: BroadcastReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {

                WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                    val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                    if (state == WifiP2pManager.WIFI_P2P_STATE_DISABLED) {
                        LogUtils.e(TAG, "Wifi p2p disabled.")
                        updateState { WifiP2pConnectionState() }.bindLife()
                    } else {
                        LogUtils.d(TAG, "Wifi p2p enabled.")
                        updateState { it.copy(isP2pEnabled = true) }.bindLife()
                    }
                }

                WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                    val wifiDevicesList =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(
                                WifiP2pManager.EXTRA_P2P_DEVICE_LIST, WifiP2pDeviceList::class.java
                            )
                        } else {
                            intent.getParcelableExtra(WifiP2pManager.EXTRA_P2P_DEVICE_LIST)
                        }
                    LogUtils.d(
                        TAG,
                        "WIFI p2p devices: ${wifiDevicesList?.deviceList?.joinToString { "${it.deviceName} -> ${it.deviceAddress}" }}"
                    )
                    updateState { oldState ->
                        oldState.copy(peers = wifiDevicesList?.deviceList?.map {
                            P2pPeer(
                                it.deviceName, it.deviceAddress
                            )
                        } ?: emptyList())
                    }.bindLife()
                }

                WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                    LogUtils.d(TAG, "Connection state change.")
                    launch {
                        checkWifiConnection()
                    }
                }

                WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                    // Android 10 can't get mac address.
                }
            }
        }
    }

    override fun firstLaunchInitData() {
        launch {
            val permissionNeed = mutableListOf<String>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionNeed.add(Manifest.permission.READ_MEDIA_IMAGES)
                    permissionNeed.add(Manifest.permission.READ_MEDIA_AUDIO)
                    permissionNeed.add(Manifest.permission.READ_MEDIA_VIDEO)
                } else {
                    permissionNeed.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            } else {
                permissionNeed.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                permissionNeed.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionNeed.add(Manifest.permission.NEARBY_WIFI_DEVICES)
            } else {
                permissionNeed.add(Manifest.permission.ACCESS_FINE_LOCATION)
                permissionNeed.add(Manifest.permission.ACCESS_COARSE_LOCATION)
            }
            PermissionX.init(this@TransferActivity).permissions(permissionNeed)
                .request { allGranted, grantedList, deniedList ->
                    if (allGranted) {
                        ToastUtils.showShort("All permissions are granted")
                    } else {
                        ToastUtils.showShort("These permissions are denied: $deniedList")
                    }
                }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intentFilter = IntentFilter().apply {
            addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
        }
        registerReceiver(wifiReceiver, intentFilter)
    }

    var peers: Optional<WifiP2pDeviceList>? = null
    override fun initViews(binding: ActivityTransferBinding) {
        launch {
//            render({ it.p2pHandshake to it.connectionStatus }) { (handShake, status) ->
//                binding.tvSendContent.apply {
//                    text = text.toString().addNextLine(
//                        getString(
//                            R.string.wifi_p2p_connection_local_address,
//                            handShake.get().localAddress.toString().removePrefix("/")
//                        )
//                    )
//                }
//            }

            render({ it.peers }) { peers ->
                binding.tvSendContent.apply {
                    peers.forEach {
                        text = text.toString().addNextLine(
                            "$it.deviceName---${it.macAddress}"
                        )
                        ToastUtils.showShort(text)
                    }
                }
            }
        }
        launch(Dispatchers.IO) {
            while (true) {
                val (isP2pEnabled, connection) = bindState().map { it.isP2pEnabled to it.wifiP2PConnection.getOrNull() }
                    .firstOrError().await()
                if (!isP2pEnabled) {
                    updateState { oldState -> oldState.copy(peers = emptyList()) }.await()
                } else {
                    if (connection == null) {
                        val state = wifiP2pManager.discoverPeersSuspend(wifiChannel)
                        LogUtils.d(state)
                        if (state == WifiActionResult.Success) {
                            peers = wifiP2pManager.requestPeersSuspend(channel = wifiChannel)
                            LogUtils.d(peers)
                            updateState { oldState ->
                                oldState.copy(peers = peers!!.getOrNull()?.deviceList?.map {
                                    P2pPeer(
                                        it.deviceName, it.deviceAddress
                                    )
                                } ?: emptyList())
                            }.await()
                        } else {
                            updateState { oldState -> oldState.copy(peers = emptyList()) }.await()
                            LogUtils.d(TAG, "Request discover peer fail: $state")
                        }
                    } else {
                        updateState { oldState -> oldState.copy(peers = emptyList()) }.await()
                    }
                }
                delay(3000)
            }
        }
        binding.btnSend.onClick {
            launch {
                val config = WifiP2pConfig()
                peers?.getOrNull()?.deviceList?.forEach {
                    LogUtils.d(it.deviceName, it.deviceAddress)
                }
                peers?.getOrNull()?.deviceList?.first().apply {
                    config.deviceAddress = this?.deviceAddress
                }
                val state = wifiP2pManager.connectSuspend(wifiChannel, config)
                if (state == WifiActionResult.Success) {
                    LogUtils.d(TAG, "Request P2P connection success !!!")
                    val connectionInfo = wifiP2pManager.requestConnectionInfoSuspend(wifiChannel).getOrNull()
                    LogUtils.d(TAG, "Connection group address: ${connectionInfo?.groupOwnerAddress}, is group owner: ${connectionInfo?.isGroupOwner}")
                } else {
                    updateState { it.copy(connectionStatus = ConnectionStatus.NoConnection) }.await()
                    LogUtils.e(TAG, "Request P2P connection fail: $state !!!")
                }
            }
        }
        binding.btnReceive.onClick {

        }
    }

    private suspend fun checkWifiConnection(): WifiP2pConnection? {
        val connectionOld =
            bindState().map { it.wifiP2PConnection }.firstOrError().await().getOrNull()
        val connectionNew =
            wifiP2pManager.requestConnectionInfoSuspend(wifiChannel).getOrNull()?.let {
                WifiP2pConnection(
                    isGroupOwner = it.isGroupOwner, groupOwnerAddress = it.groupOwnerAddress
                )
            }
        LogUtils.d(
            TAG,
            "Connection group address: ${connectionNew?.groupOwnerAddress}, is group owner: ${connectionNew?.isGroupOwner}"
        )
        if (connectionNew != connectionOld) {
            updateState {
                it.copy(
                    wifiP2PConnection = Optional.ofNullable(connectionNew),
                    connectionStatus = if (connectionNew == null) {
                        ConnectionStatus.NoConnection
                    } else {
                        it.connectionStatus
                    }
                )
            }.await()
        }
        return connectionNew
    }

    override fun onDestroy() {
        super.onDestroy()
        Dispatchers.IO.asExecutor().execute {
            try {
                bindState().firstOrError()
                    .blockingGet().p2pHandshake.getOrNull()?.p2pConnection?.closeConnectionIfActive()
            } catch (_: Throwable) {

            }
        }
        wifiP2pManager.cancelConnect(wifiChannel, null)
        wifiP2pManager.removeGroup(wifiChannel, null)
        this.unregisterReceiver(wifiReceiver)

    }

    companion object {
        private const val TAG = "TransferActivity"

        data class P2pPeer(
            val deviceName: String,
            val macAddress: String,
        )

        data class WifiP2pConnection(
            val isGroupOwner: Boolean, val groupOwnerAddress: InetAddress
        )

        data class P2pHandshake(
            val p2pConnection: P2pConnection,
            val localAddress: InetAddress,
            val remoteAddress: InetAddress,
            val remoteDeviceName: String
        )

        enum class ConnectionStatus {
            NoConnection, Connecting, Handshaking, Connected
        }

        data class WifiP2pConnectionState(
            val isP2pEnabled: Boolean = false,
            val peers: List<P2pPeer> = emptyList(),
            val wifiP2PConnection: Optional<WifiP2pConnection> = Optional.empty(),
            val p2pHandshake: Optional<P2pHandshake> = Optional.empty(),
            val connectionStatus: ConnectionStatus = ConnectionStatus.NoConnection
        )
    }

}