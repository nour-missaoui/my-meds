package com.tunmed.pillswaterreminder.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.tunmed.pillswaterreminder.presentation.theme.AppTheme

@Composable
fun PermissionRow(
    title: String,
    statusText: String,
    ok: Boolean,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    detail: String? = null,
) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                statusText,
                style = MaterialTheme.typography.labelMedium,
                color = if (ok) AppTheme.colors.taken else AppTheme.colors.missed,
            )
            if (detail != null && !ok) {
                Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (!ok) {
            FilledTonalButton(onClick = onAction) { Text(actionLabel) }
        }
    }
}
