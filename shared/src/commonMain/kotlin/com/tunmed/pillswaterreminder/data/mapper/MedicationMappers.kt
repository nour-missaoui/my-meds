package com.tunmed.pillswaterreminder.data.mapper

import com.tunmed.pillswaterreminder.data.local.entity.DoseLogEntity
import com.tunmed.pillswaterreminder.data.local.entity.MedScheduleEntity
import com.tunmed.pillswaterreminder.data.local.entity.MedicationEntity
import com.tunmed.pillswaterreminder.domain.model.DoseLog
import com.tunmed.pillswaterreminder.domain.model.DoseStatus
import com.tunmed.pillswaterreminder.domain.model.DoseUnit
import com.tunmed.pillswaterreminder.domain.model.MedSchedule
import com.tunmed.pillswaterreminder.domain.model.Medication
import com.tunmed.pillswaterreminder.domain.model.MedicationColor
import com.tunmed.pillswaterreminder.domain.model.MedicationForm
import com.tunmed.pillswaterreminder.domain.model.MedicationStatus
import com.tunmed.pillswaterreminder.domain.model.RecurrenceRule
import kotlin.time.Instant
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.number

internal fun MedicationEntity.toDomain(): Medication = Medication(
    id = id,
    name = name,
    dose = dose,
    form = MedicationForm.valueOf(form),
    note = note,
    color = MedicationColor.valueOf(color),
    startDate = LocalDate.parse(startDate),
    endDate = endDate?.let { LocalDate.parse(it) },
    status = MedicationStatus.valueOf(status),
    doseAmount = doseAmount,
    doseUnit = doseUnit?.let { name -> DoseUnit.entries.firstOrNull { it.name == name } },
    strength = strength,
)

internal fun Medication.toEntity(): MedicationEntity = MedicationEntity(
    id = id,
    name = name,
    dose = dose,
    form = form.name,
    note = note,
    color = color.name,
    startDate = startDate.toString(),
    endDate = endDate?.toString(),
    status = status.name,
    doseAmount = doseAmount,
    doseUnit = doseUnit?.name,
    strength = strength,
)

private const val RULE_DAILY = "DAILY"
private const val RULE_EVERY_N_DAYS = "EVERY_N_DAYS"
private const val RULE_WEEKLY = "WEEKLY"
private const val RULE_MONTHLY_DAY = "MONTHLY_DAY"
private const val RULE_MONTHLY_WEEKDAY = "MONTHLY_WEEKDAY"
private const val RULE_YEARLY = "YEARLY"
private const val RULE_AS_NEEDED = "AS_NEEDED"

internal fun MedScheduleEntity.toDomain(): MedSchedule = MedSchedule(
    id = id,
    medicationId = medicationId,
    rule = toRule(),
    times = times.decodeTimes(),
    headsUpDays = headsUpDays,
    validFrom = LocalDate.parse(validFrom),
    validTo = validTo?.let { LocalDate.parse(it) },
    doseByTime = timeDoses.decodeTimeDoses(),
)

private fun MedScheduleEntity.toRule(): RecurrenceRule = when (ruleType) {
    RULE_DAILY -> RecurrenceRule.Daily
    RULE_EVERY_N_DAYS -> RecurrenceRule.EveryNDays(
        interval = everyNDays ?: 1,
        anchor = LocalDate.parse(anchorDate ?: validFrom),
    )
    RULE_WEEKLY -> RecurrenceRule.Weekly(
        days = weekdays.orEmpty().split(",").filter { it.isNotBlank() }
            .map { DayOfWeek(it.trim().toInt()) }.toSet(),
    )
    RULE_MONTHLY_DAY -> RecurrenceRule.MonthlyOnDay(dayOfMonth = dayOfMonth ?: 1)
    RULE_MONTHLY_WEEKDAY -> RecurrenceRule.MonthlyOnWeekday(
        weekOfMonth = weekOfMonth ?: 1,
        dayOfWeek = DayOfWeek(weekday ?: 1),
    )
    RULE_YEARLY -> RecurrenceRule.Yearly(month = Month(month ?: 1), dayOfMonth = dayOfMonth ?: 1)
    else -> RecurrenceRule.AsNeeded
}

internal fun MedSchedule.toEntity(): MedScheduleEntity {
    val base = MedScheduleEntity(
        id = id,
        medicationId = medicationId,
        ruleType = RULE_AS_NEEDED,
        times = times.encodeTimes(),
        headsUpDays = headsUpDays,
        validFrom = validFrom.toString(),
        validTo = validTo?.toString(),
        timeDoses = doseByTime.encodeTimeDoses(),
    )
    return when (val r = rule) {
        RecurrenceRule.Daily -> base.copy(ruleType = RULE_DAILY)
        is RecurrenceRule.EveryNDays -> base.copy(
            ruleType = RULE_EVERY_N_DAYS,
            everyNDays = r.interval,
            anchorDate = r.anchor.toString(),
        )
        is RecurrenceRule.Weekly -> base.copy(
            ruleType = RULE_WEEKLY,
            weekdays = r.days.map { it.isoDayNumber }.sorted().joinToString(","),
        )
        is RecurrenceRule.MonthlyOnDay -> base.copy(ruleType = RULE_MONTHLY_DAY, dayOfMonth = r.dayOfMonth)
        is RecurrenceRule.MonthlyOnWeekday -> base.copy(
            ruleType = RULE_MONTHLY_WEEKDAY,
            weekOfMonth = r.weekOfMonth,
            weekday = r.dayOfWeek.isoDayNumber,
        )
        is RecurrenceRule.Yearly -> base.copy(
            ruleType = RULE_YEARLY,
            month = r.month.number,
            dayOfMonth = r.dayOfMonth,
        )
        RecurrenceRule.AsNeeded -> base
    }
}

internal fun DoseLogEntity.toDomain(): DoseLog = DoseLog(
    id = id,
    medicationId = medicationId,
    scheduleId = scheduleId,
    scheduledAt = Instant.fromEpochMilliseconds(scheduledAtEpochMs),
    status = DoseStatus.valueOf(status),
    actedAt = actedAtEpochMs?.let { Instant.fromEpochMilliseconds(it) },
)

internal fun DoseLog.toEntity(): DoseLogEntity = DoseLogEntity(
    id = id,
    medicationId = medicationId,
    scheduleId = scheduleId,
    scheduledAtEpochMs = scheduledAt.toEpochMilliseconds(),
    status = status.name,
    actedAtEpochMs = actedAt?.toEpochMilliseconds(),
)
