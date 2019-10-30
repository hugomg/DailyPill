package com.example.thyroidhelper

import android.app.Application

@Suppress("unused")
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DataModel.init(this)
        Notifications.init(this)
    }
}