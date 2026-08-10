package com.trueq.app

import android.app.Application
import com.trueq.app.di.AppContainer

class TrueQApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
