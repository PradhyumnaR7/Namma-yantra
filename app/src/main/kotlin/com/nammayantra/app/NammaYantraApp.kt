package com.nammayantra.app

import android.app.Application
import com.google.firebase.FirebaseApp

class NammaYantraApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
