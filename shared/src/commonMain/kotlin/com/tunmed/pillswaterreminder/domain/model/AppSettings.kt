package com.tunmed.pillswaterreminder.domain.model

data class AppSettings(
    val onboardingDone: Boolean = false,
    val snoozeMinutes: Int = 10,
    val missedAfterMinutes: Int = 60,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
)
