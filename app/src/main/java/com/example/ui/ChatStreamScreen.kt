package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TurnState
import com.example.model.TurnStatus
import com.example.ui.components.*
import com.example.ui.theme.ClaudeTerracotta
import com.example.viewmodel.ClaudeTurnViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatStreamScreen(
    viewModel: ClaudeTurnViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var showInfoDialog by remember { mutableStateOf(false) }

    // Auto-scroll when new items arrive or response streams
    LaunchedEffect(uiState.currentTurn.stepGroups.size, uiState.currentTurn.finalResponseText.length) {
        if (uiState.currentTurn.status in listOf(TurnStatus.THINKING, TurnStatus.EXECUTING, TurnStatus.STREAMING_RESPONSE)) {
            listState.animateScrollToItem(listState.layoutInfo.totalItemsCount.coerceAtLeast(1) - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Claude",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        )
                        Text(
                            text = uiState.sessionTitle,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.5.sp
                            ),
                            maxLines = 1
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.openSummarySheet() },
                        modifier = Modifier.testTag("top_summary_button")
                    ) {
                        Surface(
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                            color = ClaudeTerracotta.copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ClaudeTerracotta.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "Summary",
                                color = ClaudeTerracotta,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    IconButton(onClick = { showInfoDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Interaction Model Spec Info"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                // Interactive Playback Control Bar
                PlaybackControlBar(
                    isStepByStep = uiState.playbackConfig.isStepByStepMode,
                    onToggleStepByStep = { viewModel.toggleStepByStepMode() },
                    canStepForward = uiState.canStepForward,
                    onStepForward = { viewModel.stepForward() },
                    isPaused = uiState.playbackConfig.isPaused,
                    onTogglePause = { viewModel.togglePause() },
                    currentSpeed = uiState.playbackConfig.speedMultiplier,
                    onSelectSpeed = { viewModel.setPlaybackSpeed(it) },
                    onReplay = { viewModel.replayCurrentTurn() },
                    onOpenScenarios = { viewModel.openScenarioPicker() },
                    onOpenSummary = { viewModel.openSummarySheet() }
                )

                // Input bar with Model selection pill & audio button
                ChatInputBar(
                    selectedModel = uiState.selectedModel,
                    onModelClick = { viewModel.openModelPicker() },
                    onSendPrompt = { text, atts ->
                        viewModel.submitUserPrompt(text, atts)
                        coroutineScope.launch {
                            listState.animateScrollToItem(listState.layoutInfo.totalItemsCount.coerceAtLeast(1) - 1)
                        }
                    },
                    isAudioRecording = uiState.isAudioRecording,
                    onToggleAudio = { viewModel.toggleAudioRecording() }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = innerPadding.calculateBottomPadding() + 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Render previously completed turns in session if any
            items(uiState.completedTurns) { turn ->
                TurnStreamItem(
                    turn = turn,
                    onStepGroupClick = { groupIdx -> viewModel.openSummarySheet(groupIdx) },
                    onActivityClick = { viewModel.openSummarySheet() },
                    onDeliverableClick = { del -> viewModel.setPreviewDeliverable(del) }
                )
            }

            // Render active current turn
            item {
                TurnStreamItem(
                    turn = uiState.currentTurn,
                    onStepGroupClick = { groupIdx -> viewModel.openSummarySheet(groupIdx) },
                    onActivityClick = { viewModel.openSummarySheet() },
                    onDeliverableClick = { del -> viewModel.setPreviewDeliverable(del) }
                )
            }
        }
    }

    // Modal BottomSheet for Summary (Dual-View synchronized in real-time!)
    if (uiState.isSummarySheetOpen) {
        SummaryBottomSheet(
            turnState = uiState.currentTurn,
            focusedGroupIndex = uiState.summaryFocusedGroupIndex,
            onDismiss = { viewModel.closeSummarySheet() }
        )
    }

    // Model Picker BottomSheet
    if (uiState.isModelPickerOpen) {
        ModelSelectorSheet(
            selectedModel = uiState.selectedModel,
            onModelSelected = { viewModel.selectModel(it) },
            onDismiss = { viewModel.closeModelPicker() }
        )
    }

    // Scenario Picker BottomSheet
    if (uiState.isScenarioPickerOpen) {
        ScenarioSelectorSheet(
            scenarios = uiState.availableScenarios,
            onSelectScenario = { viewModel.loadScenario(it, autoRun = true) },
            onDismiss = { viewModel.closeScenarioPicker() }
        )
    }

    // Deliverable Inspector Modal
    uiState.previewDeliverable?.let { deliv ->
        DeliverablePreviewModal(
            deliverable = deliv,
            onDismiss = { viewModel.setPreviewDeliverable(null) }
        )
    }

    // Architecture & Reverse-Engineering Info Dialog
    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = {
                Text(
                    text = "Claude Interaction Model Spec",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Implemented 1:1 according to the 10 reverse-engineered execution screenshots:",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                    )
                    Text("• Inline Stream: borderless canvas, no nested card wrappers")
                    Text("• Phase-based step groups (e.g. [7 steps >], [2 steps >])")
                    Text("• Context bridges streamed between groups in Serif prose")
                    Text("• Dynamic active pills (>_ command, ✎ edit, 🕘 plan, coral spinner)")
                    Text("• Dual-View synced state with Summary BottomSheet")
                    Text("• Monospace code tokens & deliverable archives")
                }
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text("Got it")
                }
            }
        )
    }
}

/**
 * Renders an entire Claude turn stream seamlessly without outer card wrapper!
 */
@Composable
fun TurnStreamItem(
    turn: TurnState,
    onStepGroupClick: (Int) -> Unit,
    onActivityClick: () -> Unit,
    onDeliverableClick: (com.example.model.Deliverable) -> Unit,
    modifier: Modifier = Modifier
) {
    if (turn.userPrompt.isBlank() && turn.stepGroups.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. User Message (in light grey card #F3F3F3)
        if (turn.userPrompt.isNotBlank()) {
            UserMessageCard(
                promptText = turn.userPrompt,
                attachments = turn.promptAttachments
            )
        }

        // 2. Execution Phases (Step groups + Context bridges)
        turn.stepGroups.forEachIndexed { groupIndex, group ->
            // If group has completed steps, render collapsed pill "[N steps >]"
            if (group.steps.isNotEmpty()) {
                val completedSteps = group.steps.filter { it.status == com.example.model.StepStatus.COMPLETED }.size
                val stepCount = if (completedSteps > 0) completedSteps else group.steps.size
                
                StepGroupPill(
                    stepCount = stepCount,
                    onClick = { onStepGroupClick(groupIndex) }
                )
            }

            // Between-group context / bridge prose emitted inline in Serif
            if (!group.bridgeText.isNullOrBlank()) {
                ContextBridgeProse(text = group.bridgeText)
            }
        }

        // 3. Dynamic Active Activity Pill (if currently executing or planning)
        if (turn.status in listOf(TurnStatus.THINKING, TurnStatus.EXECUTING) && !turn.activeActivityLabel.isNullOrBlank()) {
            ActivityPill(
                label = turn.activeActivityLabel,
                iconType = turn.activeActivityIcon,
                onClick = onActivityClick
            )
        }

        // 4. Inline Deliverables Produced (.tar.gz, .kt)
        if (turn.deliverables.isNotEmpty()) {
            turn.deliverables.forEach { deliv ->
                DeliverableCard(
                    deliverable = deliv,
                    onClick = { onDeliverableClick(deliv) },
                    onDownloadClick = { onDeliverableClick(deliv) }
                )
            }
        }

        // 5. Final Output Stream (Large Serif Title, Headings, Monospace Code, Whole-Turn Summary)
        if (turn.finalResponseText.isNotBlank()) {
            FinalResponseView(
                title = turn.finalResponseTitle,
                content = turn.finalResponseText,
                turnSummary = turn.turnSummary
            )
        }
    }
}
