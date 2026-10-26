package com.tunmed.pillswaterreminder.presentation.navigation

import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import pillswaterreminder.shared.generated.resources.Res
import pillswaterreminder.shared.generated.resources.ic_nav_history
import pillswaterreminder.shared.generated.resources.ic_nav_meds
import pillswaterreminder.shared.generated.resources.ic_nav_settings
import pillswaterreminder.shared.generated.resources.ic_nav_today
import pillswaterreminder.shared.generated.resources.nav_history
import pillswaterreminder.shared.generated.resources.nav_meds
import pillswaterreminder.shared.generated.resources.nav_settings
import pillswaterreminder.shared.generated.resources.nav_today

enum class TopLevelDestination(
    val route: Any,
    val label: StringResource,
    val icon: DrawableResource,
) {
    TODAY(TodayRoute, Res.string.nav_today, Res.drawable.ic_nav_today),
    MEDS(MedicationsRoute, Res.string.nav_meds, Res.drawable.ic_nav_meds),
    HISTORY(HistoryRoute, Res.string.nav_history, Res.drawable.ic_nav_history),
    SETTINGS(SettingsRoute, Res.string.nav_settings, Res.drawable.ic_nav_settings),
}
