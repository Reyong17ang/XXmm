package com.example.lib_net.netty.extensions

interface IServerManager {

    fun <Request, Response> registerServer(s: IServer<Request, Response>)

    fun <Request, Response> unregisterServer(s: IServer<Request, Response>)

    fun clearAllServers()
}