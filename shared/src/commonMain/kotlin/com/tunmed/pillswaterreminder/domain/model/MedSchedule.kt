package com.tunmed.pillswaterreminder.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

data class TimeDose(
    val text: String,
    val amount: Double? = null,
)

data class MedSchedule(
    val id: Long = 0,
    val medicationId: Long,
    val rule: RecurrenceRule,
    val times: List<LocalTime>,
    val headsUpDays: Int = 0,
    val validFrom: LocalDate,
    val validTo: LocalDate? = null,
    val doseByTime: Map<LocalTime, TimeDose> = emptyMap(),
) {
    fun doseTextAt(time: LocalTime, default: String): String = doseByTime[time]?.text ?: default
}
