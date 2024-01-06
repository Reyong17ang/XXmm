package com.example.lib_net.transferproto.fileexplore


interface FileExploreRequestHandler<Req, Resp> {

    fun onRequest(isNew: Boolean, request: Req): Resp?
}