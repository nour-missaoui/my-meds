package com.tunmed.pillswaterreminder.presentation.medications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tunmed.pillswaterreminder.domain.model.MedicationOverview
import com.tunmed.pillswaterreminder.domain.model.MedicationStatus
import com.tunmed.pillswaterreminder.presentation.components.PlaceholderCard
import com.tunmed.pillswaterreminder.presentation.components.ScreenHeader
import com.tunmed.pillswaterreminder.presentation.theme.AppTheme
import com.tunmed.pillswaterreminder.presentation.theme.toColor
import kotlinx.datetime.LocalDate
import org.koin.compose.viewmodel.koinViewModel
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import pillswaterreminder.shared.generated.resources.Res
import pillswaterreminder.shared.generated.resources.action_add_medication
import pillswaterreminder.shared.generated.resources.action_cancel
import pillswaterreminder.shared.generated.resources.action_delete
import pillswaterreminder.shared.generated.resources.action_edit
import pillswaterreminder.shared.generated.resources.action_hide
import pillswaterreminder.shared.generated.resources.action_pause
import pillswaterreminder.shared.generated.resources.action_restore
import pillswaterreminder.shared.generated.resources.action_resume
import pillswaterreminder.shared.generated.resources.action_show
import pillswaterreminder.shared.generated.resources.cd_more_actions
import pillswaterreminder.shared.generated.resources.delete_dialog_body
import pillswaterreminder.shared.generated.resources.delete_dialog_title
import pillswaterreminder.shared.generated.resources.ic_add
import pillswaterreminder.shared.generated.resources.ic_more_vert
import pillswaterreminder.shared.generated.resources.label_archived
import pillswaterreminder.shared.generated.resources.label_paused
import pillswaterreminder.shared.generated.resources.meds_count
import pillswaterreminder.shared.generated.resources.meds_empty_body
import pillswaterreminder.shared.generated.resources.section_active
import pillswaterreminder.shared.generated.resources.section_archived
import pillswaterreminder.shared.generated.resources.section_paused
import pillswaterreminder.shared.generated.resources.title_medications
import pillswaterreminder.shared.generated.resources.today_empty_title

@Composable
fun MedicationsScreen(
    onAddMedication: () -> Unit,
    onOpenMedication: (Long) -> Unit,
    viewModel: MedicationsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showArchived by rememberSaveable { mutableStateOf(false) }
    var confirmDeleteId by rememberSaveable { mutableStateOf<Long?>(null) }
    val actions = MedicationActions(
        onOpen = onOpenMedication,
        onPause = viewModel::pause,
        onResume = viewModel::resume,
        onDelete = { confirmDeleteId = it },
        onRestore = viewModel::restore,
    )

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = AppTheme.spacing.l, end = AppTheme.spacing.l, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.m),
        ) {
            item {
                ScreenHeader(
                    title = stringResource(Res.string.title_medications),
                    overline = pluralStringResource(Res.plurals.meds_count, state.listedCount, state.listedCount),
                )
            }
            if (state.loaded && state.listedCount == 0) {
                item {
                    PlaceholderCard(
                        title = stringResource(Res.string.today_empty_title),
                        body = stringResource(Res.string.meds_empty_body),
                    )
                }
            }
            section("active", Res.string.section_active, state.active, state.today, actions)
            section("paused", Res.string.section_paused, state.paused, state.today, actions)
            if (state.archived.isNotEmpty()) {
                item(key = "archived-header") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stringResource(Res.string.section_archived, state.archived.size),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(onClick = { showArchived = !showArchived }) {
                            Text(stringResource(if (showArchived) Res.string.action_hide else Res.string.action_show))
                        }
                    }
                }
                if (showArchived) {
                    items(state.archived, key = { it.medication.id }) { item ->
                        MedicationCard(item, state.today, actions)
                    }
                }
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

    val deleting = confirmDeleteId?.let { id -> (state.active + state.paused).firstOrNull { it.medication.id == id } }
    if (deleting != null) {
        AlertDialog(
            onDismissRequest = { confirmDeleteId = null },
            title = { Text(stringResource(Res.string.delete_dialog_title, deleting.medication.name)) },
            text = { Text(stringResource(Res.string.delete_dialog_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.delete(deleting.medication.id)
                        confirmDeleteId = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) {
                    Text(stringResource(Res.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteId = null }) { Text(stringResource(Res.string.action_cancel)) }
            },
        )
    }
}

private class MedicationActions(
    val onOpen: (Long) -> Unit,
    val onPause: (Long) -> Unit,
    val onResume: (Long) -> Unit,
    val onDelete: (Long) -> Unit,
    val onRestore: (Long) -> Unit,
)

private fun LazyListScope.section(
    key: String,
    title: StringResource,
    items: List<MedicationOverview>,
    today: LocalDate?,
    actions: MedicationActions,
) {
    if (items.isEmpty()) return
    item(key = "header-$key") {
        Text(
            stringResource(title),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = AppTheme.spacing.s),
        )
    }
    items(items, key = { it.medication.id }) { item ->
        MedicationCard(item, today, actions)
    }
}

