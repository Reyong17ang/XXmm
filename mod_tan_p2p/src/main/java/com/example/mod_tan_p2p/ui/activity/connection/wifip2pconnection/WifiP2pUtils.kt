package com.example.mod_tan_p2p.ui.activity.connection.wifip2pconnection

import android.net.wifi.p2p.WifiP2pManager

enum class WifiActionResult(val code: Int) {
    Success(-1),
    Error(WifiP2pManager.ERROR),
    Busy(WifiP2pManager.BUSY),
    Unsupported(WifiP2pManager.P2P_UNSUPPORTED)
}