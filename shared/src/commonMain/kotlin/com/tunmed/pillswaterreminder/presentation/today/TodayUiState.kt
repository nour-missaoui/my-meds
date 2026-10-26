package com.tunmed.pillswaterreminder.presentation.today

import com.tunmed.pillswaterreminder.domain.model.DoseStatus
import com.tunmed.pillswaterreminder.domain.model.TodayDose
import kotlinx.datetime.LocalDate

data class TodayUiState(
    val date: LocalDate? = null,
    val rows: List<TodayDoseRow> = emptyList(),
    val hasMedications: Boolean = false,
    val notificationsAllowed: Boolean = true,
    val exactAlarmsAllowed: Boolean = true,
)

data class TodayDoseRow(
    val dose: TodayDose,
    val time: String,
    val actedTime: String?,
    val isPast: Boolean,
) {
    val key: String get() = "${dose.medication.id}-${dose.scheduledAt.toEpochMilliseconds()}"

    val canAnswer: Boolean get() = dose.status != DoseStatus.TAKEN && dose.status != DoseStatus.SKIPPED
}
