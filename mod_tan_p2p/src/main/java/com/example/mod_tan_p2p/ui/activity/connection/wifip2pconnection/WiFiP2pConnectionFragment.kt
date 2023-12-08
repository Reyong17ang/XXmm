package com.example.mod_tan_p2p.ui.activity.connection.wifip2pconnection

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.os.Bundle
import com.example.mod_tan_p2p.R
import com.example.mod_tan_p2p.base.BaseFragment
import com.example.mod_tan_p2p.databinding.WifiP2pConnectionFragmentBinding
import com.example.mod_tan_p2p.utils.AndroidLog
import com.tans.tfiletransporter.transferproto.p2pconn.P2pConnection
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.await
import java.net.InetAddress
import java.util.Optional

class WiFiP2pConnectionFragment :
    BaseFragment<WifiP2pConnectionFragmentBinding, WiFiP2pConnectionFragment.Companion.WifiP2pConnectionState>(
        R.layout.wifi_p2p_connection_fragment, WifiP2pConnectionState()
    ) {
    private val wifiP2pManager: WifiP2pManager by lazy {
        context?.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
    }
    private val wifiChannel: WifiP2pManager.Channel by lazy {
        wifiP2pManager.initialize(
            requireActivity(),
            requireActivity().mainLooper,
            null
        )
    }
    private val wifiReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                    val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                    if (state == WifiP2pManager.WIFI_P2P_STATE_DISABLED) {
                        AndroidLog.e(TAG, "Wifi p2p disabled.")
                        updateState { WifiP2pConnectionState() }.bindLife()
                    } else {
                        AndroidLog.d(TAG, "Wifi p2p enabled.")
                        updateState { it.copy(isP2pEnabled = true) }.bindLife()
                    }
                }

                WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                    val wifiDevicesList =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(
                                WifiP2pManager.EXTRA_P2P_DEVICE_LIST,
                                WifiP2pDeviceList::class.java
                            )
                        } else {
                            intent.getParcelableExtra(WifiP2pManager.EXTRA_P2P_DEVICE_LIST)
                        }
                    AndroidLog.d(
                        TAG,
                        "WIFI p2p devices: ${wifiDevicesList?.deviceList?.joinToString { "${it.deviceName} -> ${it.deviceAddress}" }}"
                    )
                    updateState { oldState ->
                        oldState.copy(peers = wifiDevicesList?.deviceList?.map {
                            P2pPeer(
                                it.deviceName,
                                it.deviceAddress
                            )
                        } ?: emptyList())
                    }.bindLife()
                }

                WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                    AndroidLog.d(TAG, "Connection state change.")
                    launch {
//                        checkWifiConnection()
                    }
                }

                WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {}
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
        requireActivity().registerReceiver(wifiReceiver, intentFilter)
    }

    override fun initViews(binding: WifiP2pConnectionFragmentBinding) {
        launch {
            launch {
                closeCurrentWifiConnection()
            }
        }
    }

    private suspend fun closeCurrentWifiConnection() {
        updateState { oldState ->
            oldState.copy(
                wifiP2PConnection = Optional.empty(),
                connectionStatus = ConnectionStatus.NoConnection
            )
        }.await()
//        wifiP2pManager.cancelConnectionSuspend(wifiChannel)
//        wifiP2pManager.removeGroupSuspend(wifiChannel)
    }

//    private suspend fun checkWifiConnection(): WifiP2pConnection? {
//
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        wifiP2pManager.cancelConnect(wifiChannel, null)
        wifiP2pManager.removeGroup(wifiChannel, null)
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().unregisterReceiver(wifiReceiver)
    }

    companion object {
        private const val TAG = "WifiP2pConn"

        data class P2pPeer(
            val deviceName: String,
            val macAddress: String,
        )

        data class WifiP2pConnection(
            val isGroupOwner: Boolean,
            val groupOwnerAddress: InetAddress
        )

        data class P2pHandshake(
            val p2pConnection: P2pConnection,
            val localAddress: InetAddress,
            val remoteAddress: InetAddress,
            val remoteDeviceName: String
        )

        enum class ConnectionStatus {
            NoConnection,
            Connecting,
            Handshaking,
            Connected
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