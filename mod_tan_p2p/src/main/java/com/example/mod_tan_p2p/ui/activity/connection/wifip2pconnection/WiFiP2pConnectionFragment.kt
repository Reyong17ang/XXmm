package com.example.mod_tan_p2p.ui.activity.connection.wifip2pconnection

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.os.Bundle
import android.view.View
import com.example.mod_tan_p2p.R
import com.example.mod_tan_p2p.base.BaseFragment
import com.example.mod_tan_p2p.databinding.WifiP2pConnectionFragmentBinding
import com.example.mod_tan_p2p.utils.AndroidLog
import com.jakewharton.rxbinding4.view.clicks
import com.tans.rxutils.ignoreSeveralClicks
import com.tans.tfiletransporter.transferproto.p2pconn.P2pConnection
import com.tans.tfiletransporter.transferproto.p2pconn.transferFileSuspend
import io.reactivex.rxjava3.kotlin.withLatestFrom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.await
import kotlinx.coroutines.rx3.rxSingle
import java.net.InetAddress
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

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
            render({ it.p2pHandshake to it.connectionStatus }) { (handShake, status) ->
                if (handShake.isPresent) {
                    binding.localAddressTv.text = getString(
                        R.string.wifi_p2p_connection_local_address,
                        handShake.get().localAddress.toString().removePrefix("/")
                    )
                } else {
                    binding.localAddressTv.text =
                        getString(R.string.wifi_p2p_connection_local_address, "Not Available")
                    binding.remoteConnectedDeviceTv.text = getString(
                        R.string.wifi_p2p_connection_remote_device,
                        "Not Available", "Not Available", status.toString()
                    )
                }
            }.bindLife()

            render({ it.wifiP2PConnection to it.p2pHandshake }) { (wifiP2pConnection, handshake) ->
                if (wifiP2pConnection.isPresent) {
                    binding.connectedActionsLayout.visibility = View.VISIBLE
                    binding.remoteDevicesRv.visibility = View.GONE
                    if (handshake.isPresent) {
                        binding.transferFileLayout.visibility = View.VISIBLE
                    } else {
                        binding.transferFileLayout.visibility = View.INVISIBLE
                    }
                } else {
                    binding.connectedActionsLayout.visibility = View.GONE
                    binding.remoteDevicesRv.visibility = View.VISIBLE
                }
            }.bindLife()

            launch(Dispatchers.IO) {
                while (true) {
                    val (isP2pEnabled, connection) = bindState().map { it.isP2pEnabled to it.wifiP2PConnection.getOrNull() }
                        .firstOrError().await()
                    if (isResumed && isVisible) {
                        if (!isP2pEnabled) {
                            updateState { oldState -> oldState.copy(peers = emptyList()) }.await()
                        } else {
                            if (connection == null) {
                                val state = wifiP2pManager.discoverPeersSuspend(wifiChannel)
                                if (state == WifiActionResult.Success) {
                                    AndroidLog.d(TAG, "Request discover peer success")
                                    val peers =
                                        wifiP2pManager.requestPeersSuspend(channel = wifiChannel)
                                    AndroidLog.d(
                                        TAG,
                                        "WIFI p2p devices: ${peers.orElseGet { null }?.deviceList?.joinToString { "${it.deviceName} -> ${it.deviceAddress}" }}"
                                    )
                                    updateState { oldState ->
                                        oldState.copy(peers = peers.getOrNull()?.deviceList?.map {
                                            P2pPeer(
                                                it.deviceName,
                                                it.deviceAddress
                                            )
                                        } ?: emptyList())
                                    }.await()
                                } else {
                                    updateState { oldState -> oldState.copy(peers = emptyList()) }.await()
                                    AndroidLog.e(TAG, "Request discover peer fail: $state")
                                }
                            } else {
                                updateState { oldState -> oldState.copy(peers = emptyList()) }.await()
                            }
                        }
                    }
                    delay(4000)
                }
            }

            binding.transferFileLayout.clicks()
                .ignoreSeveralClicks(duration = 1000)
                .withLatestFrom(bindState().map { it.p2pHandshake })
                .switchMapSingle { (_, handshake) ->
                    rxSingle(Dispatchers.IO) {
                        val connection = handshake.getOrNull()?.p2pConnection
                        if (connection == null) {
                            AndroidLog.e(TAG, "Request transfer file fail: handshake is null.")
                        } else {
                            val requestTransferResult = runCatching {
                                connection.transferFileSuspend()
                            }
                            if (requestTransferResult.isFailure) {
                                AndroidLog.e(
                                    TAG,
                                    "Request transfer file fail: ${requestTransferResult.exceptionOrNull()?.message}"
                                )
                            } else {
                                AndroidLog.d(TAG, "Request transfer file success.")
                            }
                        }
                    }
                }.bindLife()

        }
    }

    override fun onResume() {
        super.onResume()
        binding.connectedActionsLayout.visibility = View.GONE
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