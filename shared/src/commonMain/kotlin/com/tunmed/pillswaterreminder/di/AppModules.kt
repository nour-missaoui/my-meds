package com.tunmed.pillswaterreminder.di

import com.tunmed.pillswaterreminder.core.time.SystemTimeProvider
import com.tunmed.pillswaterreminder.core.time.TimeProvider
import com.tunmed.pillswaterreminder.data.local.db.AppDatabase
import com.tunmed.pillswaterreminder.data.repository.DoseLogRepositoryImpl
import com.tunmed.pillswaterreminder.data.repository.MedicationRepositoryImpl
import com.tunmed.pillswaterreminder.data.repository.SettingsRepositoryImpl
import com.tunmed.pillswaterreminder.domain.reminder.ScheduleEngine
import com.tunmed.pillswaterreminder.domain.repository.DoseLogRepository
import com.tunmed.pillswaterreminder.domain.repository.MedicationRepository
import com.tunmed.pillswaterreminder.domain.repository.SettingsRepository
import com.tunmed.pillswaterreminder.domain.usecase.dose.RecordDoseActionUseCase
import com.tunmed.pillswaterreminder.domain.usecase.medication.ObserveMedicationUseCase
import com.tunmed.pillswaterreminder.domain.usecase.medication.ObserveMedicationsUseCase
import com.tunmed.pillswaterreminder.domain.usecase.medication.SaveMedicationUseCase
import com.tunmed.pillswaterreminder.domain.usecase.medication.SetMedicationStatusUseCase
import com.tunmed.pillswaterreminder.domain.usecase.reminder.RescheduleAllUseCase
import com.tunmed.pillswaterreminder.presentation.AppViewModel
import com.tunmed.pillswaterreminder.presentation.history.HistoryViewModel
import com.tunmed.pillswaterreminder.presentation.medications.EditMedicationViewModel
import com.tunmed.pillswaterreminder.presentation.medications.MedicationsViewModel
import com.tunmed.pillswaterreminder.presentation.onboarding.OnboardingViewModel
import com.tunmed.pillswaterreminder.presentation.settings.SettingsViewModel
import com.tunmed.pillswaterreminder.presentation.today.TodayViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/*
 * Dependency rule: presentation -> domain <- data. Only this file (and the platform
 * module) knows the concrete classes. The platform module must provide:
 *   AppDatabase, DataStore<Preferences>, ReminderScheduler.
 */

val coreModule = module {
    single<TimeProvider> { SystemTimeProvider() }
}

val dataModule = module {
    single { get<AppDatabase>().medicationDao() }
    single { get<AppDatabase>().doseLogDao() }

    single<MedicationRepository> { MedicationRepositoryImpl(get()) }
    single<DoseLogRepository> { DoseLogRepositoryImpl(get()) }
    single<SettingsRepository> { SettingsRepositoryImpl(get()) }
}

val domainModule = module {
    single { ScheduleEngine() }

    factoryOf(::RescheduleAllUseCase)

    factoryOf(::ObserveMedicationsUseCase)
    factoryOf(::ObserveMedicationUseCase)
    factoryOf(::SaveMedicationUseCase)
    factoryOf(::SetMedicationStatusUseCase)

    factoryOf(::RecordDoseActionUseCase)
}

val presentationModule = module {
    viewModelOf(::AppViewModel)
    viewModelOf(::TodayViewModel)
    viewModelOf(::MedicationsViewModel)
    viewModel { params ->
        EditMedicationViewModel(
            medicationId = params.get(),
            observeMedication = get(),
            saveMedication = get(),
            timeProvider = get(),
        )
    }
    viewModelOf(::HistoryViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::OnboardingViewModel)
}

/** Everything except the platform module. */
fun sharedModules(): List<Module> = listOf(coreModule, dataModule, domainModule, presentationModule)
