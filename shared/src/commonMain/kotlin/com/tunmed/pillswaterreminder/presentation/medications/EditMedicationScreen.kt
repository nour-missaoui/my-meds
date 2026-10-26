package com.tunmed.pillswaterreminder.presentation.medications

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tunmed.pillswaterreminder.presentation.components.PlaceholderCard
import com.tunmed.pillswaterreminder.presentation.components.ScreenHeader
import com.tunmed.pillswaterreminder.presentation.theme.AppTheme
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import pillswaterreminder.shared.generated.resources.Res
import pillswaterreminder.shared.generated.resources.action_save
import pillswaterreminder.shared.generated.resources.title_edit_medication
import pillswaterreminder.shared.generated.resources.title_new_medication

@Composable
fun EditMedicationScreen(
    medicationId: Long,
    onBack: () -> Unit,
    viewModel: EditMedicationViewModel = koinViewModel(parameters = { parametersOf(medicationId) }),
) {
    val medication by viewModel.medication.collectAsStateWithLifecycle()
    var name by rememberSaveable { mutableStateOf("") }
    var dose by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(medication) {
        medication?.let {
            name = it.name
            dose = it.dose
        }
    }

    val title = if (viewModel.isNew) Res.string.title_new_medication else Res.string.title_edit_medication
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = AppTheme.spacing.l),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.l),
    ) {
        ScreenHeader(title = stringResource(title), onBack = onBack)
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = dose, onValueChange = { dose = it }, label = { Text("Dose") }, modifier = Modifier.fillMaxWidth())
        PlaceholderCard(
            title = "Schedule",
            body = "Daily / every N days / weekly / monthly / yearly / as needed, times, heads-up and dates (Phase 3). Saved as Daily 08:00 for now.",
        )
        Button(
            onClick = { viewModel.save(name, dose, onSaved = onBack) },
            enabled = name.isNotBlank() && dose.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(Res.string.action_save))
        }
    }
}
