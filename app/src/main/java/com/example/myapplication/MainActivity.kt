package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mod_p2p.ui.connection.P2pConnectionActivity
import com.example.mod_tan_p2p.ui.activity.connection.ConnectionActivity
import com.example.mod_tan_p2p.ui.activity.filetransport.FileTransportActivity
import com.example.mydemo.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        initListener()
    }

    private fun initListener() {
        mBinding.btnP2pTan.setOnClickListener {
            startActivity(Intent(this@MainActivity, ConnectionActivity::class.java))
        }
        mBinding.btnP2p.setOnClickListener {
            startActivity(Intent(this@MainActivity, P2pConnectionActivity::class.java))
        }
        mBinding.btnTransport.setOnClickListener {
            startActivity(Intent(this@MainActivity, FileTransportActivity::class.java))
        }
    }
}