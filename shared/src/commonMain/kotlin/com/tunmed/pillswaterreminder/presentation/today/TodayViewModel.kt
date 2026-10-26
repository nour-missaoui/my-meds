package com.tunmed.pillswaterreminder.presentation.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tunmed.pillswaterreminder.core.time.TimeProvider
import com.tunmed.pillswaterreminder.core.time.toHhMm
import com.tunmed.pillswaterreminder.domain.model.DoseStatus
import com.tunmed.pillswaterreminder.domain.model.MedicationStatus
import com.tunmed.pillswaterreminder.domain.reminder.ReminderPermissions
import com.tunmed.pillswaterreminder.domain.usecase.dose.RecordDoseActionUseCase
import com.tunmed.pillswaterreminder.domain.usecase.medication.ObserveMedicationsUseCase
import com.tunmed.pillswaterreminder.domain.usecase.today.ObserveTodayDosesUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class TodayViewModel(
    private val observeTodayDoses: ObserveTodayDosesUseCase,
    observeMedications: ObserveMedicationsUseCase,
    private val recordDoseAction: RecordDoseActionUseCase,
    private val permissions: ReminderPermissions,
    private val timeProvider: TimeProvider,
) : ViewModel() {

    private data class PermissionState(val notifications: Boolean, val exactAlarms: Boolean)

    private val day = MutableStateFlow(timeProvider.today())
    private val permissionState = MutableStateFlow(readPermissions())

    private val refreshedAt = MutableStateFlow(timeProvider.now())

    val uiState: StateFlow<TodayUiState> = combine(
        day.flatMapLatest { date -> observeTodayDoses(date).map { doses -> date to doses } },
        observeMedications(),
        permissionState,
        refreshedAt,
    ) { (date, doses), medications, perms, _ ->
        val now = timeProvider.now()
        val zone = timeProvider.timeZone()
        TodayUiState(
            date = date,
            rows = doses.map { dose ->
                TodayDoseRow(
                    dose = dose,
                    time = dose.time.toHhMm(),
                    actedTime = dose.actedAt?.toLocalDateTime(zone)?.time?.toHhMm(),
                    isPast = dose.scheduledAt <= now,
                )
            },
            hasMedications = medications.any { it.status == MedicationStatus.ACTIVE },
            notificationsAllowed = perms.notifications,
            exactAlarmsAllowed = perms.exactAlarms,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TodayUiState())

    fun refresh() {
        day.value = timeProvider.today()
        permissionState.value = readPermissions()
        refreshedAt.value = timeProvider.now()
    }

    fun answer(row: TodayDoseRow, status: DoseStatus) {
        viewModelScope.launch {
            recordDoseAction(
                medicationId = row.dose.medication.id,
                scheduleId = row.dose.scheduleId,
                scheduledAt = row.dose.scheduledAt,
                status = status,
            )
        }
    }

    fun openNotificationSettings() = permissions.openNotificationSettings()

    fun openExactAlarmSettings() = permissions.openExactAlarmSettings()

    private fun readPermissions() = PermissionState(
        notifications = permissions.notificationsAllowed(),
        exactAlarms = permissions.exactAlarmsAllowed(),
    )
}
