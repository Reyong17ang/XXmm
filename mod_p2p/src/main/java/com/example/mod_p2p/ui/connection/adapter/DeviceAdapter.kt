package com.example.mod_p2p.ui.connection.adapter

import android.net.wifi.p2p.WifiP2pDevice
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.mod_p2p.base.BaseBindViewHolder
import com.example.mod_p2p.base.BaseRecyclerViewAdapter
import com.example.mod_p2p.databinding.ItemP2pDeviceBinding
import com.example.mod_p2p.utils.WifiP2pUtils

class DeviceAdapter : BaseRecyclerViewAdapter<WifiP2pDevice,ItemP2pDeviceBinding>() {


    override fun getViewBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemP2pDeviceBinding {
            return ItemP2pDeviceBinding.inflate(layoutInflater,parent,false)
    }
    override fun onBindDefViewHolder(
        holder: BaseBindViewHolder<ItemP2pDeviceBinding>,
        item: WifiP2pDevice?,
        position: Int
    ) {
        with(holder.binding) {
            item?.let {
                tvDeviceName.text = it.deviceName
                tvDeviceAddress.text = it.deviceAddress
                tvDeviceDetails.text = WifiP2pUtils.getDeviceStatus(deviceStatus = it.status)
            }
        }
    }


}
