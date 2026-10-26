# Architecture — Meds Reminder

Kotlin Multiplatform + Compose Multiplatform. **Android only for now**; iOS targets stay in
the build but are not wired (Koin is only started on Android).

> **Scope: medications only** (water reminders dropped 26 Oct 2026, water code removed the same day).
> The product spec (requirement ids M-*, S-*, R-*, D-*) lives in the plan doc "Meds Reminder — Plan & Spec".

## Modules

| Module | Role |
| --- | --- |
| `androidApp` | Android entry point only: `MedsReminderApp` (starts Koin, creates notification channels, schedules the daily worker), `MainActivity`, `AndroidManifest.xml` (permissions + receivers). |
| `shared` | Everything else. `commonMain` = UI, ViewModels, domain, data. `androidMain` = AlarmManager, receivers, WorkManager, Room/DataStore builders. |
| `iosApp` | Untouched template (later phase). |

## Layers (package `com.tunmed.pillswaterreminder`)

```
presentation  ──►  domain  ◄──  data
     (UI)        (pure Kotlin)   (Room 3, DataStore)
                      ▲
                 platform (androidMain: alarms, notifications)
```

Dependency rule: `domain` imports nothing from `data`, `presentation` or Android.
Only `di/` knows concrete classes.

```
shared/src/commonMain/kotlin/com/tunmed/pillswaterreminder/
├── App.kt                      root composable (theme + start destination)
├── core/time/                  TimeProvider (the only place that reads the clock)
├── domain/
│   ├── model/                  Medication, MedSchedule, RecurrenceRule, ScheduledMedication,
│   │                           DoseLog, AppSettings, ReminderOccurrence
│   ├── repository/             interfaces (Medication, DoseLog, Settings)
│   ├── reminder/               ScheduleEngine (pure rules), ReminderScheduler (port)
│   └── usecase/                medication/, dose/, reminder/
├── data/
│   ├── local/db/               AppDatabase (Room 3), DatabaseFactory
│   ├── local/entity/           Room entities (flat columns, no type converters)
│   ├── local/dao/              MedicationDao, DoseLogDao
│   ├── local/settings/         DataStore factory
│   ├── mapper/                 entity <-> domain
│   └── repository/             *RepositoryImpl
├── presentation/
│   ├── theme/                  colours, type, shapes, spacing = Figma tokens
│   ├── navigation/             type-safe routes, NavHost, bottom bar
│   ├── components/             shared UI (ScreenHeader, …)
│   └── today/ medications/ history/ settings/ onboarding/
│                               one folder per feature: Screen + ViewModel (+ UiState)
└── di/AppModules.kt            Koin modules

shared/src/androidMain/kotlin/com/tunmed/pillswaterreminder/
├── di/AndroidPlatformModule.kt AppDatabase, DataStore, ReminderScheduler
└── platform/
    ├── database/  settings/    Android builders
    ├── reminder/               AndroidReminderScheduler, ReminderReceiver,
    │                           ReminderActionReceiver, SystemEventsReceiver, RescheduleWorker
    └── notification/           NotificationChannels
```

## Conventions

- One use case = one class with `operator fun invoke`. ViewModels call use cases, never DAOs.
- Every write that changes reminders ends with `RescheduleAllUseCase` (single next alarm).
- Tests and commits reference spec ids (e.g. `S-4`, `R-3`) from the plan doc.
- Times stored as `"08:00,10:30"`, dates as ISO `"2026-10-15"`, instants as epoch millis.
- Medication edits create a new `MedSchedule` version; never update a schedule in place.
- UI copy goes in `composeResources/values/strings.xml` (ready for FR / AR later).
- Room schemas are exported to `shared/schemas/` — commit them.

## Status of the skeleton

| Area | State |
| --- | --- |
| Gradle, DI, Room, DataStore, navigation, theme | wired |
| Today | lists ACTIVE medications; dose timeline comes in Phase 3 |
| Add medication | minimal form (name + dose, saved as Daily 08:00) |
| `ScheduleEngine.isDueOn / occurrencesOn / nextAfter` | `TODO` — Phase 1 (S-1 to S-7), tests in `commonTest` are `@Ignore`d |
| `RescheduleAllUseCase`, notifications, Taken / Snooze / Skip | no-op / TODO — Phase 2 (R-1 to R-7) |
| Final screens from Figma | placeholders — Phase 3 |

Run: `./gradlew :androidApp:assembleDebug` · tests: `./gradlew :shared:testAndroidHostTest`
