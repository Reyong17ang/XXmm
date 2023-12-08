package yj

import com.example.lib_net.netty.findLocalAddressV4
import com.example.lib_net.transferproto.SimpleCallback
import com.example.lib_net.transferproto.p2pconn.P2pConnection
import com.example.lib_net.transferproto.p2pconn.P2pConnectionObserver
import com.example.lib_net.transferproto.p2pconn.P2pConnectionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

object P2pClientTest {
    @JvmStatic
    fun main(args: Array<String>) {
        val localAddress = findLocalAddressV4()[0]
        println(localAddress)
        val p2pConnection = P2pConnection(
            currentDeviceName = "Client_Device",
            log = TestLog
        )
        p2pConnection.addObserver(object : P2pConnectionObserver {

            override fun onNewState(state: P2pConnectionState) {
                println("Client State: $state")
            }

            override fun requestTransferFile(
                handshake: P2pConnectionState.Handshake,
                isReceiver: Boolean
            ) {
            }
        })
        p2pConnection.connect(
            serverAddress = localAddress,
            simpleCallback = object : SimpleCallback<Unit> {
                override fun onSuccess(data: Unit) {
                    println("Client connection success.")
                }

                override fun onError(errorMsg: String) {
                    println("Client connection fail: $errorMsg")
                }
            }
        )
        runBlocking {
            delay(60 * 1000 * 5)
        }
    }
}