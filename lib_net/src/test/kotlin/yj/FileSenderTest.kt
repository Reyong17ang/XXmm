package yj

import com.example.lib_net.netty.findLocalAddressV4
import com.example.lib_net.transferproto.fileexplore.model.FileExploreFile
import com.example.lib_net.transferproto.filetransfer.FileSender
import com.example.lib_net.transferproto.filetransfer.FileTransferObserver
import com.example.lib_net.transferproto.filetransfer.FileTransferState
import com.example.lib_net.transferproto.filetransfer.model.SenderFile
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.io.File

object FileSenderTest {

    @JvmStatic
    fun main(args: Array<String>) {
        val rootDir = File("./lib_net/testdir")
        if (!rootDir.isDirectory) {
            rootDir.mkdirs()
        }
        val test1File = File(rootDir, "test.txt")
        if (!test1File.isFile) {
            println("TestFile1 is not exit")
            return
        }
        val test2File = File(rootDir, "test2.txt")
        if (!test2File.isFile) {
            println("TestFile2 is not exit")
            return
        }
        val localAddress = findLocalAddressV4()[0]
        val sender = FileSender(
            files = listOf(
                SenderFile(
                    realFile = test1File,
                    exploreFile = FileExploreFile(
                        name = test1File.name,
                        path = test1File.name,
                        size = test1File.length(),
                        lastModify = test1File.lastModified()
                    )
                ),
                SenderFile(
                    realFile = test2File,
                    exploreFile = FileExploreFile(
                        name = test2File.name,
                        path = test2File.name,
                        size = test2File.length(),
                        lastModify = test2File.lastModified()
                    )
                )
            ),
            bindAddress = localAddress,
            log = TestLog
        )

        sender.addObserver(object : FileTransferObserver {

            override fun onNewState(s: FileTransferState) {
                println("Sender state: $s")
            }

            override fun onStartFile(file: FileExploreFile) {
                println("Sender start ${file.name}")
            }

            override fun onProgressUpdate(file: FileExploreFile, progress: Long) {
                println("Sender process ${progress.toDouble() / file.size.toDouble()}")
            }

            override fun onEndFile(file: FileExploreFile) {
                println("Sender end ${file.name}")
            }

        })
        sender.start()
        runBlocking {
            delay(60 * 1000 * 5)
        }

    }
}