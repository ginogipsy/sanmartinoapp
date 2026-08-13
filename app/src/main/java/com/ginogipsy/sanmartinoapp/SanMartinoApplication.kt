package com.ginogipsy.sanmartinoapp

import android.app.Application
import com.ginogipsy.sanmartinoapp.di.AppContainer
import com.ginogipsy.sanmartinoapp.di.DefaultAppContainer

class SanMartinoApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
