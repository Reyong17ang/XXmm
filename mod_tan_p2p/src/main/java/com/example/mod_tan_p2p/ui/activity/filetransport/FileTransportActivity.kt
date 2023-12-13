package com.example.mod_tan_p2p.ui.activity.filetransport

import com.example.mod_tan_p2p.R
import com.example.mod_tan_p2p.base.BaseActivity
import com.example.mod_tan_p2p.base.FileTreeUI
import com.example.mod_tan_p2p.databinding.FolderSelectActivityBinding

class FileTransportActivity : BaseActivity<FolderSelectActivityBinding, Unit>(
    R.layout.folder_select_activity,
    defaultState = Unit
) {
    private var fileTreeUI: FileTreeUI? = null

}