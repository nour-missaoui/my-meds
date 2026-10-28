package com.tunmed.pillswaterreminder.presentation.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tunmed.pillswaterreminder.core.time.TimeProvider
import com.tunmed.pillswaterreminder.core.time.toHhMm
import com.tunmed.pillswaterreminder.domain.model.AsNeededToday
import com.tunmed.pillswaterreminder.domain.model.DoseStatus
import com.tunmed.pillswaterreminder.domain.model.MedicationStatus
import com.tunmed.pillswaterreminder.domain.model.TodayDose
import com.tunmed.pillswaterreminder.domain.reminder.ReminderPermissions
import com.tunmed.pillswaterreminder.domain.reminder.UpcomingDose
import com.tunmed.pillswaterreminder.domain.usecase.dose.LogAsNeededDoseUseCase
import com.tunmed.pillswaterreminder.domain.usecase.dose.RecordDoseActionUseCase
import com.tunmed.pillswaterreminder.domain.usecase.medication.ObserveMedicationsUseCase
import com.tunmed.pillswaterreminder.domain.usecase.today.ObserveAsNeededTodayUseCase
import com.tunmed.pillswaterreminder.domain.usecase.today.ObserveTodayDosesUseCase
import com.tunmed.pillswaterreminder.domain.usecase.today.ObserveUpcomingUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class TodayViewModel(
    private val observeTodayDoses: ObserveTodayDosesUseCase,
    private val observeUpcoming: ObserveUpcomingUseCase,
    private val observeAsNeeded: ObserveAsNeededTodayUseCase,
    observeMedications: ObserveMedicationsUseCase,
    private val recordDoseAction: RecordDoseActionUseCase,
    private val logAsNeededDose: LogAsNeededDoseUseCase,
    private val permissions: ReminderPermissions,
    private val timeProvider: TimeProvider,
) : ViewModel() {

    private data class PermissionState(val notifications: Boolean, val exactAlarms: Boolean)

    private data class DayContent(
        val date: LocalDate,
        val doses: List<TodayDose>,
        val upcoming: List<UpcomingDose>,
        val asNeeded: List<AsNeededToday>,
    )

    private val day = MutableStateFlow(timeProvider.today())
    private val permissionState = MutableStateFlow(readPermissions())
    private val refreshedAt = MutableStateFlow(timeProvider.now())

    private val dayContent = day.flatMapLatest { date ->
        combine(observeTodayDoses(date), observeUpcoming(date), observeAsNeeded(date)) { doses, upcoming, asNeeded ->
            DayContent(date, doses, upcoming, asNeeded)
        }
    }

    val uiState: StateFlow<TodayUiState> = combine(
        dayContent,
        observeMedications(),
        permissionState,
        refreshedAt,
    ) { content, medications, perms, _ ->
        val now = timeProvider.now()
        val zone = timeProvider.timeZone()
        TodayUiState(
            loaded = true,
            date = content.date,
            rows = content.doses.map { dose ->
                TodayDoseRow(
                    dose = dose,
                    time = dose.time.toHhMm(),
                    actedTime = dose.actedAt?.toLocalDateTime(zone)?.time?.toHhMm(),
                    isPast = dose.scheduledAt <= now,
                )
            },
            asNeeded = content.asNeeded.map { item ->
                AsNeededRow(item = item, lastTime = item.lastTakenAt?.toLocalDateTime(zone)?.time?.toHhMm())
            },
            upcoming = content.upcoming,
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

    fun logAsNeeded(medicationId: Long) {
        viewModelScope.launch { logAsNeededDose(medicationId) }
    }

    fun openNotificationSettings() = permissions.openNotificationSettings()

    fun openExactAlarmSettings() = permissions.openExactAlarmSettings()

    private fun readPermissions() = PermissionState(
        notifications = permissions.notificationsAllowed(),
        exactAlarms = permissions.exactAlarmsAllowed(),
    )
}
