package com.example.mod_p2p.utils

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.view.View
import androidx.appcompat.app.AppCompatActivity

object SystemUIUtils {
    fun hideStatusBarAndNavigationBar(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = activity.window
            val decorView = window.decorView
            val option = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
            decorView.systemUiVisibility = option
            window.statusBarColor = Color.TRANSPARENT
        }
        hideActionBar(activity as AppCompatActivity)
    }

    private fun hideActionBar(activity: AppCompatActivity) {
        if (activity.supportActionBar != null) {
            activity.supportActionBar!!.hide()
        }
    }
}