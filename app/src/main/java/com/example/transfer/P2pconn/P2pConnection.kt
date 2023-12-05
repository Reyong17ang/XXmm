package com.example.transfer.P2pconn

import com.example.transfer.SimpleStateable
import com.example.transfer.extensions.ConnectionServerClientImpl
import com.example.transfer.qrscanconn.SimpleObservable
import com.tans.tfiletransporter.ILog
import com.tans.tfiletransporter.netty.tcp.NettyTcpServerConnectionTask
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.atomic.AtomicReference

class P2pConnection(
    private val currentDeviceName: String,
    private val log: ILog
) : SimpleObservable<P2pConnectionObserver>, SimpleStateable<P2pConnectionState> {

    override val state: AtomicReference<P2pConnectionState> = AtomicReference(P2pConnectionState.NoConnection)

    override val observers: LinkedBlockingDeque<P2pConnectionObserver> by lazy {
        LinkedBlockingDeque()
    }

    private val activeCommunicationNettyTask: AtomicReference<ConnectionServerClientImpl?> by lazy {
        AtomicReference(null)
    }

    private val activeServerNettyTask: AtomicReference<NettyTcpServerConnectionTask?> by lazy {
        AtomicReference(null)
    }
    fun closeConnectionIfActive() {
        activeCommunicationNettyTask.get()?.let {
            it.stopTask()
            activeCommunicationNettyTask.set(null)
        }
        activeServerNettyTask.get()?.let {
            it.stopTask()
            activeServerNettyTask.set(null)
        }
        newState(P2pConnectionState.NoConnection)
        clearObserves()
    }
    companion object {
        private const val TAG = "P2pConnection"
    }
}