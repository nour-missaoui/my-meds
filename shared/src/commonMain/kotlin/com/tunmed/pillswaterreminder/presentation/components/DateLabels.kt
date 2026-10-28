package com.tunmed.pillswaterreminder.presentation.components

import androidx.compose.runtime.Composable
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.number
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import pillswaterreminder.shared.generated.resources.Res
import pillswaterreminder.shared.generated.resources.month_1
import pillswaterreminder.shared.generated.resources.month_10
import pillswaterreminder.shared.generated.resources.month_11
import pillswaterreminder.shared.generated.resources.month_12
import pillswaterreminder.shared.generated.resources.month_2
import pillswaterreminder.shared.generated.resources.month_3
import pillswaterreminder.shared.generated.resources.month_4
import pillswaterreminder.shared.generated.resources.month_5
import pillswaterreminder.shared.generated.resources.month_6
import pillswaterreminder.shared.generated.resources.month_7
import pillswaterreminder.shared.generated.resources.month_8
import pillswaterreminder.shared.generated.resources.month_9
import pillswaterreminder.shared.generated.resources.weekday_1
import pillswaterreminder.shared.generated.resources.weekday_2
import pillswaterreminder.shared.generated.resources.weekday_3
import pillswaterreminder.shared.generated.resources.weekday_4
import pillswaterreminder.shared.generated.resources.weekday_5
import pillswaterreminder.shared.generated.resources.weekday_6
import pillswaterreminder.shared.generated.resources.weekday_7
import pillswaterreminder.shared.generated.resources.weekday_short_1
import pillswaterreminder.shared.generated.resources.weekday_short_2
import pillswaterreminder.shared.generated.resources.weekday_short_3
import pillswaterreminder.shared.generated.resources.weekday_short_4
import pillswaterreminder.shared.generated.resources.weekday_short_5
import pillswaterreminder.shared.generated.resources.weekday_short_6
import pillswaterreminder.shared.generated.resources.weekday_short_7

@Composable
fun dateLabel(date: LocalDate): String = "${date.day} ${stringResource(monthRes(date.month))} ${date.year}"

@Composable
fun dayLabel(date: LocalDate): String =
    "${stringResource(weekdayShortRes(date.dayOfWeek))} ${date.day} ${stringResource(monthRes(date.month))}"

@Composable
fun longDayLabel(date: LocalDate): String =
    "${stringResource(weekdayRes(date.dayOfWeek))} ${date.day} ${stringResource(monthRes(date.month))}"

fun monthRes(month: Month): StringResource = when (month.number) {
    1 -> Res.string.month_1
    2 -> Res.string.month_2
    3 -> Res.string.month_3
    4 -> Res.string.month_4
    5 -> Res.string.month_5
    6 -> Res.string.month_6
    7 -> Res.string.month_7
    8 -> Res.string.month_8
    9 -> Res.string.month_9
    10 -> Res.string.month_10
    11 -> Res.string.month_11
    else -> Res.string.month_12
}

fun weekdayRes(day: DayOfWeek): StringResource = when (day.isoDayNumber) {
    1 -> Res.string.weekday_1
    2 -> Res.string.weekday_2
    3 -> Res.string.weekday_3
    4 -> Res.string.weekday_4
    5 -> Res.string.weekday_5
    6 -> Res.string.weekday_6
    else -> Res.string.weekday_7
}

fun weekdayShortRes(day: DayOfWeek): StringResource = when (day.isoDayNumber) {
    1 -> Res.string.weekday_short_1
    2 -> Res.string.weekday_short_2
    3 -> Res.string.weekday_short_3
    4 -> Res.string.weekday_short_4
    5 -> Res.string.weekday_short_5
    6 -> Res.string.weekday_short_6
    else -> Res.string.weekday_short_7
}
