package com.tunmed.pillswaterreminder.domain.usecase.settings

import com.tunmed.pillswaterreminder.domain.model.AppSettings
import com.tunmed.pillswaterreminder.domain.repository.SettingsRepository
import com.tunmed.pillswaterreminder.domain.usecase.reminder.RescheduleAllUseCase

class UpdateSettingsUseCase(
    private val repository: SettingsRepository,
    private val rescheduleAll: RescheduleAllUseCase,
) {
    suspend operator fun invoke(transform: (AppSettings) -> AppSettings) {
        repository.update(transform)
        rescheduleAll()
    }
}
