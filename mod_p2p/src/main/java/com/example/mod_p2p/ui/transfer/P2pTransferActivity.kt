package com.example.mod_p2p.ui.transfer

import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import com.example.mod_p2p.base.BaseMvvmActivity
import com.example.mod_p2p.databinding.ActivityP2pTransferBinding
import com.example.mod_p2p.ext.onClick
import com.example.mod_p2p.ui.connection.viewmodel.P2pViewModel

class P2pTransferActivity:BaseMvvmActivity<ActivityP2pTransferBinding,P2pViewModel>() {

    override fun initView(savedInstanceState: Bundle?) {

    }

    override fun initListener() {
        mBinding.tvChooseFile.onClick {

        }
    }
}