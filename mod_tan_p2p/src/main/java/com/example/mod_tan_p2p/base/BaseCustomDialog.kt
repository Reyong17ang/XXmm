package com.example.mod_tan_p2p.base

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatDialog
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.example.mod_tan_p2p.R
import com.example.mod_tan_p2p.core.BindLife
import com.example.mod_tan_p2p.core.Stateable
import com.example.mod_tan_p2p.databinding.BaseDialogLayoutBinding
import com.jakewharton.rxbinding4.view.clicks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel

abstract class BaseCustomDialog<Binding: ViewDataBinding, State : Any>(context: Context, @LayoutRes val layoutId: Int, defaultState: State, private val clearBackground: Boolean = false, private val outSizeCancelable: Boolean = true)
    : AppCompatDialog(context), BindLife by BindLife(),  CoroutineScope by CoroutineScope(Dispatchers.Main), Stateable<State> by Stateable(defaultState) {


    val baseDialogBinding: BaseDialogLayoutBinding by lazy {
        DataBindingUtil.inflate<BaseDialogLayoutBinding>(LayoutInflater.from(context), R.layout.base_dialog_layout, window?.decorView as? ViewGroup, false)
    }

    val binding: Binding by lazy {
        DataBindingUtil.inflate<Binding>(LayoutInflater.from(context), layoutId, baseDialogBinding.outsideLayout, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding.root.isClickable = true
        adjustContentView(binding.root)
        val lp = FrameLayout.LayoutParams(binding.root.layoutParams as ViewGroup.MarginLayoutParams)
        lp.gravity = Gravity.CENTER
        baseDialogBinding.outsideLayout.addView(binding.root, lp)
        setContentView(baseDialogBinding.root)
        window?.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            if (clearBackground) {
                clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            }
        }
    }

    open fun adjustContentView(content: View): View = content

    override fun onStart() {
        super.onStart()
        setCanceledOnTouchOutside(false)
        baseDialogBinding.outsideLayout.clicks()
            .doOnNext {
                if (outSizeCancelable) {
                    cancel()
                }
            }
            .bindLife()
        bindingStart(binding)
    }

    open fun bindingStart(binding: Binding) {}

    override fun onStop() {
        super.onStop()
        bindingStop(binding)
        cancel("${this::class.java} closed")
        lifeCompositeDisposable.clear()
    }

    open fun bindingStop(binding: Binding) {}

}
