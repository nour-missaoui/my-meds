package com.tunmed.pillswaterreminder.domain.model

/** User preferences (spec D-1). Stored in DataStore, not in Room. */
data class AppSettings(
    val onboardingDone: Boolean = false,
    val snoozeMinutes: Int = 10,
    /** A PENDING dose becomes MISSED after this many minutes (spec R-3). */
    val missedAfterMinutes: Int = 60,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
)
