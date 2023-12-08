package com.example.lib_net.transferproto.filetransfer.model

import com.example.lib_net.transferproto.fileexplore.model.FileExploreFile
import java.io.File

data class SenderFile(
    val realFile: File,
    val exploreFile: FileExploreFile
)