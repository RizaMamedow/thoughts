package dev.thoughts.app

import android.app.Application
import dev.thoughts.app.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ThoughtsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@ThoughtsApplication)
            modules(appModule)
        }
    }
}
