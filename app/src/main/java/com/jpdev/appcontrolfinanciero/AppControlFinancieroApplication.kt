package com.jpdev.appcontrolfinanciero

import android.app.Application
import com.jpdev.appcontrolfinanciero.data.FinanceRepository
import com.jpdev.appcontrolfinanciero.data.local.AppDatabase
import com.jpdev.appcontrolfinanciero.data.prefs.SettingsDataStore
import com.jpdev.appcontrolfinanciero.notifications.NotificationHelper

class AppControlFinancieroApplication : Application() {

    lateinit var repository: FinanceRepository
        private set

    lateinit var settingsDataStore: SettingsDataStore
        private set

    override fun onCreate() {
        super.onCreate()
        val database = AppDatabase.getInstance(this)
        repository = FinanceRepository(database.incomeDao(), database.expenseDao())
        settingsDataStore = SettingsDataStore(this)
        NotificationHelper.createChannel(this)
    }
}
