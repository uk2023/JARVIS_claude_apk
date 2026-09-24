package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.ClaudeRailLineLight
import com.example.ui.theme.ClaudeTerracotta

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryBottomSheet(
    turnState: TurnState,
    focusedGroupIndex: Int?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        dragHandle = null,
        modifier = modifier.testTag("summary_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
        ) {
            // Header: (X) button + Centered bold title "Summary"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .align(Alignment.CenterStart)
                        .testTag("summary_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Summary",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "Summary",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )

            // Timeline Tree
            val allSteps = turnState.stepGroups.flatMap { it.steps }
            val isActive = turnState.status in listOf(TurnStatus.THINKING, TurnStatus.EXECUTING)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // Render flat chronological timeline of steps from all groups
                itemsIndexed(allSteps) { index, step ->
                    val isLastItem = index == allSteps.lastIndex && !isActive
                    TimelineNodeItem(
                        step = step,
                        isLast = isLastItem,
                        stepNumber = index + 1
                    )
                }

                // If currently active, render the live active thinking / executing node!
                if (isActive) {
                    item {
                        ActiveLiveNodeItem(
                            label = turnState.activeActivityLabel ?: "Thinking",
                            isLast = true
                        )
                    }
                }

                // If completed, render completion badge
                if (turnState.status == TurnStatus.COMPLETED) {
                    item {
                        CompletedFinalNodeItem()
                    }
                }
            }
        }
    }
}

@Composable
fun TimelineNodeItem(
    step: Step,
    isLast: Boolean,
    stepNumber: Int
) {
    var isExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = step.details.isNotBlank()) {
                isExpanded = !isExpanded
            },
        verticalAlignment = Alignment.Top
    ) {
        // Timeline rail column with node indicator
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(36.dp)
        ) {
            // Node icon container
            Box(
                modifier = Modifier.size(28.dp),
                contentAlignment = Alignment.Center
            ) {
                when (step.toolType) {
                    ToolType.THOUGHT -> {
                        // Solid grey dot (o) as specified in Section 7
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF888888))
                        )
                    }
                    ToolType.MEMORY -> {
                        // Rounded rectangle icon containing book icon (📖)
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFE8EDF5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color(0xFF2563EB)
                            )
                        }
                    }
                    ToolType.COMMAND -> {
                        // Rounded rectangle icon containing terminal prompt (>_)
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFF1F1EF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = ">_",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color(0xFF333333)
                            )
                        }
                    }
                    ToolType.EDIT -> {
                        // Rounded rectangle icon containing pencil (✎)
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFFBF0EA)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = ClaudeTerracotta
                            )
                        }
                    }
                    ToolType.SEARCH -> {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF6B7280))
                        )
                    }
                }
            }

            // Vertical connecting line (rail)
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(1.5.dp)
                        .heightIn(min = 36.dp)
                        .background(ClaudeRailLineLight)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Label and details
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(top = 3.dp, bottom = 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = step.label,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = if (step.toolType == ToolType.THOUGHT) FontWeight.Normal else FontWeight.Medium,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.weight(1f)
                )

                if (step.details.isNotBlank()) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle details",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            // Expandable details container (stdout, memory dump, patch)
            AnimatedVisibility(visible = isExpanded && step.details.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = androidx.compose.foundation.BorderStroke(
                        0.5.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text(
                        text = step.details,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ActiveLiveNodeItem(
    label: String,
    isLast: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(36.dp)
        ) {
            Box(
                modifier = Modifier.size(28.dp),
                contentAlignment = Alignment.Center
            ) {
                // Colored orange/coral dot (•)
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(ClaudeTerracotta.copy(alpha = pulseAlpha))
                        .border(2.dp, ClaudeTerracotta, CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = ClaudeTerracotta
            ),
            modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
        )
    }
}

@Composable
fun CompletedFinalNodeItem() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(36.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF10B981))
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Completed execution turn",
            style = MaterialTheme.typography.labelMedium.copy(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF10B981)
            )
        )
    }
}
