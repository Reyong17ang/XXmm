package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mod_p2p.ui.connection.P2pConnectionActivity
import com.example.mydemo.databinding.ActivityMainBinding
import com.example.transfer.ui.TransferActivity


class MainActivity : AppCompatActivity() {
    private lateinit var mBinding :ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        initListener()
    }

    private fun initListener() {
        mBinding.btnTransfer.setOnClickListener {
            startActivity(Intent(this@MainActivity,TransferActivity::class.java))
        }
        mBinding.btnP2p.setOnClickListener {
            startActivity(Intent(this@MainActivity,P2pConnectionActivity::class.java))
        }
    }
}