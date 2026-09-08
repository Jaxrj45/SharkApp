package com.example.tibuapp

import android.app.Application
import com.example.tibuapp.di.AppContainer

class TibuApp: Application() {

    lateinit var container: AppContainer

    override fun onCreate(){
        super.onCreate()
        container = AppContainer(this)
    }

}