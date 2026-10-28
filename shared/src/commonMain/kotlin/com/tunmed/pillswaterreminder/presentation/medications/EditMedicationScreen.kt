package com.tunmed.pillswaterreminder.presentation.medications

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tunmed.pillswaterreminder.core.time.parseTime
import com.tunmed.pillswaterreminder.core.time.toHhMm
import com.tunmed.pillswaterreminder.domain.model.MedicationColor
import com.tunmed.pillswaterreminder.domain.model.MedicationForm
import com.tunmed.pillswaterreminder.domain.model.TimeDose
import com.tunmed.pillswaterreminder.domain.reminder.DoseTimes
import com.tunmed.pillswaterreminder.presentation.components.DateField
import com.tunmed.pillswaterreminder.presentation.components.ScreenHeader
import com.tunmed.pillswaterreminder.presentation.components.SelectField
import com.tunmed.pillswaterreminder.presentation.theme.AppTheme
import com.tunmed.pillswaterreminder.presentation.theme.toColor
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import pillswaterreminder.shared.generated.resources.Res
import pillswaterreminder.shared.generated.resources.action_cancel
import pillswaterreminder.shared.generated.resources.action_delete
import pillswaterreminder.shared.generated.resources.action_delete_medication
import pillswaterreminder.shared.generated.resources.action_ok
import pillswaterreminder.shared.generated.resources.action_remove
import pillswaterreminder.shared.generated.resources.action_save
import pillswaterreminder.shared.generated.resources.amount_error
import pillswaterreminder.shared.generated.resources.cd_fewer_times
import pillswaterreminder.shared.generated.resources.cd_more_times
import pillswaterreminder.shared.generated.resources.colour_amber
import pillswaterreminder.shared.generated.resources.colour_aqua
import pillswaterreminder.shared.generated.resources.colour_coral
import pillswaterreminder.shared.generated.resources.colour_green
import pillswaterreminder.shared.generated.resources.colour_grey
import pillswaterreminder.shared.generated.resources.colour_red
import pillswaterreminder.shared.generated.resources.delete_dialog_body
import pillswaterreminder.shared.generated.resources.delete_dialog_title
import pillswaterreminder.shared.generated.resources.dose_mode_details
import pillswaterreminder.shared.generated.resources.dose_mode_quick
import pillswaterreminder.shared.generated.resources.dose_per_time_hint
import pillswaterreminder.shared.generated.resources.dose_preview
import pillswaterreminder.shared.generated.resources.dose_quick_placeholder
import pillswaterreminder.shared.generated.resources.end_before_start
import pillswaterreminder.shared.generated.resources.end_date_note
import pillswaterreminder.shared.generated.resources.ic_add
import pillswaterreminder.shared.generated.resources.ic_remove
import pillswaterreminder.shared.generated.resources.label_amount
import pillswaterreminder.shared.generated.resources.label_colour
import pillswaterreminder.shared.generated.resources.label_dose
import pillswaterreminder.shared.generated.resources.label_dose_per_time
import pillswaterreminder.shared.generated.resources.label_end_date
import pillswaterreminder.shared.generated.resources.label_form
import pillswaterreminder.shared.generated.resources.label_name
import pillswaterreminder.shared.generated.resources.label_note
import pillswaterreminder.shared.generated.resources.label_start_date
import pillswaterreminder.shared.generated.resources.label_strength
import pillswaterreminder.shared.generated.resources.label_times_per_day
import pillswaterreminder.shared.generated.resources.label_unit
import pillswaterreminder.shared.generated.resources.no_end_date
import pillswaterreminder.shared.generated.resources.note_placeholder
import pillswaterreminder.shared.generated.resources.strength_placeholder
import pillswaterreminder.shared.generated.resources.time_picker_title
import pillswaterreminder.shared.generated.resources.times_error
import pillswaterreminder.shared.generated.resources.times_hint
import pillswaterreminder.shared.generated.resources.title_edit_medication
import pillswaterreminder.shared.generated.resources.title_new_medication