@Composable
private fun MedicationCard(item: MedicationOverview, today: LocalDate?, actions: MedicationActions) {
    val medication = item.medication
    val archived = medication.status == MedicationStatus.ARCHIVED
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !archived) { actions.onOpen(medication.id) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(Modifier.height(IntrinsicSize.Min)) {
            Box(
                Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(medication.color.toColor()),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = AppTheme.spacing.l, top = AppTheme.spacing.m, bottom = AppTheme.spacing.m)
                    .alpha(if (medication.status == MedicationStatus.ACTIVE) 1f else 0.7f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(medication.name, style = MaterialTheme.typography.titleMedium)
                Text(medication.dose, style = MaterialTheme.typography.bodyMedium)
                item.schedule?.let {
                    Text(
                        scheduleSummary(it),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                val statusLine = when {
                    medication.status == MedicationStatus.PAUSED -> stringResource(Res.string.label_paused)
                    archived -> stringResource(Res.string.label_archived)
                    item.nextDoseAt != null && today != null -> nextDoseLabel(item.nextDoseAt, today)
                    else -> null
                }
                if (statusLine != null) {
                    Text(
                        statusLine,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (medication.status == MedicationStatus.ACTIVE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            MedicationMenu(item, actions)
        }
    }
}

@Composable
private fun MedicationMenu(item: MedicationOverview, actions: MedicationActions) {
    var open by remember { mutableStateOf(false) }
    val id = item.medication.id
    Box {
        IconButton(onClick = { open = true }) {
            Icon(painterResource(Res.drawable.ic_more_vert), contentDescription = stringResource(Res.string.cd_more_actions))
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            fun pick(action: (Long) -> Unit) {
                open = false
                action(id)
            }
            when (item.medication.status) {
                MedicationStatus.ACTIVE -> {
                    DropdownMenuItem(text = { Text(stringResource(Res.string.action_edit)) }, onClick = { pick(actions.onOpen) })
                    DropdownMenuItem(text = { Text(stringResource(Res.string.action_pause)) }, onClick = { pick(actions.onPause) })
                    DropdownMenuItem(
                        text = { Text(stringResource(Res.string.action_delete), color = MaterialTheme.colorScheme.error) },
                        onClick = { pick(actions.onDelete) },
                    )
                }
                MedicationStatus.PAUSED -> {
                    DropdownMenuItem(text = { Text(stringResource(Res.string.action_edit)) }, onClick = { pick(actions.onOpen) })
                    DropdownMenuItem(text = { Text(stringResource(Res.string.action_resume)) }, onClick = { pick(actions.onResume) })
                    DropdownMenuItem(
                        text = { Text(stringResource(Res.string.action_delete), color = MaterialTheme.colorScheme.error) },
                        onClick = { pick(actions.onDelete) },
                    )
                }
                MedicationStatus.ARCHIVED -> {
                    DropdownMenuItem(text = { Text(stringResource(Res.string.action_restore)) }, onClick = { pick(actions.onRestore) })
                }
            }
        }
    }
}
