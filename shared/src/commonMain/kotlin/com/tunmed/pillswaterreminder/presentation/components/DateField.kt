package com.tunmed.pillswaterreminder.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import pillswaterreminder.shared.generated.resources.Res
import pillswaterreminder.shared.generated.resources.action_cancel
import pillswaterreminder.shared.generated.resources.action_ok
import pillswaterreminder.shared.generated.resources.ic_arrow_drop_down

@Composable
fun DateField(
    label: String,
    date: LocalDate?,
    onDateChange: (LocalDate?) -> Unit,
    modifier: Modifier = Modifier,
    emptyText: String = "",
    clearLabel: String? = null,
    initialPickerDate: LocalDate? = null,
) {
    var picking by rememberSaveable { mutableStateOf(false) }
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.weight(1f)) {
            OutlinedTextField(
                value = date?.let { dateLabel(it) } ?: emptyText,
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                label = { Text(label) },
                trailingIcon = { Icon(painterResource(Res.drawable.ic_arrow_drop_down), contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
            )
            Box(
                Modifier
                    .matchParentSize()
                    .clickable(onClickLabel = label, role = Role.Button) { picking = true },
            )
        }
        if (clearLabel != null && date != null) {
            TextButton(onClick = { onDateChange(null) }) { Text(clearLabel) }
        }
    }
    if (picking) {
        DatePickerDialogFor(
            initial = date ?: initialPickerDate,
            onDismiss = { picking = false },
            onConfirm = { picked ->
                picking = false
                onDateChange(picked)
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialogFor(initial: LocalDate?, onDismiss: () -> Unit, onConfirm: (LocalDate) -> Unit) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initial?.atStartOfDayIn(TimeZone.UTC)?.toEpochMilliseconds(),
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val millis = state.selectedDateMillis
                    if (millis == null) onDismiss() else onConfirm(Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC).date)
                },
            ) {
                Text(stringResource(Res.string.action_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.action_cancel)) }
        },
    ) {
        DatePicker(state = state)
    }
}
