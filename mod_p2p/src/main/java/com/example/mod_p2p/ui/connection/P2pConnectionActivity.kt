package com.example.mod_p2p.ui.connection

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.blankj.utilcode.util.UriUtils
import com.example.mod_p2p.DirectActionListener
import com.example.mod_p2p.DirectBroadcastReceiver
import com.example.mod_p2p.base.BaseMvvmActivity
import com.example.mod_p2p.databinding.ActivityP2pConnectionBinding
import com.example.mod_p2p.ext.addStrings
import com.example.mod_p2p.ext.onClick
import com.example.mod_p2p.ext.plusInt
import com.example.mod_p2p.model.ViewState
import com.example.mod_p2p.ui.connection.adapter.DeviceAdapter
import com.example.mod_p2p.ui.connection.viewmodel.P2pViewModel
import com.example.mod_p2p.utils.WifiP2pUtils
import com.permissionx.guolindev.PermissionX
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Timer
import java.util.TimerTask
import kotlin.coroutines.resume

class P2pConnectionActivity : BaseMvvmActivity<ActivityP2pConnectionBinding, P2pViewModel>() {

    private var connectionInfoAvailable = false
    private var broadcastReceiver: BroadcastReceiver? = null
    private var wifiP2pInfo: WifiP2pInfo? = null
    private var wifiP2pEnabled = false
    private val wifiP2pDeviceList = mutableListOf<WifiP2pDevice>()
    private val deviceAdapter = DeviceAdapter()
    private var receiveFileSize = ""

    private val wifiP2pManager: WifiP2pManager by lazy {
        getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
    }

    private val wifiP2pChannel: WifiP2pManager.Channel by lazy {
        wifiP2pManager.initialize(
            this, this.mainLooper, null
        )
    }
    private val getContentLaunch = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { imageUri ->
        if (imageUri != null) {
            val ipAddress = wifiP2pInfo?.groupOwnerAddress?.hostAddress
            LogUtils.d("getContentLaunch $imageUri $ipAddress")
            if (!ipAddress.isNullOrBlank()) {
                mViewModel.send(
                    ipAddress = ipAddress,
                    fileUri = imageUri
                )
            }
            val uri2File = UriUtils.uri2File(imageUri)
            LogUtils.d("FileSize${FileUtils.getSize(uri2File)}----${FileUtils.getLength(uri2File)}----")
            receiveFileSize = FileUtils.getSize(uri2File)
        }
    }

