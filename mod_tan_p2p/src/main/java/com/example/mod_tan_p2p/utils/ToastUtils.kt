package com.example.mod_tan_p2p.utils

import android.content.Context
import android.widget.Toast

fun Context.showToastShort(resString: Int) {
    Toast.makeText(this, resString, Toast.LENGTH_SHORT).show()
}

fun Context.showToastShort(s: String) {
    Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
}