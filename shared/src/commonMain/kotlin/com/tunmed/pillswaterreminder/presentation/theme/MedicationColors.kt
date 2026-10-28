package com.tunmed.pillswaterreminder.presentation.theme

import androidx.compose.ui.graphics.Color
import com.tunmed.pillswaterreminder.domain.model.MedicationColor

fun MedicationColor.toColor(): Color = when (this) {
    MedicationColor.CORAL -> Palette.MedBright
    MedicationColor.AQUA -> Palette.AquaBright
    MedicationColor.AMBER -> Palette.SwatchAmber
    MedicationColor.GREEN -> Palette.SwatchGreen
    MedicationColor.RED -> Palette.SwatchRed
    MedicationColor.GREY -> Palette.TextTertiary
}
