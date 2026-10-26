package com.tunmed.pillswaterreminder.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable data object OnboardingRoute

@Serializable data object TodayRoute

@Serializable data object MedicationsRoute

@Serializable
data class EditMedicationRoute(val medicationId: Long = NEW_MEDICATION) {
    companion object {
        const val NEW_MEDICATION = -1L
    }
}

@Serializable data object HistoryRoute

@Serializable data object SettingsRoute
