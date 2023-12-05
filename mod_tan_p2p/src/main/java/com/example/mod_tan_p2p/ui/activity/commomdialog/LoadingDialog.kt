package com.example.mod_tan_p2p.ui.activity.commomdialog

import android.app.Activity
import android.app.Dialog
import com.example.mod_tan_p2p.R
import com.example.mod_tan_p2p.base.BaseCustomDialog
import com.example.mod_tan_p2p.databinding.LoadingDialogLayoutBinding

fun Activity.showLoadingDialog(cancelable: Boolean = false): Dialog {
return object :BaseCustomDialog<LoadingDialogLayoutBinding,Unit>(
    context =this,
layoutId = R.layout.loading_dialog_layout,
defaultState = Unit,
clearBackground =true,
outSizeCancelable =false
){}.apply { setCancelable(cancelable);show() }
}