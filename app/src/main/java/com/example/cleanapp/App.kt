package com.example.cleanapp

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin
import com.example.cleanapp.di.appModule
import com.example.data.di.dataModule

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            // Эта функция регистрирует KoinWorkerFactory,
            // чтобы Koin мог создавать экземпляры Worker'ов
            workManagerFactory()
            // Загружаем все модули нашего приложения
            modules(appModule, dataModule)
        }
    }
}

