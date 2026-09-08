package com.autologix.tms

import android.app.Application
import com.autologix.tms.core.di.AppContainer
import com.autologix.tms.core.di.DefaultAppContainer

class TmsApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
