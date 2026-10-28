package com.tunmed.pillswaterreminder.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tunmed.pillswaterreminder.core.time.TimeProvider
import com.tunmed.pillswaterreminder.domain.model.DoseStatus
import com.tunmed.pillswaterreminder.domain.model.HistoryEntry
import com.tunmed.pillswaterreminder.domain.model.HistoryMonth
import com.tunmed.pillswaterreminder.domain.usecase.dose.CorrectDoseLogUseCase
import com.tunmed.pillswaterreminder.domain.usecase.history.ObserveHistoryUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

data class HistoryUiState(
    val month: HistoryMonth? = null,
    val selected: LocalDate? = null,
    val today: LocalDate? = null,
) {
    val canGoNext: Boolean get() = month != null && today != null && month.firstDay < firstOfMonth(today)
}

fun firstOfMonth(date: LocalDate): LocalDate = LocalDate(date.year, date.month, 1)

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(
    private val observeHistory: ObserveHistoryUseCase,
    private val correctDoseLog: CorrectDoseLogUseCase,
    private val timeProvider: TimeProvider,
) : ViewModel() {

    private val today = MutableStateFlow(timeProvider.today())
    private val month = MutableStateFlow(firstOfMonth(timeProvider.today()))
    private val selected = MutableStateFlow<LocalDate?>(timeProvider.today())

    val state: StateFlow<HistoryUiState> = combine(
        month.flatMapLatest { observeHistory(it) },
        selected,
        today,
    ) { history, day, now ->
        HistoryUiState(month = history, selected = day, today = now)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoryUiState())

    fun refresh() {
        today.value = timeProvider.today()
    }

    fun previousMonth() = showMonth(month.value.minus(1, DateTimeUnit.MONTH))

    fun nextMonth() {
        val next = month.value.plus(1, DateTimeUnit.MONTH)
        if (next <= firstOfMonth(today.value)) showMonth(next)
    }

    fun select(date: LocalDate) {
        if (date <= today.value) selected.value = date
    }

    fun correct(entry: HistoryEntry, status: DoseStatus) {
        viewModelScope.launch { correctDoseLog(entry.log, status) }
    }

    private fun showMonth(first: LocalDate) {
        month.value = first
        val current = today.value
        selected.value = if (firstOfMonth(current) == first) current else null
    }
}
