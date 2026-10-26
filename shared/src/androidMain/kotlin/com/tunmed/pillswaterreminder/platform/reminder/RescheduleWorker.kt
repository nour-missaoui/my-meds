package com.tunmed.pillswaterreminder.platform.reminder

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.tunmed.pillswaterreminder.domain.usecase.reminder.RescheduleAllUseCase
import java.util.concurrent.TimeUnit
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class RescheduleWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params), KoinComponent {

    private val rescheduleAll: RescheduleAllUseCase by inject()

    override suspend fun doWork(): Result {
        rescheduleAll()
        return Result.success()
    }

    companion object {
        private const val UNIQUE_NAME = "reschedule-daily"

        fun enqueueDaily(context: Context) {
            val request = PeriodicWorkRequestBuilder<RescheduleWorker>(1, TimeUnit.DAYS).build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(UNIQUE_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        }
    }
}
