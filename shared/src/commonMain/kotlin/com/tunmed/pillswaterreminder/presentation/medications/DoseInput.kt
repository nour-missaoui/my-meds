package com.tunmed.pillswaterreminder.presentation.medications

import androidx.compose.runtime.Composable
import com.tunmed.pillswaterreminder.domain.model.DoseUnit
import com.tunmed.pillswaterreminder.domain.model.MedicationForm
import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import pillswaterreminder.shared.generated.resources.Res
import pillswaterreminder.shared.generated.resources.form_capsule
import pillswaterreminder.shared.generated.resources.form_cream
import pillswaterreminder.shared.generated.resources.form_drops
import pillswaterreminder.shared.generated.resources.form_inhaler
import pillswaterreminder.shared.generated.resources.form_injection
import pillswaterreminder.shared.generated.resources.form_liquid
import pillswaterreminder.shared.generated.resources.form_other
import pillswaterreminder.shared.generated.resources.form_patch
import pillswaterreminder.shared.generated.resources.form_powder
import pillswaterreminder.shared.generated.resources.form_spray
import pillswaterreminder.shared.generated.resources.form_suppository
import pillswaterreminder.shared.generated.resources.form_tablet
import pillswaterreminder.shared.generated.resources.unit_application
import pillswaterreminder.shared.generated.resources.unit_capsule
import pillswaterreminder.shared.generated.resources.unit_dose
import pillswaterreminder.shared.generated.resources.unit_drop
import pillswaterreminder.shared.generated.resources.unit_injection
import pillswaterreminder.shared.generated.resources.unit_ml
import pillswaterreminder.shared.generated.resources.unit_patch
import pillswaterreminder.shared.generated.resources.unit_puff
import pillswaterreminder.shared.generated.resources.unit_sachet
import pillswaterreminder.shared.generated.resources.unit_spray
import pillswaterreminder.shared.generated.resources.unit_suppository
import pillswaterreminder.shared.generated.resources.unit_tablet
import pillswaterreminder.shared.generated.resources.unit_unit

sealed interface DoseInput {
    data class Quick(val text: String) : DoseInput

    data class Detailed(
        val form: MedicationForm,
        val amount: Double,
        val unit: DoseUnit,
        val strength: String?,
        val display: String,
    ) : DoseInput
}

fun parseDoseAmount(text: String): Double? {
    val value = text.trim().replace(',', '.').toDoubleOrNull() ?: return null
    return value.takeIf { it > 0.0 && it <= MAX_DOSE_AMOUNT }
}

fun formatDoseAmount(amount: Double): String =
    if (amount % 1.0 == 0.0) amount.toLong().toString() else amount.toString()

const val PLURAL_OTHER = 100

private const val MAX_DOSE_AMOUNT = 1000.0

private fun pluralQuantity(amount: Double): Int = if (amount % 1.0 == 0.0) amount.toInt() else PLURAL_OTHER

@Composable
fun formLabel(form: MedicationForm): String = stringResource(
    when (form) {
        MedicationForm.TABLET -> Res.string.form_tablet
        MedicationForm.CAPSULE -> Res.string.form_capsule
        MedicationForm.LIQUID -> Res.string.form_liquid
        MedicationForm.POWDER -> Res.string.form_powder
        MedicationForm.INJECTION -> Res.string.form_injection
        MedicationForm.INHALER -> Res.string.form_inhaler
        MedicationForm.SPRAY -> Res.string.form_spray
        MedicationForm.DROPS -> Res.string.form_drops
        MedicationForm.CREAM -> Res.string.form_cream
        MedicationForm.PATCH -> Res.string.form_patch
        MedicationForm.SUPPOSITORY -> Res.string.form_suppository
        MedicationForm.OTHER -> Res.string.form_other
    },
)

private fun unitPlural(unit: DoseUnit): PluralStringResource = when (unit) {
    DoseUnit.TABLET -> Res.plurals.unit_tablet
    DoseUnit.CAPSULE -> Res.plurals.unit_capsule
    DoseUnit.ML -> Res.plurals.unit_ml
    DoseUnit.SACHET -> Res.plurals.unit_sachet
    DoseUnit.INJECTION -> Res.plurals.unit_injection
    DoseUnit.UNIT -> Res.plurals.unit_unit
    DoseUnit.PUFF -> Res.plurals.unit_puff
    DoseUnit.SPRAY -> Res.plurals.unit_spray
    DoseUnit.DROP -> Res.plurals.unit_drop
    DoseUnit.APPLICATION -> Res.plurals.unit_application
    DoseUnit.PATCH -> Res.plurals.unit_patch
    DoseUnit.SUPPOSITORY -> Res.plurals.unit_suppository
    DoseUnit.DOSE -> Res.plurals.unit_dose
}

@Composable
fun unitLabel(unit: DoseUnit): String = pluralStringResource(unitPlural(unit), PLURAL_OTHER)

@Composable
fun unitLabelFor(unit: DoseUnit, amount: Double?): String =
    pluralStringResource(unitPlural(unit), amount?.let { pluralQuantity(it) } ?: PLURAL_OTHER)

@Composable
fun doseSummary(amounts: List<Double>, unit: DoseUnit, strength: String): String {
    val base = amounts.map { doseDisplay(it, unit, "") }.joinToString(" / ")
    val extra = strength.trim()
    return if (extra.isEmpty()) base else "$base · $extra"
}

@Composable
fun doseDisplay(amount: Double, unit: DoseUnit, strength: String): String {
    val base = "${formatDoseAmount(amount)} ${pluralStringResource(unitPlural(unit), pluralQuantity(amount))}"
    val extra = strength.trim()
    return if (extra.isEmpty()) base else "$base · $extra"
}
