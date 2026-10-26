package com.tunmed.pillswaterreminder.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Aqua (Figma "water" tokens) = primary brand colour; coral ("med" tokens) = medication accents. */
private val LightColors = lightColorScheme(
    primary = Palette.Aqua,
    onPrimary = Palette.TextOnColor,
    primaryContainer = Palette.AquaContainer,
    onPrimaryContainer = Palette.AquaOnContainer,
    secondary = Palette.MedPrimary,
    onSecondary = Palette.TextOnColor,
    secondaryContainer = Palette.MedContainer,
    onSecondaryContainer = Palette.MedOnContainer,
    tertiary = Palette.StatusTaken,
    onTertiary = Palette.TextOnColor,
    tertiaryContainer = Palette.StatusTakenContainer,
    onTertiaryContainer = Palette.StatusTaken,
    background = Palette.BgApp,
    onBackground = Palette.TextPrimary,
    surface = Palette.BgSurface,
    onSurface = Palette.TextPrimary,
    surfaceVariant = Palette.BgSurfaceVariant,
    onSurfaceVariant = Palette.TextSecondary,
    outline = Palette.BorderStrong,
    outlineVariant = Palette.BorderDefault,
    error = Palette.StatusMissed,
    onError = Palette.TextOnColor,
    errorContainer = Palette.StatusMissedContainer,
    onErrorContainer = Palette.StatusMissed,
    scrim = Palette.BgScrim,
)

/** App colours that Material 3 has no slot for (aqua bright, dose statuses, …). */
@Immutable
data class ExtendedColors(
    val aquaBright: Color,
    val medBright: Color,
    val textTertiary: Color,
    val taken: Color,
    val takenContainer: Color,
    val missed: Color,
    val missedContainer: Color,
    val due: Color,
    val dueContainer: Color,
    val skipped: Color,
    val skippedContainer: Color,
)

private val LightExtendedColors = ExtendedColors(
    aquaBright = Palette.AquaBright,
    medBright = Palette.MedBright,
    textTertiary = Palette.TextTertiary,
    taken = Palette.StatusTaken,
    takenContainer = Palette.StatusTakenContainer,
    missed = Palette.StatusMissed,
    missedContainer = Palette.StatusMissedContainer,
    due = Palette.StatusDue,
    dueContainer = Palette.StatusDueContainer,
    skipped = Palette.StatusSkipped,
    skippedContainer = Palette.StatusSkippedContainer,
)

private val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalExtendedColors provides LightExtendedColors,
        LocalSpacing provides Spacing(),
    ) {
        MaterialTheme(
            colorScheme = LightColors,
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}

/** Access with AppTheme.colors.taken, AppTheme.spacing.l, … */
object AppTheme {
    val colors: ExtendedColors
        @Composable @ReadOnlyComposable get() = LocalExtendedColors.current

    val spacing: Spacing
        @Composable @ReadOnlyComposable get() = LocalSpacing.current
}