@Composable
fun EditMedicationScreen(
    medicationId: Long,
    onBack: () -> Unit,
    viewModel: EditMedicationViewModel = koinViewModel(parameters = { parametersOf(medicationId) }),
) {
    val medication by viewModel.medication.collectAsStateWithLifecycle()
    val schedule by viewModel.schedule.collectAsStateWithLifecycle()
    var name by rememberSaveable { mutableStateOf("") }
    var detailed by rememberSaveable { mutableStateOf(false) }
    var doseText by rememberSaveable { mutableStateOf("") }
    var formKey by rememberSaveable { mutableStateOf(MedicationForm.TABLET.name) }
    var unitKey by rememberSaveable { mutableStateOf(MedicationForm.TABLET.defaultUnit.name) }
    var amountText by rememberSaveable { mutableStateOf("1") }
    var strength by rememberSaveable { mutableStateOf("") }
    var times by rememberSaveable { mutableStateOf(if (viewModel.isNew) "08:00" else "") }
    var editingTime by rememberSaveable { mutableStateOf(-1) }
    var scheduleDraft by rememberSaveable(stateSaver = ScheduleDraftSaver) {
        mutableStateOf(ScheduleDraft.startingOn(viewModel.today))
    }
    var perTime by rememberSaveable { mutableStateOf(false) }
    var startDateText by rememberSaveable { mutableStateOf(viewModel.today.toString()) }
    var endDateText by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    var colorKey by rememberSaveable { mutableStateOf(MedicationColor.CORAL.name) }
    val perTimeAmounts = rememberSaveable(saver = StringMapSaver) { mutableStateMapOf<String, String>() }
    val perTimeTexts = rememberSaveable(saver = StringMapSaver) { mutableStateMapOf<String, String>() }
    var medicationLoaded by rememberSaveable { mutableStateOf(false) }
    var scheduleLoaded by rememberSaveable { mutableStateOf(false) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(medication) {
        val loaded = medication
        if (loaded != null && !medicationLoaded) {
            name = loaded.name
            doseText = loaded.dose
            if (loaded.form != MedicationForm.OTHER || loaded.hasDoseDetails) formKey = loaded.form.name
            unitKey = (loaded.doseUnit ?: loaded.form.defaultUnit).name
            loaded.doseAmount?.let { amountText = formatDoseAmount(it) }
            strength = loaded.strength.orEmpty()
            detailed = loaded.hasDoseDetails
            startDateText = loaded.startDate.toString()
            endDateText = loaded.endDate?.toString().orEmpty()
            note = loaded.note.orEmpty()
            colorKey = loaded.color.name
            medicationLoaded = true
        }
    }
    LaunchedEffect(schedule) {
        val loaded = schedule
        if (loaded != null && !scheduleLoaded) {
            times = loaded.times.encodeTimes()
            scheduleDraft = ScheduleDraft.of(loaded.rule, loaded.headsUpDays, viewModel.today)
            loaded.doseByTime.forEach { (time, dose) ->
                val key = time.toHhMm()
                perTimeTexts[key] = dose.text
                dose.amount?.let { perTimeAmounts[key] = formatDoseAmount(it) }
            }
            perTime = loaded.doseByTime.isNotEmpty()
            scheduleLoaded = true
        }
    }

    val form = MedicationForm.entries.firstOrNull { it.name == formKey } ?: MedicationForm.TABLET
    val unit = form.units.firstOrNull { it.name == unitKey } ?: form.defaultUnit

    val startDate = LocalDate.parse(startDateText)
    val endDate = endDateText.takeIf { it.isNotBlank() }?.let { LocalDate.parse(it) }
    val datesValid = endDate == null || endDate >= startDate
    val color = MedicationColor.entries.firstOrNull { it.name == colorKey } ?: MedicationColor.CORAL
    val rule = scheduleDraft.copy(anchor = startDate).toRule()
    val usesTimes = scheduleDraft.kind.hasTimes
    val doseTimes = times.split(',').mapNotNull { parseTime(it) }
    val hasDuplicateTimes = doseTimes.size != doseTimes.distinct().size
    val timesReady = !usesTimes || (doseTimes.isNotEmpty() && !hasDuplicateTimes)
    val varies = usesTimes && perTime && doseTimes.size > 1

    val singleAmount = parseDoseAmount(amountText)
    val amountInvalid = singleAmount == null && amountText.isNotBlank()
    val timeAmounts = doseTimes.map { parseDoseAmount(perTimeAmounts[it.toHhMm()] ?: amountText) }
    val timeTexts = doseTimes.mapIndexed { i, time ->
        if (detailed) {
            timeAmounts[i]?.let { doseDisplay(it, unit, strength) }
        } else {
            (perTimeTexts[time.toHhMm()] ?: doseText).trim().ifEmpty { null }
        }
    }
    val perTimeReady = timeTexts.all { it != null }
    val reallyVaries = varies && perTimeReady && timeTexts.distinct().size > 1

    val medicationDoseText: String? = when {
        varies && !perTimeReady -> null
        reallyVaries && detailed -> doseSummary(timeAmounts.filterNotNull(), unit, strength)
        reallyVaries -> timeTexts.filterNotNull().joinToString(" / ")
        varies -> timeTexts.firstOrNull()
        detailed -> singleAmount?.let { doseDisplay(it, unit, strength) }
        else -> doseText.trim().ifEmpty { null }
    }
    val medicationAmount: Double? = if (varies) timeAmounts.firstOrNull() else singleAmount
    val doseReady = medicationDoseText != null && (!detailed || medicationAmount != null)

    val title = if (viewModel.isNew) Res.string.title_new_medication else Res.string.title_edit_medication
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = AppTheme.spacing.l),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.l),
    ) {
        ScreenHeader(title = stringResource(title), onBack = onBack)
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(Res.string.label_name)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.s)) {
            FilterChip(
                selected = !detailed,
                onClick = { detailed = false },
                label = { Text(stringResource(Res.string.dose_mode_quick)) },
            )
            FilterChip(
                selected = detailed,
                onClick = { detailed = true },
                label = { Text(stringResource(Res.string.dose_mode_details)) },
            )
        }
        if (detailed) {
            SelectField(
                label = stringResource(Res.string.label_form),
                value = formLabel(form),
                options = MedicationForm.entries,
                optionLabel = { formLabel(it) },
                optionDetail = { option -> option.units.map { unitLabel(it) }.joinToString(", ") },
                onSelect = { selected ->
                    formKey = selected.name
                    unitKey = selected.defaultUnit.name
                },
                modifier = Modifier.fillMaxWidth(),
            )
            if (varies) {
                SelectField(
                    label = stringResource(Res.string.label_unit),
                    value = unitLabel(unit),
                    options = form.units,
                    optionLabel = { unitLabel(it) },
                    onSelect = { selected -> unitKey = selected.name },
                    selectable = form.units.size > 1,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.m)) {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text(stringResource(Res.string.label_amount)) },
                        isError = amountInvalid,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                    )
                    SelectField(
                        label = stringResource(Res.string.label_unit),
                        value = unitLabelFor(unit, singleAmount),
                        options = form.units,
                        optionLabel = { unitLabel(it) },
                        onSelect = { selected -> unitKey = selected.name },
                        selectable = form.units.size > 1,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (amountInvalid) {
                    Text(
                        stringResource(Res.string.amount_error),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
            OutlinedTextField(
                value = strength,
                onValueChange = { strength = it },
                label = { Text(stringResource(Res.string.label_strength)) },
                placeholder = { Text(stringResource(Res.string.strength_placeholder)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                stringResource(Res.string.dose_preview, medicationDoseText ?: "—"),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else if (varies) {
            Text(
                stringResource(Res.string.dose_per_time_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            OutlinedTextField(
                value = doseText,
                onValueChange = { doseText = it },
                label = { Text(stringResource(Res.string.label_dose)) },
                placeholder = { Text(stringResource(Res.string.dose_quick_placeholder)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        ScheduleSection(draft = scheduleDraft, onChange = { scheduleDraft = it })

        DateField(
            label = stringResource(Res.string.label_start_date),
            date = startDate,
            onDateChange = { picked -> picked?.let { startDateText = it.toString() } },
            modifier = Modifier.fillMaxWidth(),
        )
        DateField(
            label = stringResource(Res.string.label_end_date),
            date = endDate,
            onDateChange = { picked -> endDateText = picked?.toString().orEmpty() },
            emptyText = stringResource(Res.string.no_end_date),
            clearLabel = stringResource(Res.string.action_remove),
            initialPickerDate = startDate,
            modifier = Modifier.fillMaxWidth(),
        )
        if (!datesValid) {
            Text(
                stringResource(Res.string.end_before_start),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        } else if (endDate != null) {
            Text(
                stringResource(Res.string.end_date_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (usesTimes) TimesPerDay(
            times = doseTimes,
            hasDuplicates = hasDuplicateTimes,
            perTime = perTime,
            onPerTimeChange = { perTime = it },
            onCountChange = { count -> times = DoseTimes.spread(count).encodeTimes() },
            onEditTime = { index -> editingTime = index },
            perTimeInput = { time ->
                val key = time.toHhMm()
                if (detailed) {
                    val value = perTimeAmounts[key] ?: amountText
                    val parsed = parseDoseAmount(value)
                    OutlinedTextField(
                        value = value,
                        onValueChange = { perTimeAmounts[key] = it },
                        isError = parsed == null,
                        suffix = { Text(unitLabelFor(unit, parsed)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    val value = perTimeTexts[key] ?: doseText
                    OutlinedTextField(
                        value = value,
                        onValueChange = { perTimeTexts[key] = it },
                        isError = value.isBlank(),
                        placeholder = { Text(stringResource(Res.string.dose_quick_placeholder)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                }
            },
        )

        ColourPicker(selected = color, onSelect = { colorKey = it.name })
        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text(stringResource(Res.string.label_note)) },
            placeholder = { Text(stringResource(Res.string.note_placeholder)) },
            maxLines = 3,
            modifier = Modifier.fillMaxWidth(),
        )

        Button(
            onClick = {
                val text = medicationDoseText
                val dose = when {
                    text == null -> null
                    detailed -> medicationAmount?.let { DoseInput.Detailed(form, it, unit, strength, text) }
                    else -> DoseInput.Quick(text)
                }
                val doseByTime: Map<LocalTime, TimeDose> = if (reallyVaries) {
                    doseTimes.mapIndexed { i, time ->
                        time to TimeDose(text = timeTexts[i].orEmpty(), amount = if (detailed) timeAmounts[i] else null)
                    }.toMap()
                } else {
                    emptyMap()
                }
                if (dose != null && rule != null && timesReady && datesValid) {
                    viewModel.save(
                        name = name,
                        dose = dose,
                        times = if (usesTimes) doseTimes.sorted() else emptyList(),
                        doseByTime = doseByTime,
                        rule = rule,
                        headsUpDays = scheduleDraft.headsUpToSave(),
                        startDate = startDate,
                        endDate = endDate,
                        note = note,
                        color = color,
                        onSaved = onBack,
                    )
                }
            },
            enabled = name.isNotBlank() && doseReady && rule != null && timesReady && datesValid,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(Res.string.action_save))
        }
        if (!viewModel.isNew) {
            OutlinedButton(
                onClick = { confirmDelete = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(Res.string.action_delete_medication))
            }
        }
        Spacer(Modifier.height(AppTheme.spacing.xl))
    }

    val timeBeingEdited = doseTimes.getOrNull(editingTime)
    if (timeBeingEdited != null) {
        DoseTimePickerDialog(
            initial = timeBeingEdited,
            onDismiss = { editingTime = -1 },
            onConfirm = { picked ->
                val index = editingTime
                val oldKey = timeBeingEdited.toHhMm()
                val newKey = picked.toHhMm()
                if (oldKey != newKey) {
                    perTimeAmounts.remove(oldKey)?.let { perTimeAmounts[newKey] = it }
                    perTimeTexts.remove(oldKey)?.let { perTimeTexts[newKey] = it }
                }
                times = doseTimes.mapIndexed { i, time -> if (i == index) picked else time }.sorted().encodeTimes()
                editingTime = -1
            },
        )
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(Res.string.delete_dialog_title, medication?.name ?: name)) },
            text = { Text(stringResource(Res.string.delete_dialog_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmDelete = false
                        viewModel.delete(onDeleted = onBack)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) {
                    Text(stringResource(Res.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) {
                    Text(stringResource(Res.string.action_cancel))
                }
            },
        )
    }
}

private const val TIMES_PER_ROW = 4
private val TIME_BUTTON_WIDTH = 88.dp

private fun List<LocalTime>.encodeTimes(): String = joinToString(",") { it.toHhMm() }

private val StringMapSaver: Saver<SnapshotStateMap<String, String>, Any> = listSaver(
    save = { map -> map.flatMap { (key, value) -> listOf(key, value) } },
    restore = { flat ->
        mutableStateMapOf<String, String>().apply { flat.chunked(2).forEach { (key, value) -> put(key, value) } }
    },
)

@Composable
private fun TimesPerDay(
    times: List<LocalTime>,
    hasDuplicates: Boolean,
    perTime: Boolean,
    onPerTimeChange: (Boolean) -> Unit,
    onCountChange: (Int) -> Unit,
    onEditTime: (Int) -> Unit,
    perTimeInput: @Composable RowScope.(LocalTime) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.s)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(Res.string.label_times_per_day),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = { onCountChange(times.size - 1) }, enabled = times.size > 1) {
                Icon(painterResource(Res.drawable.ic_remove), contentDescription = stringResource(Res.string.cd_fewer_times))
            }
            Text("${times.size}", style = MaterialTheme.typography.titleMedium)
            IconButton(onClick = { onCountChange(times.size + 1) }, enabled = times.size < DoseTimes.MAX_PER_DAY) {
                Icon(painterResource(Res.drawable.ic_add), contentDescription = stringResource(Res.string.cd_more_times))
            }
        }
        if (times.size > 1) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(Res.string.label_dose_per_time),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f),
                )
                Switch(checked = perTime, onCheckedChange = onPerTimeChange)
            }
        }
        if (perTime && times.size > 1) {
            times.forEachIndexed { index, time ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.m),
                ) {
                    TimeButton(time = time, onClick = { onEditTime(index) }, modifier = Modifier.width(TIME_BUTTON_WIDTH))
                    perTimeInput(time)
                }
            }
        } else {
            times.withIndex().chunked(TIMES_PER_ROW).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.s)) {
                    row.forEach { (index, time) ->
                        TimeButton(time = time, onClick = { onEditTime(index) }, modifier = Modifier.weight(1f))
                    }
                    repeat(TIMES_PER_ROW - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
        Text(
            stringResource(if (hasDuplicates) Res.string.times_error else Res.string.times_hint),
            style = MaterialTheme.typography.bodySmall,
            color = if (hasDuplicates) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TimeButton(time: LocalTime, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = AppTheme.spacing.xs),
        modifier = modifier,
    ) {
        Text(time.toHhMm())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DoseTimePickerDialog(
    initial: LocalTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit,
) {
    val state = rememberTimePickerState(initialHour = initial.hour, initialMinute = initial.minute, is24Hour = true)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.time_picker_title)) },
        text = { TimePicker(state = state) },
        confirmButton = {
            TextButton(onClick = { onConfirm(LocalTime(state.hour, state.minute)) }) {
                Text(stringResource(Res.string.action_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.action_cancel)) }
        },
    )
}

@Composable
private fun ColourPicker(selected: MedicationColor, onSelect: (MedicationColor) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.s)) {
        Text(stringResource(Res.string.label_colour), style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.m)) {
            MedicationColor.entries.forEach { option ->
                val name = stringResource(colourNameRes(option))
                Box(
                    Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(option.toColor())
                        .border(
                            width = if (option == selected) 3.dp else 0.dp,
                            color = if (option == selected) MaterialTheme.colorScheme.onSurface else option.toColor(),
                            shape = CircleShape,
                        )
                        .selectable(selected = option == selected, role = Role.RadioButton, onClick = { onSelect(option) })
                        .semantics { contentDescription = name },
                )
            }
        }
    }
}

private fun colourNameRes(color: MedicationColor): StringResource = when (color) {
    MedicationColor.CORAL -> Res.string.colour_coral
    MedicationColor.AQUA -> Res.string.colour_aqua
    MedicationColor.AMBER -> Res.string.colour_amber
    MedicationColor.GREEN -> Res.string.colour_green
    MedicationColor.RED -> Res.string.colour_red
    MedicationColor.GREY -> Res.string.colour_grey
}

