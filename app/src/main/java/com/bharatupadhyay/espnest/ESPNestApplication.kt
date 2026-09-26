package com.bharatupadhyay.espnest

import android.app.Application
import com.bharatupadhyay.espnest.di.AppContainer

class ESPNestApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
