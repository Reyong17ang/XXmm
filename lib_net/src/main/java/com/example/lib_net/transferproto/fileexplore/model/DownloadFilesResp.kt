package com.example.lib_net.transferproto.fileexplore.model

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class DownloadFilesResp(
    val maxConnection: Int
)