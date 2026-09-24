package com.example.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ContextBridgeProse(
    text: String,
    modifier: Modifier = Modifier
) {
    // Determine if it's a section bridge (e.g. "Ab Budget section:") or explanatory prose
    val isHeaderBridge = text.startsWith("Ab ") || text.endsWith(":") && text.length < 40

    if (isHeaderBridge) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                lineHeight = 22.sp,
                color = MaterialTheme.colorScheme.onBackground
            ),
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .testTag("context_bridge_header")
        )
    } else {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Normal,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                color = MaterialTheme.colorScheme.onBackground
            ),
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .testTag("context_bridge_prose")
        )
    }
}
