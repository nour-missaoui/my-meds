package com.tunmed.pillswaterreminder.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tunmed.pillswaterreminder.domain.model.DoseStatus
import com.tunmed.pillswaterreminder.presentation.theme.AppTheme
import org.jetbrains.compose.resources.stringResource
import pillswaterreminder.shared.generated.resources.Res
import pillswaterreminder.shared.generated.resources.status_due
import pillswaterreminder.shared.generated.resources.status_missed
import pillswaterreminder.shared.generated.resources.status_not_logged
import pillswaterreminder.shared.generated.resources.status_skipped
import pillswaterreminder.shared.generated.resources.status_snoozed
import pillswaterreminder.shared.generated.resources.status_taken_at
import pillswaterreminder.shared.generated.resources.status_upcoming

data class StatusStyle(val label: String, val color: Color, val container: Color)

@Composable
fun doseStatusStyle(status: DoseStatus?, actedTime: String?, scheduledTime: String, isPast: Boolean): StatusStyle {
    val colors = AppTheme.colors
    return when (status) {
        DoseStatus.TAKEN -> StatusStyle(stringResource(Res.string.status_taken_at, actedTime ?: scheduledTime), colors.taken, colors.takenContainer)
        DoseStatus.SKIPPED -> StatusStyle(stringResource(Res.string.status_skipped), colors.skipped, colors.skippedContainer)
        DoseStatus.MISSED -> StatusStyle(stringResource(Res.string.status_missed), colors.missed, colors.missedContainer)
        DoseStatus.SNOOZED -> StatusStyle(stringResource(Res.string.status_snoozed), colors.due, colors.dueContainer)
        DoseStatus.PENDING -> StatusStyle(stringResource(Res.string.status_due), colors.due, colors.dueContainer)
        null ->
            if (isPast) {
                StatusStyle(stringResource(Res.string.status_not_logged), colors.textTertiary, MaterialTheme.colorScheme.surfaceVariant)
            } else {
                StatusStyle(stringResource(Res.string.status_upcoming), MaterialTheme.colorScheme.onSurfaceVariant, MaterialTheme.colorScheme.surfaceVariant)
            }
    }
}

@Composable
fun StatusChip(style: StatusStyle, modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(RoundedCornerShape(50))
            .background(style.container)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(style.label, style = MaterialTheme.typography.labelMedium, color = style.color)
    }
}

@Composable
fun ColorDot(color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier
            .padding(end = AppTheme.spacing.m)
            .clip(RoundedCornerShape(50))
            .background(color)
            .padding(5.dp),
    )
}
