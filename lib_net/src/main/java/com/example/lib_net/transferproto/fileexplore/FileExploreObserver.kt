package com.example.lib_net.transferproto.fileexplore

import com.example.lib_net.transferproto.fileexplore.model.SendMsgReq

interface FileExploreObserver {

    fun onNewState(state: FileExploreState)

    fun onNewMsg(msg: SendMsgReq)
}