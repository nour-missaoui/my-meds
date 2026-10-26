package com.tunmed.pillswaterreminder.platform.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.tunmed.pillswaterreminder.domain.usecase.reminder.RescheduleAllUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Re-arms the next alarm after events that clear or shift alarms: reboot, clock or
 * time-zone change, app update, and the exact-alarm permission being granted.
 * Intent filters are declared in androidApp's AndroidManifest.xml.
 */
class SystemEventsReceiver : BroadcastReceiver(), KoinComponent {

    private val rescheduleAll: RescheduleAllUseCase by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            try {
                rescheduleAll()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
