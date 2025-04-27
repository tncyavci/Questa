package com.example.questa

import android.app.Application
import com.example.questa.di.appModule
import com.google.firebase.FirebaseApp
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import timber.log.Timber

class QuestaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        
        // Initialize Koin
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@QuestaApp)
            modules(appModule)
        }
    }
} 