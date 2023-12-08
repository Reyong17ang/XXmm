package com.example.lib_net.transferproto.filetransfer.model

import androidx.annotation.Keep
import com.example.lib_net.transferproto.fileexplore.model.FileExploreFile
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class DownloadReq(
    val file: FileExploreFile,
    val start: Long,
    val end: Long
)