    private val directActionListener = object : DirectActionListener {
        override fun wifiP2pEnabled(enabled: Boolean) {
            wifiP2pEnabled = enabled
        }


        override fun onPeersAvailable(wifiP2pDeviceList: Collection<WifiP2pDevice>) {
            dismissLoading()
            LogUtils.d("onPeersAvailable :" + wifiP2pDeviceList.size + wifiP2pDeviceList)
//            this@P2pConnectionActivity.wifiP2pDeviceList.clear()
//            this@P2pConnectionActivity.wifiP2pDeviceList.addAll(wifiP2pDeviceList)
//            deviceAdapter.notifyDataSetChanged()
            mViewModel.updateWifiP2pDeviceList(wifiP2pDeviceList)
        }

        override fun onConnectionInfoAvailable(wifiP2pInfo: WifiP2pInfo) {
            dismissLoading()
//            wifiP2pDeviceList.clear()
//            deviceAdapter.notifyDataSetChanged()
            LogUtils.d("onConnectionInfoAvailable")
            LogUtils.d("onConnectionInfoAvailable groupFormed: " + wifiP2pInfo.groupFormed)
            LogUtils.d("onConnectionInfoAvailable isGroupOwner: " + wifiP2pInfo.isGroupOwner)
            LogUtils.d("onConnectionInfoAvailable getHostAddress: " + wifiP2pInfo.groupOwnerAddress.hostAddress)
            val stringBuilder = StringBuilder()
            stringBuilder.append("\n")
            stringBuilder.append("是否群主：")
            stringBuilder.append(if (wifiP2pInfo.isGroupOwner) "是群主" else "非群主")
            stringBuilder.append("\n")
            stringBuilder.append("群主IP地址：")
            stringBuilder.append(wifiP2pInfo.groupOwnerAddress.hostAddress)
            if (wifiP2pInfo.groupFormed && !wifiP2pInfo.isGroupOwner) {
                this@P2pConnectionActivity.wifiP2pInfo = wifiP2pInfo
            }
//            startActivity(Intent(this@P2pConnectionActivity, P2pTransferActivity::class.java))
        }

        override fun onSelfDeviceAvailable(wifiP2pDevice: WifiP2pDevice) {
            LogUtils.d("onSelfDeviceAvailable")
            LogUtils.d("DeviceName: " + wifiP2pDevice.deviceName)
            LogUtils.d("DeviceAddress: " + wifiP2pDevice.deviceAddress)
            LogUtils.d("Status: " + wifiP2pDevice.status)
            val peersInfo =
                "deviceName：" + wifiP2pDevice.deviceName + "\n" + "deviceAddress：" + wifiP2pDevice.deviceAddress + "\n" + "deviceStatus：" + WifiP2pUtils.getDeviceStatus(
                    wifiP2pDevice.status
                )
            if (wifiP2pDevice.status == WifiP2pDevice.CONNECTED) {
                mViewModel.startListener()
                LogUtils.d("onSelfDeviceAvailable+Socket开始监听,可以发送文件")
            }
            LogUtils.d(peersInfo)
        }

        override fun onDisconnection() {
            LogUtils.d("onDisconnection")
            wifiP2pDeviceList.clear()
            deviceAdapter.notifyDataSetChanged()
            wifiP2pInfo = null
//            ToastUtils.showShort("处于非连接状态")
        }

        override fun onChannelDisconnected() {
            LogUtils.d("onChannelDisconnected")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermissions()
        startObserve()
        broadcastReceiver =
            DirectBroadcastReceiver(wifiP2pManager, wifiP2pChannel, directActionListener)
        registerReceiver(broadcastReceiver, DirectBroadcastReceiver.getIntentFilter())
        initEvent()
        createGroup()
        scanPeersWhenResume()
    }

    override fun initView(savedInstanceState: Bundle?) {
        mBinding.rvDevices.apply {
            itemAnimator = DefaultItemAnimator()
            layoutManager =
                LinearLayoutManager(this@P2pConnectionActivity, RecyclerView.VERTICAL, false)
            adapter = deviceAdapter.apply {
                onItemClickListener = { _: View, position: Int ->
                    val wifiP2pDevice = mViewModel.wifiP2pDeviceList.value?.getOrNull(position)
                    if (wifiP2pDevice != null) {
                        interruptWhenStop()
                        connect(wifiP2pDevice)
                    }
                }
            }
        }
    }


    private fun startObserve() {
        mViewModel.wifiP2pDeviceList.observe(this, Observer {
            deviceAdapter.setData(mViewModel.wifiP2pDeviceList.value)
            mBinding.tvTop0.text = mViewModel.wifiP2pDeviceList.value?.size.toString()
        })
    }

    @SuppressLint("MissingPermission")
    private fun startScanPeers() {
        lifecycleScope.launch {
            if (wifiP2pEnabled) {
//                showLoading("正在搜索设备")
//                wifiP2pDeviceList.clear()
//                deviceAdapter.notifyDataSetChanged()
                wifiP2pManager.discoverPeers(wifiP2pChannel,
                    object : WifiP2pManager.ActionListener {
                        override fun onSuccess() {
                            LogUtils.d("WifiP2pManager.ActionListener.onSuccess")
//                            ToastUtils.showShort("discoverPeers Success")
                        }

                        override fun onFailure(reasonCode: Int) {
                            LogUtils.d("WifiP2pManager.ActionListener.onFailure")
//                            ToastUtils.showShort("discoverPeers Failure：$reasonCode")
                            dismissLoading()
                        }
                    })
            }
        }
    }

    override fun initListener() {
        mBinding.tvText.onClick {
            startScanPeers()
        }
        mBinding.tvChooseFile.onClick {
            getContentLaunch.launch("*/*")
        }
        mBinding.tvCancel.onClick {

        }
        mBinding.tvListen.onClick {
            mViewModel.startListener()
        }
        mBinding.tvCreateGroup.onClick {
            createGroup()
        }
        mBinding.tvCancelGroup.onClick {
            removeGroup()
        }
        mBinding.ivRotateLight.onClick {
            disconnect()
            removeGroup()
            mViewModel.updateWifiP2pDeviceList(emptyList())
            deviceAdapter.notifyDataSetChanged()
            startScanPeers()
        }
        mBinding.tvBottom0.onClick {
            startScanPeers()
        }
        mBinding.tvBottom1.onClick {
            getContentLaunch.launch("*/*")
        }
        mBinding.tvBottom2.onClick {
            createGroup()
        }
    }

    private fun initEvent() {
        lifecycleScope.launch {
            mViewModel.viewState.collect {
                when (it) {
                    ViewState.Idle -> {
                        dismissLoading()
                    }

                    ViewState.Connecting -> {
//                        showLoading("文件连接中...")
                    }

                    is ViewState.Receiving -> {
                        showLoading("文件传输中...")
                    }

                    is ViewState.Success -> {
                        dismissLoading()
                        ToastUtils.showLong("文件将保存到${mViewModel.getStorageDir(this@P2pConnectionActivity)}")
                        mBinding.tvTop1.apply {
                            text = text.toString().plusInt(1)
                            LogUtils.d("Test" + text.toString().plusInt(1))
                        }
                        mBinding.tvTop2.apply {
                            text = addStrings(text.toString(), receiveFileSize)
                            LogUtils.d("Test" , (text.toString()))
                            LogUtils.d("Test" , (receiveFileSize))
                            LogUtils.d("Test" , (addStrings(text.toString(), receiveFileSize)))
                        }
                    }

                    is ViewState.Failed -> {
                        dismissLoading()
                    }
                }
            }
        }
        lifecycleScope.launch {
            mViewModel.log.collect {
                LogUtils.d(it)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun connect(wifiP2pDevice: WifiP2pDevice) {
        val wifiP2pConfig = WifiP2pConfig()
        wifiP2pConfig.deviceAddress = wifiP2pDevice.deviceAddress
        wifiP2pConfig.wps.setup = WpsInfo.PBC
        showLoading("正在连接，deviceName: " + wifiP2pDevice.deviceName)
        ToastUtils.showShort("正在连接，deviceName: " + wifiP2pDevice.deviceName)
        wifiP2pManager.connect(wifiP2pChannel, wifiP2pConfig,
            object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    LogUtils.d("connect onSuccess")
                }

                override fun onFailure(reason: Int) {
                    ToastUtils.showShort("连接失败 $reason")
                    dismissLoading()
                }
            })
    }

    private fun disconnect() {
        wifiP2pManager.cancelConnect(wifiP2pChannel, object : WifiP2pManager.ActionListener {
            override fun onFailure(reasonCode: Int) {
                LogUtils.d("cancelConnect onFailure:$reasonCode")
            }

            override fun onSuccess() {
                LogUtils.d("cancelConnect onSuccess")
            }
        })
        wifiP2pManager.removeGroup(wifiP2pChannel, null)
    }

    @SuppressLint("MissingPermission")
    private fun createGroup() {
        lifecycleScope.launch {
            removeGroupIfNeed()
            wifiP2pManager.createGroup(wifiP2pChannel, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    val log = "createGroup onSuccess"
                    LogUtils.d(log)
                    ToastUtils.showShort(log)
                }

                override fun onFailure(reason: Int) {
                    val log = "createGroup onFailure: $reason"
                    LogUtils.d(log)
                    ToastUtils.showShort(log)
                }
            })
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun removeGroupIfNeed() {
        return suspendCancellableCoroutine { continuation ->
            wifiP2pManager.requestGroupInfo(wifiP2pChannel) { group ->
                if (group == null) {
                    continuation.resume(value = Unit)
                } else {
                    wifiP2pManager.removeGroup(wifiP2pChannel,
                        object : WifiP2pManager.ActionListener {
                            override fun onSuccess() {
                                val log = "removeGroup onSuccess"
                                LogUtils.d(log)
                                ToastUtils.showShort(log)
                                continuation.resume(value = Unit)
                            }

                            override fun onFailure(reason: Int) {
                                val log = "removeGroup onFailure: $reason"
                                LogUtils.d(log)
                                ToastUtils.showShort(log)
                                continuation.resume(value = Unit)
                            }
                        })
                }
            }
        }
    }

    private fun removeGroup() {
        lifecycleScope.launch {
            removeGroupIfNeed()
        }
    }

    var timer: Timer? = null

    private fun scanPeersWhenResume() {
        lifecycleScope.launch {
            if (timer == null) {
                timer = Timer()
                val timerTask = object : TimerTask() {
                    override fun run() {
                        startScanPeers()
                    }
                }
                timer?.schedule(timerTask, 0, 4000)
            }
        }
    }

    private fun interruptWhenStop() {
        disconnect()
        removeGroup()
        mViewModel.updateWifiP2pDeviceList(emptyList())
    }

    private fun checkPermissions() {
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
            permissionNeed.add(Manifest.permission.READ_EXTERNAL_STORAGE )
            permissionNeed.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionNeed.add(Manifest.permission.NEARBY_WIFI_DEVICES)
        } else {
            permissionNeed.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissionNeed.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        PermissionX.init(this@P2pConnectionActivity).permissions(permissionNeed)
            .onExplainRequestReason { scope, deniedList ->
                scope.showRequestReasonDialog(
                    deniedList, "要让WI-FI直连正常工作，一定得打开WI-FI和GPS权限", "我已了解", "取消"
                )
            }.onForwardToSettings { scope, deniedList ->
                scope.showForwardToSettingsDialog(
                    deniedList, "您需要去应用程序设置当中手动开启权限", "我已明白", "取消"
                )
            }.request { allGranted, grantedList, deniedList ->
                if (allGranted) {
                    ToastUtils.showShort("所有申请的权限都已通过")
                } else {
                    ToastUtils.showShort("您拒绝了如下权限：$deniedList")
                }
            }
    }


    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
        timer = null
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver)
        }
        interruptWhenStop()

    }
}