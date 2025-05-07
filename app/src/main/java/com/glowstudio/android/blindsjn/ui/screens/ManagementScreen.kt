package com.glowstudio.android.blindsjn.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Displays the Sales Management screen with centered text.
 *
 * This composable shows a full-screen layout with the label "매출관리" (Sales Management) centered on the screen.
 */
@Composable
fun ManagementScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "매출관리")
    }
}