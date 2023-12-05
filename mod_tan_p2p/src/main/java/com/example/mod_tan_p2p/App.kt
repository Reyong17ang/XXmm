package com.example.mod_tan_p2p

import android.app.Application
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Settings.init(this)
    }



}