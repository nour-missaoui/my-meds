package com.tunmed.pillswaterreminder.domain.model

import kotlinx.datetime.LocalDate

enum class DoseUnit { TABLET, CAPSULE, ML, SACHET, INJECTION, UNIT, PUFF, SPRAY, DROP, APPLICATION, PATCH, SUPPOSITORY, DOSE }

enum class MedicationForm(val units: List<DoseUnit>) {
    TABLET(listOf(DoseUnit.TABLET)),
    CAPSULE(listOf(DoseUnit.CAPSULE)),
    LIQUID(listOf(DoseUnit.ML, DoseUnit.DROP)),
    POWDER(listOf(DoseUnit.SACHET)),
    INJECTION(listOf(DoseUnit.INJECTION, DoseUnit.UNIT, DoseUnit.ML)),
    INHALER(listOf(DoseUnit.PUFF)),
    SPRAY(listOf(DoseUnit.SPRAY)),
    DROPS(listOf(DoseUnit.DROP)),
    CREAM(listOf(DoseUnit.APPLICATION)),
    PATCH(listOf(DoseUnit.PATCH)),
    SUPPOSITORY(listOf(DoseUnit.SUPPOSITORY)),
    OTHER(listOf(DoseUnit.DOSE)),
    ;

    val defaultUnit: DoseUnit get() = units.first()
}

enum class MedicationStatus { ACTIVE, PAUSED, ARCHIVED }

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
    val doseAmount: Double? = null,
    val doseUnit: DoseUnit? = null,
    val strength: String? = null,
) {
    val hasDoseDetails: Boolean get() = doseAmount != null && doseUnit != null
}
