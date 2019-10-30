package com.example.thyroidhelper

import android.app.Application;

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DataModel.init(this)
        Notifications.init(this)
    }
}