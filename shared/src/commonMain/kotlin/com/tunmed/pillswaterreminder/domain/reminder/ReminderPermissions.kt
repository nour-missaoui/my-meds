package com.tunmed.pillswaterreminder.domain.reminder

interface ReminderPermissions {
    fun notificationsAllowed(): Boolean
    fun exactAlarmsAllowed(): Boolean
    fun openNotificationSettings()
    fun openExactAlarmSettings()
}
