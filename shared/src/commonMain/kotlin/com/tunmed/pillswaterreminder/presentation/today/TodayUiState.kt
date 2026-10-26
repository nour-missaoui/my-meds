package com.tunmed.pillswaterreminder.presentation.today

import com.tunmed.pillswaterreminder.domain.model.Medication
import kotlinx.datetime.LocalDate

data class TodayUiState(
    val date: LocalDate? = null,
    /** ACTIVE medications. Phase 3 replaces this with today's dose timeline from ScheduleEngine. */
    val activeMedications: List<Medication> = emptyList(),
)
