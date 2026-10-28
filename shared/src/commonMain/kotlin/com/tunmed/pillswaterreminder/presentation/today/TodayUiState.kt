package com.tunmed.pillswaterreminder.presentation.today

import com.tunmed.pillswaterreminder.domain.model.AsNeededToday
import com.tunmed.pillswaterreminder.domain.model.DoseStatus
import com.tunmed.pillswaterreminder.domain.model.TodayDose
import com.tunmed.pillswaterreminder.domain.reminder.UpcomingDose
import kotlinx.datetime.LocalDate

data class TodayUiState(
    val loaded: Boolean = false,
    val date: LocalDate? = null,
    val rows: List<TodayDoseRow> = emptyList(),
    val asNeeded: List<AsNeededRow> = emptyList(),
    val upcoming: List<UpcomingDose> = emptyList(),
    val hasMedications: Boolean = false,
    val notificationsAllowed: Boolean = true,
    val exactAlarmsAllowed: Boolean = true,
) {
    val takenCount: Int get() = rows.count { it.dose.status == DoseStatus.TAKEN }
    val timeGroups: List<Pair<String, List<TodayDoseRow>>> get() = rows.groupBy { it.time }.toList()
}

data class TodayDoseRow(
    val dose: TodayDose,
    val time: String,
    val actedTime: String?,
    val isPast: Boolean,
) {
    val key: String get() = "${dose.medication.id}-${dose.scheduledAt.toEpochMilliseconds()}"

    val canAnswer: Boolean get() = dose.status != DoseStatus.TAKEN && dose.status != DoseStatus.SKIPPED
}

data class AsNeededRow(
    val item: AsNeededToday,
    val lastTime: String?,
)
