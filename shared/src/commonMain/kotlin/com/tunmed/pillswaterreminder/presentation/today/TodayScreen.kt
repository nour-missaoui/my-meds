package com.tunmed.pillswaterreminder.presentation.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tunmed.pillswaterreminder.domain.model.Medication
import com.tunmed.pillswaterreminder.presentation.components.PlaceholderCard
import com.tunmed.pillswaterreminder.presentation.components.ScreenHeader
import com.tunmed.pillswaterreminder.presentation.theme.AppTheme
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pillswaterreminder.shared.generated.resources.Res
import pillswaterreminder.shared.generated.resources.action_add_medication
import pillswaterreminder.shared.generated.resources.title_today

@Composable
fun TodayScreen(
    onAddMedication: () -> Unit,
    viewModel: TodayViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AppTheme.spacing.l),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.m),
    ) {
        ScreenHeader(title = stringResource(Res.string.title_today), overline = state.date?.toString())
        if (state.activeMedications.isEmpty()) {
            PlaceholderCard(title = "No medications yet", body = "Add your first medication to get reminders.")
            Button(
                onClick = onAddMedication,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(Res.string.action_add_medication))
            }
        } else {
            state.activeMedications.forEach { ActiveMedicationRow(it) }
        }
        PlaceholderCard(
            title = "Today's doses",
            body = "Timeline with Taken / Skipped / Missed and the Upcoming card (Phase 3, needs ScheduleEngine from Phase 1).",
        )
    }
}

/** Placeholder for the Figma "Reminder row" component (Phase 3). */
@Composable
private fun ActiveMedicationRow(medication: Medication) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(AppTheme.spacing.l)) {
            Text(medication.name, style = MaterialTheme.typography.titleMedium)
            Text(medication.dose, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
