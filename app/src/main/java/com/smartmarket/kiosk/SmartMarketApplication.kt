package com.smartmarket.kiosk

import android.app.Application
import com.smartmarket.kiosk.core.di.AppContainer

class SmartMarketApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
