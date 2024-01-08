package com.example.mod_tan_p2p.base

import android.os.Bundle
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.addCallback
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mod_tan_p2p.core.BindLife
import com.example.mod_tan_p2p.core.Stateable
import io.reactivex.rxjava3.subjects.Subject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.di
import org.kodein.di.android.retainedSubDI
import org.kodein.di.bind
import org.kodein.di.singleton
import kotlin.coroutines.CoroutineContext

abstract class BaseActivity<Binding : ViewDataBinding, State : Any>(
    @LayoutRes
    layoutId: Int,
    val defaultState: State
) : AppCompatActivity(), CoroutineScope, Stateable<State>, BindLife by BindLife(), DIAware {

    class ActivityViewModel<State : Any>(defaultState: State) : ViewModel(),
        BindLife by BindLife(),
        CoroutineScope by CoroutineScope(Dispatchers.Main),
        Stateable<State> by Stateable(defaultState) {

        fun clearRxLife() {
            lifeCompositeDisposable.clear()
        }

        override fun onCleared() {
            super.onCleared()
            lifeCompositeDisposable.clear()
            cancel("Activity Finish")
        }
    }

    @Suppress("UNCHECKED_CAST")
    private val viewModel: ActivityViewModel<State> by lazy {
        ViewModelProvider(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ActivityViewModel(defaultState) as T
            }
        })[ActivityViewModel::class.java] as ActivityViewModel<State>
    }

    override val coroutineContext: CoroutineContext by lazy { viewModel.coroutineContext }

    override val stateStore: Subject<State> by lazy { viewModel.stateStore }

    protected val binding: Binding by lazy { DataBindingUtil.setContentView(this, layoutId) }

    override val di: DI by retainedSubDI(di()) {
        bind<OnBackPressedDispatcher>() with singleton { onBackPressedDispatcher }
        addDIInstance()
    }

    open fun DI.MainBuilder.addDIInstance() {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback {
            onActivityBackPressed()
        }
        viewModel.clearRxLife()
        if (savedInstanceState == null) {
            firstLaunchInitData()
        }
        WindowCompat.setDecorFitsSystemWindows(window, true)
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.show(WindowInsetsCompat.Type.systemBars())
        insetsController.isAppearanceLightStatusBars = true
        initViews(binding)
    }

    open fun firstLaunchInitData() {

    }

    open fun initViews(binding: Binding) {

    }

    open fun onActivityBackPressed() {
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        lifeCompositeDisposable.clear()
    }
}