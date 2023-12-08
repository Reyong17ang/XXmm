package com.example.lib_net.transferproto

interface SimpleCallback<T> {
    fun onError(errorMsg: String) {}

    fun onSuccess(data: T) {}
}