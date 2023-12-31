package com.example.mod_tan_p2p.ui.activity.connection

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import com.example.mod_tan_p2p.R
import com.example.mod_tan_p2p.base.BaseActivity
import com.example.mod_tan_p2p.databinding.ConnectionActivityBinding
import com.example.mod_tan_p2p.ui.activity.commomdialog.showOptionalDialog
import com.example.mod_tan_p2p.utils.showToastShort
import com.permissionx.guolindev.PermissionX
import com.tbruyelle.rxpermissions3.RxPermissions
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.await
import kotlin.jvm.optionals.getOrNull

class ConnectionActivity :
    BaseActivity<ConnectionActivityBinding, Unit>(R.layout.connection_activity, Unit) {
    override fun firstLaunchInitData() {
        launch {
            val permissionNeed = mutableListOf<String>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionNeed.add(Manifest.permission.READ_MEDIA_IMAGES)
                    permissionNeed.add(Manifest.permission.READ_MEDIA_AUDIO)
                    permissionNeed.add(Manifest.permission.READ_MEDIA_VIDEO)
                } else {
                    permissionNeed.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            } else {
                permissionNeed.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                permissionNeed.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionNeed.add(Manifest.permission.NEARBY_WIFI_DEVICES)
            } else {
                permissionNeed.add(Manifest.permission.ACCESS_FINE_LOCATION)
                permissionNeed.add(Manifest.permission.ACCESS_COARSE_LOCATION)
            }
            RxPermissions(this@ConnectionActivity)
                .request(*permissionNeed.toTypedArray())
                .firstOrError()
                .await()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
                val grant = this@ConnectionActivity.showOptionalDialog(
                    title = getString(R.string.permission_request_title),
                    message = getString(R.string.permission_storage_request_content)
                ).await().getOrNull() ?: false
                if (grant) {
                    val i = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    i.data = Uri.fromParts("package", packageName, null)
                    startActivity(i)
                }
            }
        }
    }
}