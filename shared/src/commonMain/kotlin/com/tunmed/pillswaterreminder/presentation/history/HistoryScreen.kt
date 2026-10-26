package com.tunmed.pillswaterreminder.presentation.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tunmed.pillswaterreminder.presentation.components.PlaceholderCard
import com.tunmed.pillswaterreminder.presentation.components.ScreenHeader
import com.tunmed.pillswaterreminder.presentation.theme.AppTheme
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pillswaterreminder.shared.generated.resources.Res
import pillswaterreminder.shared.generated.resources.title_history

@Composable
fun HistoryScreen(
    @Suppress("UNUSED_PARAMETER") viewModel: HistoryViewModel = koinViewModel(),
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = AppTheme.spacing.l),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.l),
    ) {
        ScreenHeader(title = stringResource(Res.string.title_history), overline = "Last 30 days")
        PlaceholderCard(title = "Adherence", body = "Stats, month calendar and day log (Phase 3). Figma: 06 History.")
    }
}
