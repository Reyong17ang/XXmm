package com.example.lib_net.transferproto.p2pconn.model

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class P2pHandshakeReq(
    val version: Int,
    val deviceName: String
)