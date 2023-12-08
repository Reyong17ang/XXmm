package com.example.lib_net.transferproto.filetransfer

import com.example.lib_net.transferproto.fileexplore.model.FileExploreFile

interface FileTransferObserver {

    fun onNewState(s: FileTransferState)

    fun onStartFile(file: FileExploreFile)

    fun onProgressUpdate(file: FileExploreFile, progress: Long)

    fun onEndFile(file: FileExploreFile)
}