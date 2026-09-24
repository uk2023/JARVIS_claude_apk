package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ClaudeTerracotta

@Composable
fun PlaybackControlBar(
    isStepByStep: Boolean,
    onToggleStepByStep: () -> Unit,
    canStepForward: Boolean,
    onStepForward: () -> Unit,
    isPaused: Boolean,
    onTogglePause: () -> Unit,
    currentSpeed: Float,
    onSelectSpeed: (Float) -> Unit,
    onReplay: () -> Unit,
    onOpenScenarios: () -> Unit,
    onOpenSummary: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = modifier
            .fillMaxWidth()
            .testTag("playback_control_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Scenario Switcher Chip
            AssistChip(
                onClick = onOpenScenarios,
                label = { Text("Scenarios", fontSize = 12.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.AltRoute,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = ClaudeTerracotta
                    )
                },
                shape = RoundedCornerShape(14.dp),
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )

            // Step-by-Step Toggle Chip
            FilterChip(
                selected = isStepByStep,
                onClick = onToggleStepByStep,
                label = { Text("Step Mode", fontSize = 12.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.FormatListNumbered,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                },
                shape = RoundedCornerShape(14.dp)
            )

            // If Step-by-Step is ON, show "Step ▶" button
            if (isStepByStep) {
                Button(
                    onClick = onStepForward,
                    enabled = canStepForward,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ClaudeTerracotta
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Next Step", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                // Pause / Resume Chip
                IconButton(
                    onClick = onTogglePause,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = if (isPaused) "Resume" else "Pause",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Speed multiplier selector (1x, 2x, 5x, Instant)
            val speeds = listOf(1.0f to "1x", 2.0f to "2x", 5.0f to "5x", 0.0f to "⚡")
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                speeds.forEach { (sp, label) ->
                    val isSelected = currentSpeed == sp
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) ClaudeTerracotta else Color.Transparent)
                            .clickable { onSelectSpeed(sp) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Replay Button
            IconButton(
                onClick = onReplay,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Replay turn",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Summary View Quick Button
            AssistChip(
                onClick = onOpenSummary,
                label = { Text("Summary View", fontSize = 12.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.ViewTimeline,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = ClaudeTerracotta
                    )
                },
                shape = RoundedCornerShape(14.dp),
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    }
}
