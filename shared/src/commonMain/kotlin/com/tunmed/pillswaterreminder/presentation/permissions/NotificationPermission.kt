package com.tunmed.pillswaterreminder.presentation.permissions

import androidx.compose.runtime.Composable

@Composable
expect fun rememberNotificationPermissionRequest(onResult: (Boolean) -> Unit): () -> Unit
