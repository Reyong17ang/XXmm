package com.example.mod_tan_p2p.ui.activity.connection.wifip2pconnection

import android.annotation.SuppressLint
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pManager
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Optional
import kotlin.coroutines.resume

enum class WifiActionResult(val code: Int) {
    Success(-1),
    Error(WifiP2pManager.ERROR),
    Busy(WifiP2pManager.BUSY),
    Unsupported(WifiP2pManager.P2P_UNSUPPORTED)
}

@SuppressLint("MissingPermission")
suspend fun WifiP2pManager.discoverPeersSuspend(channel: WifiP2pManager.Channel) =
    suspendCancellableCoroutine<WifiActionResult> { cont ->
        discoverPeers(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                if (cont.isActive) {
                    cont.resume(WifiActionResult.Success)
                }
            }

            override fun onFailure(p0: Int) {
                if (cont.isActive) {
                    cont.resume(WifiActionResult.values().first { it.code == p0 })
                }
            }
        })
    }

@SuppressLint("MissingPermission")
suspend fun WifiP2pManager.requestPeersSuspend(channel: WifiP2pManager.Channel) =
    suspendCancellableCoroutine<Optional<WifiP2pDeviceList>> { cont ->
        requestPeers(channel) { cont.resume(Optional.ofNullable(it)) }
    }


