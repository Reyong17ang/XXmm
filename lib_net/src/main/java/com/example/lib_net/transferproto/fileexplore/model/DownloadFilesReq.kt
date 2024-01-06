package com.example.lib_net.transferproto.fileexplore.model

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class DownloadFilesReq(
    val downloadFiles: List<FileExploreFile>,
    val bufferSize: Int
)