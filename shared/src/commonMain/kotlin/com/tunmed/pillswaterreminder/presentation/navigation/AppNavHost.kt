package com.tunmed.pillswaterreminder.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.tunmed.pillswaterreminder.presentation.history.HistoryScreen
import com.tunmed.pillswaterreminder.presentation.medications.EditMedicationScreen
import com.tunmed.pillswaterreminder.presentation.medications.MedicationsScreen
import com.tunmed.pillswaterreminder.presentation.onboarding.OnboardingScreen
import com.tunmed.pillswaterreminder.presentation.settings.SettingsScreen
import com.tunmed.pillswaterreminder.presentation.today.TodayScreen
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AppNavHost(
    startWithOnboarding: Boolean,
    navController: NavHostController = rememberNavController(),
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val selectedTab = TopLevelDestination.entries.firstOrNull { tab ->
        currentDestination?.hierarchy?.any { it.hasRoute(tab.route::class) } == true
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (selectedTab != null) {
                AppBottomBar(selected = selectedTab, onSelect = { navController.navigateToTab(it) })
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (startWithOnboarding) OnboardingRoute else TodayRoute,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable<OnboardingRoute> {
                OnboardingScreen(
                    onFinished = {
                        navController.navigate(TodayRoute) {
                            popUpTo<OnboardingRoute> { inclusive = true }
                        }
                    },
                )
            }
            composable<TodayRoute> {
                TodayScreen(onAddMedication = { navController.navigate(EditMedicationRoute()) })
            }
            composable<MedicationsRoute> {
                MedicationsScreen(
                    onAddMedication = { navController.navigate(EditMedicationRoute()) },
                    onOpenMedication = { id -> navController.navigate(EditMedicationRoute(id)) },
                )
            }
            composable<EditMedicationRoute> { entry ->
                val route = entry.toRoute<EditMedicationRoute>()
                EditMedicationScreen(
                    medicationId = route.medicationId,
                    onBack = { navController.popBackStack() },
                )
            }
            composable<HistoryRoute> {
                HistoryScreen()
            }
            composable<SettingsRoute> {
                SettingsScreen()
            }
        }
    }
}

private fun NavHostController.navigateToTab(tab: TopLevelDestination) {
    navigate(tab.route) {
        popUpTo<TodayRoute> { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun AppBottomBar(
    selected: TopLevelDestination,
    onSelect: (TopLevelDestination) -> Unit,
) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        TopLevelDestination.entries.forEach { tab ->
            NavigationBarItem(
                selected = tab == selected,
                onClick = { onSelect(tab) },
                icon = { Icon(painterResource(tab.icon), contentDescription = null) },
                label = { Text(stringResource(tab.label)) },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }
    }
}
