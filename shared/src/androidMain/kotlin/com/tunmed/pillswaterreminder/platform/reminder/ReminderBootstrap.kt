package com.tunmed.pillswaterreminder.platform.reminder

import android.content.Context
import android.util.Log
import com.tunmed.pillswaterreminder.domain.usecase.reminder.RescheduleAllUseCase
import com.tunmed.pillswaterreminder.platform.notification.NotificationChannels
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object ReminderBootstrap : KoinComponent {

    private val rescheduleAll: RescheduleAllUseCase by inject()

    fun start(context: Context) {
        NotificationChannels.createAll(context)
        RescheduleWorker.enqueueDaily(context)
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            try {
                rescheduleAll()
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Start-up reminder cycle failed", e)
            }
        }
    }
}
