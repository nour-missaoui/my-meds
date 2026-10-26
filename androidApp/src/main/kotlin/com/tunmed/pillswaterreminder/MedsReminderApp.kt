package com.tunmed.pillswaterreminder

import android.app.Application
import com.tunmed.pillswaterreminder.di.androidPlatformModule
import com.tunmed.pillswaterreminder.di.sharedModules
import com.tunmed.pillswaterreminder.platform.notification.NotificationChannels
import com.tunmed.pillswaterreminder.platform.reminder.RescheduleWorker
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class MedsReminderApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@MedsReminderApp)
            modules(sharedModules() + androidPlatformModule)
        }
        NotificationChannels.createAll(this)
        RescheduleWorker.enqueueDaily(this)
    }
}
