package com.example.mod_tan_p2p.ui.activity.connection.localconnection

import com.example.mod_tan_p2p.R
import com.example.mod_tan_p2p.base.BaseFragment
import com.example.mod_tan_p2p.databinding.LocalNetworkConnectionFragmentBinding
import java.net.InetAddress
import java.util.Optional

class LocalNetworkConnectionFragment :
    BaseFragment<LocalNetworkConnectionFragmentBinding, LocalNetworkConnectionFragment.Companion.LocalNetworkState>(
        R.layout.local_network_connection_fragment, LocalNetworkState()
    ) {

    companion object {
        private const val TAG = "LocalNetworkConnectionFragment"

        data class LocalNetworkState(
            val selectedAddress: Optional<InetAddress> = Optional.empty(),
            val availableAddresses: List<InetAddress> = emptyList()
        )
    }
}