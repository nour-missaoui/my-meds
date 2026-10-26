package com.tunmed.pillswaterreminder

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tunmed.pillswaterreminder.presentation.AppViewModel
import com.tunmed.pillswaterreminder.presentation.navigation.AppNavHost
import com.tunmed.pillswaterreminder.presentation.theme.AppTheme
import org.koin.compose.viewmodel.koinViewModel

/** Root composable shared by every platform. Koin must be started before this runs. */
@Composable
fun App() {
    AppTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            val appViewModel: AppViewModel = koinViewModel()
            val onboardingDone by appViewModel.onboardingDone.collectAsStateWithLifecycle()
            val done = onboardingDone
            if (done != null) {
                // Decide the start destination once; later changes are handled by navigation.
                val startWithOnboarding = remember { !done }
                AppNavHost(startWithOnboarding = startWithOnboarding)
            }
        }
    }
}
