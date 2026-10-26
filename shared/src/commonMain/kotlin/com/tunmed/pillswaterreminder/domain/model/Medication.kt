package com.tunmed.pillswaterreminder.domain.model

import kotlinx.datetime.LocalDate

enum class MedicationForm { TABLET, CAPSULE, LIQUID, INJECTION, DROPS, INHALER, CREAM, OTHER }

enum class MedicationStatus { ACTIVE, PAUSED, ARCHIVED }

/** Tile colour the user picks for a medication (mapped to theme colours in presentation). */
enum class MedicationColor { CORAL, AQUA, AMBER, GREEN, RED, GREY }

data class Medication(
    val id: Long = 0,
    val name: String,
    val dose: String,
    val form: MedicationForm,
    val note: String? = null,
    val color: MedicationColor = MedicationColor.CORAL,
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
    val status: MedicationStatus = MedicationStatus.ACTIVE,
)
