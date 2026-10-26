package com.tunmed.pillswaterreminder.di

import com.tunmed.pillswaterreminder.core.time.SystemTimeProvider
import com.tunmed.pillswaterreminder.core.time.TimeProvider
import com.tunmed.pillswaterreminder.data.local.db.AppDatabase
import com.tunmed.pillswaterreminder.data.repository.DoseLogRepositoryImpl
import com.tunmed.pillswaterreminder.data.repository.MedicationRepositoryImpl
import com.tunmed.pillswaterreminder.data.repository.ReminderStateRepositoryImpl
import com.tunmed.pillswaterreminder.data.repository.SettingsRepositoryImpl
import com.tunmed.pillswaterreminder.domain.reminder.ScheduleEngine
import com.tunmed.pillswaterreminder.domain.repository.DoseLogRepository
import com.tunmed.pillswaterreminder.domain.repository.MedicationRepository
import com.tunmed.pillswaterreminder.domain.repository.ReminderStateRepository
import com.tunmed.pillswaterreminder.domain.repository.SettingsRepository
import com.tunmed.pillswaterreminder.domain.usecase.dose.RecordDoseActionUseCase
import com.tunmed.pillswaterreminder.domain.usecase.medication.DeleteMedicationUseCase
import com.tunmed.pillswaterreminder.domain.usecase.medication.GetCurrentScheduleUseCase
import com.tunmed.pillswaterreminder.domain.usecase.medication.ObserveMedicationUseCase
import com.tunmed.pillswaterreminder.domain.usecase.medication.ObserveMedicationsUseCase
import com.tunmed.pillswaterreminder.domain.usecase.medication.SaveMedicationUseCase
import com.tunmed.pillswaterreminder.domain.usecase.medication.SetMedicationStatusUseCase
import com.tunmed.pillswaterreminder.domain.usecase.reminder.RescheduleAllUseCase
import com.tunmed.pillswaterreminder.domain.usecase.today.ObserveTodayDosesUseCase
import com.tunmed.pillswaterreminder.presentation.AppViewModel
import com.tunmed.pillswaterreminder.presentation.history.HistoryViewModel
import com.tunmed.pillswaterreminder.presentation.medications.EditMedicationViewModel
import com.tunmed.pillswaterreminder.presentation.medications.MedicationsViewModel
import com.tunmed.pillswaterreminder.presentation.onboarding.OnboardingViewModel
import com.tunmed.pillswaterreminder.presentation.settings.SettingsViewModel
import com.tunmed.pillswaterreminder.presentation.today.TodayViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module


val coreModule = module {
    single<TimeProvider> { SystemTimeProvider() }
}

val dataModule = module {
    single { get<AppDatabase>().medicationDao() }
    single { get<AppDatabase>().doseLogDao() }

    single<MedicationRepository> { MedicationRepositoryImpl(get()) }
    single<DoseLogRepository> { DoseLogRepositoryImpl(get()) }
    single<SettingsRepository> { SettingsRepositoryImpl(get()) }
    single<ReminderStateRepository> { ReminderStateRepositoryImpl(get()) }
}

val domainModule = module {
    single { ScheduleEngine() }

    singleOf(::RescheduleAllUseCase)

    factoryOf(::ObserveMedicationsUseCase)
    factoryOf(::ObserveMedicationUseCase)
    factoryOf(::SaveMedicationUseCase)
    factoryOf(::SetMedicationStatusUseCase)
    factoryOf(::GetCurrentScheduleUseCase)
    factoryOf(::DeleteMedicationUseCase)

    factoryOf(::ObserveTodayDosesUseCase)

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
            getCurrentSchedule = get(),
            saveMedication = get(),
            deleteMedication = get(),
            timeProvider = get(),
        )
    }
    viewModelOf(::HistoryViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::OnboardingViewModel)
}

fun sharedModules(): List<Module> = listOf(coreModule, dataModule, domainModule, presentationModule)
