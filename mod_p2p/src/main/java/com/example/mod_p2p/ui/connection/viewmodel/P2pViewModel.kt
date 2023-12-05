package com.example.mod_p2p.ui.connection.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.net.wifi.p2p.WifiP2pDevice
import androidx.core.net.toFile
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.PathUtils
import com.example.mod_p2p.Constants
import com.example.mod_p2p.model.FileTransfer
import com.example.mod_p2p.model.ViewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import kotlin.random.Random

class P2pViewModel(context: Application) : AndroidViewModel(context) {

    private val _viewState = MutableSharedFlow<ViewState>()

    val viewState: SharedFlow<ViewState> = _viewState

    private val _log = MutableSharedFlow<String>()

    val log: SharedFlow<String> = _log

    private var job: Job? = null
    private val _wifiP2pDeviceList = MutableLiveData<List<WifiP2pDevice>>()
    val wifiP2pDeviceList: LiveData<List<WifiP2pDevice>> get() = _wifiP2pDeviceList

    // 在适当的地方更新_wifiP2pDeviceList的值

    fun updateWifiP2pDeviceList(devices: Collection<WifiP2pDevice>) {
        _wifiP2pDeviceList.value = devices.toList()
    }

    fun send(ipAddress: String, fileUri: Uri) {
        if (job != null) {
            return
        }
        job = viewModelScope.launch {
            withContext(context = Dispatchers.IO) {

                _viewState.emit(value = ViewState.Idle)

                var socket: Socket? = null
                var outputStream: OutputStream? = null
                var objectOutputStream: ObjectOutputStream? = null
                var fileInputStream: FileInputStream? = null
                try {
                    val cacheFile =
                        saveFileToCacheDir(context = getApplication(), fileUri = fileUri)
                    val fileTransfer = FileTransfer(fileName = cacheFile.name)

                    _viewState.emit(value = ViewState.Connecting)
                    _log.emit(value = "待发送的文件: $fileTransfer")
                    _log.emit(value = "开启 Socket")

                    socket = Socket()
                    socket.bind(null)

                    _log.emit(value = "socket connect，如果三十秒内未连接成功则放弃")

                    socket.connect(InetSocketAddress(ipAddress, Constants.PORT), 30000)

                    _viewState.emit(value = ViewState.Receiving)
                    _log.emit(value = "连接成功，开始传输文件")

                    outputStream = socket.getOutputStream()
                    objectOutputStream = ObjectOutputStream(outputStream)
                    objectOutputStream.writeObject(fileTransfer)
                    fileInputStream = FileInputStream(cacheFile)
                    val buffer = ByteArray(1024 * 100)
                    var length: Int
                    while (true) {
                        length = fileInputStream.read(buffer)
                        if (length > 0) {
                            outputStream.write(buffer, 0, length)
                        } else {
                            break
                        }
                        _log.emit(value = "正在传输文件，length : $length")
                    }
                    _log.emit(value = "文件发送成功")
                    _viewState.emit(value = ViewState.Success(file = cacheFile))
                } catch (e: Throwable) {
                    e.printStackTrace()
                    _log.emit(value = "异常: " + e.message)
                    _viewState.emit(value = ViewState.Failed(throwable = e))
                } finally {
                    fileInputStream?.close()
                    outputStream?.close()
                    objectOutputStream?.close()
                    socket?.close()
                }
            }
        }
        job?.invokeOnCompletion {
            job = null
        }
    }

    private suspend fun saveFileToCacheDir(context: Context, fileUri: Uri): File {
        return withContext(context = Dispatchers.IO) {
            val documentFile = DocumentFile.fromSingleUri(context, fileUri)
                ?: throw NullPointerException("fileName for given input Uri is null")
            val fileName = documentFile.name
            val outputFile = File(
                context.cacheDir, Random.nextInt(
                    1,
                    200
                ).toString() + "_" + fileName
            )
            if (outputFile.exists()) {
                outputFile.delete()
            }
            outputFile.createNewFile()
            val outputFileUri = Uri.fromFile(outputFile)
            copyFile(context, fileUri, outputFileUri)
            return@withContext outputFile
        }
    }

    private suspend fun copyFile(context: Context, inputUri: Uri, outputUri: Uri) {
        withContext(context = Dispatchers.IO) {
            val inputStream = context.contentResolver.openInputStream(inputUri)
                ?: throw NullPointerException("InputStream for given input Uri is null")
            val outputStream = FileOutputStream(outputUri.toFile())
            val buffer = ByteArray(1024)
            var length: Int
            while (true) {
                length = inputStream.read(buffer)
                if (length > 0) {
                    outputStream.write(buffer, 0, length)
                } else {
                    break
                }
            }
            inputStream.close()
            outputStream.close()
        }
    }

    fun startListener() {
        if (job != null) {
            return
        }
        job = viewModelScope.launch(context = Dispatchers.IO) {
            _viewState.emit(value = ViewState.Idle)

            var serverSocket: ServerSocket? = null
            var clientInputStream: InputStream? = null
            var objectInputStream: ObjectInputStream? = null
            var fileOutputStream: FileOutputStream? = null
            try {
                _viewState.emit(value = ViewState.Connecting)
                log(log = "开启 Socket")
                serverSocket = ServerSocket()
                serverSocket.bind(InetSocketAddress(Constants.PORT))
                serverSocket.reuseAddress = true
                serverSocket.soTimeout = -1
                log(log = "socket accept，三十秒内如果未成功则断开链接")
                while (true) {
                    val client = serverSocket.accept()
                    val clientIpAddress = client.inetAddress
                    val clientIp = clientIpAddress.hostAddress
                    log(log = "客户端Ip地址：${clientIp} ---$clientIpAddress")

                    _viewState.emit(value = ViewState.Receiving)

                    clientInputStream = client.getInputStream()
                    objectInputStream = ObjectInputStream(clientInputStream)
                    val fileTransfer = objectInputStream.readObject() as FileTransfer
                    val file =
                        File(getStorageDir(context = getApplication()), fileTransfer.fileName)

                    log(log = "连接成功，待接收的文件: $fileTransfer")
                    log(log = "文件将保存到: $file")
                    log(log = "开始传输文件")

                    fileOutputStream = FileOutputStream(file)
                    val buffer = ByteArray(1024 * 100)
                    while (true) {
                        val length = clientInputStream.read(buffer)
                        if (length > 0) {
                            fileOutputStream.write(buffer, 0, length)
                        } else {
                            break
                        }
                        log(log = "正在传输文件，length : $length")
                    }
                    _viewState.emit(value = ViewState.Success(file = file))
                    log(log = "文件接收成功")
                    client.close()
                }
            } catch (e: Throwable) {
                log(log = "异常: " + e.message)
                _viewState.emit(value = ViewState.Failed(throwable = e))
            } finally {
                serverSocket?.close()
                clientInputStream?.close()
                objectInputStream?.close()
                fileOutputStream?.close()
            }
        }
        job?.invokeOnCompletion {
            job = null
        }
    }

    //外存文件路径
    fun getStorageDir(context: Context): File {
        val storageDir = File(PathUtils.getExternalDownloadsPath(), AppUtils.getAppName())
        storageDir.mkdirs()
        return storageDir
    }

    //缓存文件路径
    private fun getCacheDir(context: Context): File {
        val cacheDir = File(context.cacheDir, AppUtils.getAppName())
        cacheDir.mkdirs()
        return cacheDir
    }

    private suspend fun log(log: String) {
        _log.emit(value = log)
    }
}