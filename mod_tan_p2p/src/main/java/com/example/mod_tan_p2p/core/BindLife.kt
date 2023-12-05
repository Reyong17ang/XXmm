package com.example.mod_tan_p2p.core
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable


interface BindLife {
    val lifeCompositeDisposable: CompositeDisposable

    fun <T : Any> io.reactivex.rxjava3.core.Observable<T>.bindLife() {
        lifeCompositeDisposable.add(this.subscribe({
            // Log.d(this@com.example.mod_tan_p2p.core.BindLife.javaClass.name, "Next: ${it.toString()}")
        }, {
            // Log.e(this@com.example.mod_tan_p2p.core.BindLife.javaClass.name, it.toString())
        }, {
            // Log.d(this@com.example.mod_tan_p2p.core.BindLife.javaClass.name,"Complete")
        }))
    }

    fun Completable.bindLife() {
        lifeCompositeDisposable.add(this.subscribe({
            // Log.d(this@com.example.mod_tan_p2p.core.BindLife.javaClass.name,"Complete")
        }, {
            // Log.d(this@com.example.mod_tan_p2p.core.BindLife.javaClass.name, it.toString())
        }))
    }

    fun <T : Any> Single<T>.bindLife() {
        lifeCompositeDisposable.add(this.subscribe({
            // Log.d(this@com.example.mod_tan_p2p.core.BindLife.javaClass.name, it.toString())
        }, {
            // Log.d(this@com.example.mod_tan_p2p.core.BindLife.javaClass.name, it.toString())
        }))
    }

    fun <T : Any> Maybe<T>.bindLife() {
        lifeCompositeDisposable.add(this.subscribe ({
            // Log.d(this@com.example.mod_tan_p2p.core.BindLife.javaClass.name,"Success: $it")
        }, {
            // Log.d(this@com.example.mod_tan_p2p.core.BindLife.javaClass.name, it.toString())
        }, {
            // Log.d(this@com.example.mod_tan_p2p.core.BindLife.javaClass.name,"Complete")
        }))
    }
}

fun BindLife(): BindLife = object : BindLife {
    override val lifeCompositeDisposable: CompositeDisposable = CompositeDisposable()
}