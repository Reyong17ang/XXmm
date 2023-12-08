package com.example.lib_net.netty.extensions

import com.example.lib_net.netty.PackageData

interface IBodyConverter {

    fun couldHandle(type: Int, dataClass: Class<*>): Boolean

    fun <T> convert(type: Int, dataClass: Class<T>, packageData: PackageData): T?
}