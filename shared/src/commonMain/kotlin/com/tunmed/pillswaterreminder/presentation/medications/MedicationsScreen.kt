package com.tunmed.pillswaterreminder.presentation.medications

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tunmed.pillswaterreminder.domain.model.Medication
import com.tunmed.pillswaterreminder.presentation.components.PlaceholderCard
import com.tunmed.pillswaterreminder.presentation.components.ScreenHeader
import com.tunmed.pillswaterreminder.presentation.theme.AppTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pillswaterreminder.shared.generated.resources.Res
import pillswaterreminder.shared.generated.resources.action_add_medication
import pillswaterreminder.shared.generated.resources.ic_add
import pillswaterreminder.shared.generated.resources.title_medications

@Composable
fun MedicationsScreen(
    onAddMedication: () -> Unit,
    onOpenMedication: (Long) -> Unit,
    viewModel: MedicationsViewModel = koinViewModel(),
) {
    val medications by viewModel.medications.collectAsStateWithLifecycle()
    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = AppTheme.spacing.l, end = AppTheme.spacing.l, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.m),
        ) {
            item {
                ScreenHeader(
                    title = stringResource(Res.string.title_medications),
                    overline = "${medications.size} saved",
                )
            }
            if (medications.isEmpty()) {
                item {
                    PlaceholderCard(title = "No medications yet", body = "Tap “Add medication” to create the first one.")
                }
            }
            items(medications, key = { it.id }) { medication ->
                MedicationRow(medication = medication, onClick = { onOpenMedication(medication.id) })
            }
        }
        ExtendedFloatingActionButton(
            onClick = onAddMedication,
            icon = { Icon(painterResource(Res.drawable.ic_add), contentDescription = null) },
            text = { Text(stringResource(Res.string.action_add_medication)) },
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
            modifier = Modifier.align(Alignment.BottomEnd).padding(AppTheme.spacing.l),
        )
    }
}

@Composable
private fun MedicationRow(medication: Medication, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(AppTheme.spacing.l)) {
            Text(medication.name, style = MaterialTheme.typography.titleMedium)
            Text(
                "${medication.dose} · ${medication.status}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
