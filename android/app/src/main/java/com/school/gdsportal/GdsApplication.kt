package com.school.gdsportal

import android.app.Application
import com.school.gdsportal.di.AppContainer

class GdsApplication : Application() {
    
    // Instance of AppContainer that will be used by all the Activities/ViewModels
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
