package com.tunmed.pillswaterreminder.presentation.permissions

import com.tunmed.pillswaterreminder.domain.reminder.ReminderPermissions

data class PermissionStatus(
    val notifications: Boolean = true,
    val exactAlarms: Boolean = true,
    val batteryUnrestricted: Boolean = true,
)

fun ReminderPermissions.status(): PermissionStatus = PermissionStatus(
    notifications = notificationsAllowed(),
    exactAlarms = exactAlarmsAllowed(),
    batteryUnrestricted = batteryOptimizationIgnored(),
)
