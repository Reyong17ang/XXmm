package com.example.mod_tan_p2p.ui.activity.filetransport

import android.content.Context
import android.content.Intent
import com.example.mod_tan_p2p.R
import com.example.mod_tan_p2p.base.BaseActivity
import com.example.mod_tan_p2p.databinding.FileTransportActivityBinding
import com.tans.tfiletransporter.transferproto.fileexplore.Handshake
import java.net.InetAddress
import java.util.Optional


class FileTransportActivity :
    BaseActivity<FileTransportActivityBinding, FileTransportActivity.Companion.FileTransportActivityState>(
        R.layout.file_transport_activity,
        FileTransportActivityState()
    ) {
    companion object {
        private const val TAG = "FileTransporterActivity"
        private const val LOCAL_ADDRESS_EXTRA_KEY = "local_address_extra_key"
        private const val REMOTE_ADDRESS_EXTRA_KEY = "remote_address_extra_key"
        private const val REMOTE_INFO_EXTRA_KEY = "remote_info_extra_key"
        private const val IS_SERVER_EXTRA_KEY = "is_server_extra_key"

        private fun Intent.getLocalAddress(): InetAddress = getSerializableExtra(
            LOCAL_ADDRESS_EXTRA_KEY
        ) as? InetAddress ?: error("FileTransportActivity get local address fail.")

        private fun Intent.getRemoteAddress(): InetAddress = getSerializableExtra(
            REMOTE_ADDRESS_EXTRA_KEY,
        ) as? InetAddress ?: error("FileTransportActivity get remote address fail.")

        private fun Intent.getRemoteInfo(): String = getStringExtra(REMOTE_INFO_EXTRA_KEY) ?: ""

        private fun Intent.getIsServer(): Boolean = getBooleanExtra(IS_SERVER_EXTRA_KEY, false)
        data class Message(
            val time: Long,
            val msg: String,
            val fromRemote: Boolean
        )

        data class FileTransportActivityState(
            val selectedTabType: DirTabType = DirTabType.MyApps,
            val handshake: Optional<Handshake> = Optional.empty(),
            val messages: List<Message> = emptyList()
        )

        enum class DirTabType {
            MyApps,
            MyImages,
            MyVideos,
            MyAudios,
            MyDir,
            RemoteDir,
            Message
        }

        fun getIntent(
            context: Context,
            localAddress: InetAddress,
            remoteAddress: InetAddress,
            remoteDeviceInfo: String,
            isServer: Boolean
        ): Intent {
            val i = Intent(context, FileTransportActivity::class.java)
            i.putExtra(LOCAL_ADDRESS_EXTRA_KEY, localAddress)
            i.putExtra(REMOTE_ADDRESS_EXTRA_KEY, remoteAddress)
            i.putExtra(REMOTE_INFO_EXTRA_KEY, remoteDeviceInfo)
            i.putExtra(IS_SERVER_EXTRA_KEY, isServer)
            return i
        }
    }
}