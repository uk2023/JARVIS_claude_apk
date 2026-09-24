package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.PresetScenarios
import com.example.data.TurnRepository
import com.example.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

data class PlaybackConfig(
    val speedMultiplier: Float = 1.0f, // 1.0f, 2.0f, 5.0f, 0f (instant)
    val isStepByStepMode: Boolean = false,
    val isPaused: Boolean = false
)

data class UiState(
    val currentSessionId: String = "session-1",
    val sessionTitle: String = "Pronoun Resolution (\"Ye, wo, usse\")",
    val selectedModel: ClaudeModel = ClaudeModel.ALL_MODELS[0],
    val currentTurn: TurnState = TurnState(
        turnId = "turn-0",
        userPrompt = "",
        status = TurnStatus.IDLE
    ),
    val completedTurns: List<TurnState> = emptyList(),
    val isSummarySheetOpen: Boolean = false,
    val summaryFocusedGroupIndex: Int? = null,
    val isModelPickerOpen: Boolean = false,
    val isScenarioPickerOpen: Boolean = false,
    val previewDeliverable: Deliverable? = null,
    val playbackConfig: PlaybackConfig = PlaybackConfig(),
    val canStepForward: Boolean = false,
    val isAudioRecording: Boolean = false,
    val availableScenarios: List<PresetScenarios.ScenarioScript> = PresetScenarios.ALL_SCENARIOS
)

class ClaudeTurnViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TurnRepository(application)

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var executionJob: Job? = null
    private var pendingEvents: MutableList<ClaudeUIEvent> = mutableListOf()
    private var currentEventIndex: Int = 0

    init {
        // Load default scenario (Jarvis Pronoun Bug)
        loadScenario(PresetScenarios.JARVIS_PRONOUN_BUG, autoRun = true)
    }

    fun selectModel(model: ClaudeModel) {
        _uiState.update { it.copy(selectedModel = model, isModelPickerOpen = false) }
    }

    fun openSummarySheet(groupIndex: Int? = null) {
        _uiState.update {
            it.copy(
                isSummarySheetOpen = true,
                summaryFocusedGroupIndex = groupIndex
            )
        }
    }

    fun closeSummarySheet() {
        _uiState.update { it.copy(isSummarySheetOpen = false, summaryFocusedGroupIndex = null) }
    }

    fun openModelPicker() {
        _uiState.update { it.copy(isModelPickerOpen = true) }
    }

    fun closeModelPicker() {
        _uiState.update { it.copy(isModelPickerOpen = false) }
    }

    fun openScenarioPicker() {
        _uiState.update { it.copy(isScenarioPickerOpen = true) }
    }

    fun closeScenarioPicker() {
        _uiState.update { it.copy(isScenarioPickerOpen = false) }
    }

    fun setPreviewDeliverable(deliverable: Deliverable?) {
        _uiState.update { it.copy(previewDeliverable = deliverable) }
    }

    fun toggleAudioRecording() {
        _uiState.update { it.copy(isAudioRecording = !it.isAudioRecording) }
    }

    fun setPlaybackSpeed(speed: Float) {
        _uiState.update { it.copy(playbackConfig = it.playbackConfig.copy(speedMultiplier = speed)) }
    }

    fun toggleStepByStepMode() {
        val newMode = !_uiState.value.playbackConfig.isStepByStepMode
        _uiState.update {
            it.copy(
                playbackConfig = it.playbackConfig.copy(isStepByStepMode = newMode),
                canStepForward = newMode && currentEventIndex < pendingEvents.size
            )
        }
        if (!newMode && _uiState.value.currentTurn.status in listOf(TurnStatus.THINKING, TurnStatus.EXECUTING, TurnStatus.STREAMING_RESPONSE)) {
            resumeAutoExecution()
        }
    }

    fun togglePause() {
        val newPaused = !_uiState.value.playbackConfig.isPaused
        _uiState.update { it.copy(playbackConfig = it.playbackConfig.copy(isPaused = newPaused)) }
        if (!newPaused) {
            resumeAutoExecution()
        }
    }

    fun loadScenario(scenario: PresetScenarios.ScenarioScript, autoRun: Boolean = true) {
        executionJob?.cancel()
        val newTurnId = "turn-${System.currentTimeMillis()}"
        val initialTurn = TurnState(
            turnId = newTurnId,
            userPrompt = scenario.prompt,
            promptAttachments = scenario.attachments,
            status = TurnStatus.IDLE
        )

        pendingEvents = scenario.events.toMutableList()
        currentEventIndex = 0

        _uiState.update {
            it.copy(
                sessionTitle = scenario.title,
                currentTurn = initialTurn,
                isScenarioPickerOpen = false,
                canStepForward = it.playbackConfig.isStepByStepMode && pendingEvents.isNotEmpty()
            )
        }

        if (autoRun && !_uiState.value.playbackConfig.isStepByStepMode) {
            startExecutionFlow()
        }
    }

    fun submitUserPrompt(promptText: String, attachments: List<Attachment> = emptyList()) {
        if (promptText.isBlank()) return
        executionJob?.cancel()

        // If previous turn was completed, archive it
        val previousTurn = _uiState.value.currentTurn
        val completedList = if (previousTurn.status == TurnStatus.COMPLETED) {
            _uiState.value.completedTurns + previousTurn
        } else {
            _uiState.value.completedTurns
        }

        val turnId = "turn-${System.currentTimeMillis()}"
        val initialTurn = TurnState(
            turnId = turnId,
            userPrompt = promptText.trim(),
            promptAttachments = attachments,
            status = TurnStatus.THINKING,
            activeActivityLabel = "Thinking...",
            activeActivityIcon = "thinking"
        )

        // Generate synthetic turn events based on user prompt
        pendingEvents = generateEventsForPrompt(promptText, attachments).toMutableList()
        currentEventIndex = 0

        _uiState.update {
            it.copy(
                currentTurn = initialTurn,
                completedTurns = completedList,
                sessionTitle = promptText.take(36) + if (promptText.length > 36) "..." else "",
                canStepForward = it.playbackConfig.isStepByStepMode
            )
        }

        if (!_uiState.value.playbackConfig.isStepByStepMode) {
            startExecutionFlow()
        }
    }

    fun stepForward() {
        if (currentEventIndex < pendingEvents.size) {
            val event = pendingEvents[currentEventIndex]
            currentEventIndex++
            processSingleEvent(event)
            _uiState.update {
                it.copy(canStepForward = currentEventIndex < pendingEvents.size)
            }
        }
    }

    fun replayCurrentTurn() {
        val scenario = _uiState.value.availableScenarios.find { it.title == _uiState.value.sessionTitle }
        if (scenario != null) {
            loadScenario(scenario, autoRun = true)
        } else {
            val prompt = _uiState.value.currentTurn.userPrompt
            val atts = _uiState.value.currentTurn.promptAttachments
            submitUserPrompt(prompt, atts)
        }
    }

    private fun startExecutionFlow() {
        executionJob?.cancel()
        executionJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    currentTurn = it.currentTurn.copy(status = TurnStatus.THINKING)
                )
            }

            while (currentEventIndex < pendingEvents.size) {
                if (_uiState.value.playbackConfig.isPaused || _uiState.value.playbackConfig.isStepByStepMode) {
                    break
                }

                val event = pendingEvents[currentEventIndex]
                currentEventIndex++
                processSingleEvent(event)

                val speed = _uiState.value.playbackConfig.speedMultiplier
                if (speed > 0f) {
                    val delayMs = when (event) {
                        is ClaudeUIEvent.ActivityChanged -> (600 / speed).toLong()
                        is ClaudeUIEvent.StepStarted -> (500 / speed).toLong()
                        is ClaudeUIEvent.StepCompleted -> (300 / speed).toLong()
                        is ClaudeUIEvent.BridgeEmitted -> (700 / speed).toLong()
                        is ClaudeUIEvent.FinalResponseChunk -> (800 / speed).toLong()
                        is ClaudeUIEvent.DeliverableAdded -> (400 / speed).toLong()
                        else -> (400 / speed).toLong()
                    }
                    delay(delayMs)
                }
            }

            _uiState.update {
                it.copy(canStepForward = currentEventIndex < pendingEvents.size)
            }
        }
    }

    private fun resumeAutoExecution() {
        if (!_uiState.value.playbackConfig.isStepByStepMode && !_uiState.value.playbackConfig.isPaused) {
            startExecutionFlow()
        }
    }

    /**
     * Unified State Store transition processor as per Section 15 & 16.
     * Guarantees that Main Chat and Summary Modal receive synchronized updates!
     */
    private fun processSingleEvent(event: ClaudeUIEvent) {
        val current = _uiState.value.currentTurn

        when (event) {
            is ClaudeUIEvent.TurnStarted -> {
                _uiState.update {
                    it.copy(
                        currentTurn = current.copy(
                            turnId = event.turnId,
                            userPrompt = event.prompt,
                            promptAttachments = event.attachments,
                            status = TurnStatus.THINKING
                        )
                    )
                }
            }

            is ClaudeUIEvent.ActivityChanged -> {
                _uiState.update {
                    it.copy(
                        currentTurn = current.copy(
                            activeActivityLabel = event.label,
                            activeActivityIcon = event.iconType,
                            status = TurnStatus.EXECUTING
                        )
                    )
                }
            }

            is ClaudeUIEvent.StepStarted -> {
                val newStep = Step(
                    id = event.stepId,
                    label = event.label,
                    toolType = event.toolType,
                    status = StepStatus.RUNNING,
                    details = event.details
                )

                // Append to currently open group, or create first group if none
                val groups = current.stepGroups.toMutableList()
                if (groups.isEmpty() || groups.last().isClosed) {
                    val newGroup = StepGroup(
                        id = "group-${groups.size + 1}",
                        steps = listOf(newStep),
                        isClosed = false
                    )
                    groups.add(newGroup)
                } else {
                    val last = groups.last()
                    groups[groups.lastIndex] = last.copy(steps = last.steps + newStep)
                }

                _uiState.update {
                    it.copy(
                        currentTurn = current.copy(
                            stepGroups = groups,
                            status = TurnStatus.EXECUTING
                        )
                    )
                }
            }

            is ClaudeUIEvent.StepCompleted -> {
                val groups = current.stepGroups.map { group ->
                    val updatedSteps = group.steps.map { s ->
                        if (s.id == event.stepId) s.copy(status = StepStatus.COMPLETED) else s
                    }
                    group.copy(steps = updatedSteps)
                }
                _uiState.update {
                    it.copy(
                        currentTurn = current.copy(stepGroups = groups)
                    )
                }
            }

            is ClaudeUIEvent.BridgeEmitted -> {
                // Section 5 Rule: A step group closes when the model transitions from tool execution to generating narrative context prose.
                val groups = current.stepGroups.toMutableList()
                if (groups.isNotEmpty()) {
                    val last = groups.last()
                    groups[groups.lastIndex] = last.copy(
                        isClosed = true,
                        bridgeText = event.text
                    )
                }
                _uiState.update {
                    it.copy(
                        currentTurn = current.copy(
                            stepGroups = groups,
                            activeActivityLabel = null
                        )
                    )
                }
            }

            is ClaudeUIEvent.FinalResponseChunk -> {
                val groups = current.stepGroups.map { if (!it.isClosed) it.copy(isClosed = true) else it }
                _uiState.update {
                    it.copy(
                        currentTurn = current.copy(
                            stepGroups = groups,
                            finalResponseTitle = event.title ?: current.finalResponseTitle,
                            finalResponseText = current.finalResponseText + event.delta,
                            activeActivityLabel = null,
                            status = TurnStatus.STREAMING_RESPONSE
                        )
                    )
                }
            }

            is ClaudeUIEvent.DeliverableAdded -> {
                _uiState.update {
                    it.copy(
                        currentTurn = current.copy(
                            deliverables = current.deliverables + event.deliverable
                        )
                    )
                }
            }

            is ClaudeUIEvent.TurnCompleted -> {
                val groups = current.stepGroups.map { if (!it.isClosed) it.copy(isClosed = true) else it }
                val finishedTurn = current.copy(
                    stepGroups = groups,
                    turnSummary = event.summary,
                    activeActivityLabel = null,
                    status = TurnStatus.COMPLETED
                )
                _uiState.update {
                    it.copy(currentTurn = finishedTurn)
                }

                // Persist turn to Room
                viewModelScope.launch {
                    repository.saveSession(_uiState.value.currentSessionId, _uiState.value.sessionTitle, _uiState.value.selectedModel.id)
                    repository.saveTurn(_uiState.value.currentSessionId, finishedTurn)
                }
            }

            is ClaudeUIEvent.TurnFailed -> {
                _uiState.update {
                    it.copy(
                        currentTurn = current.copy(
                            status = TurnStatus.ERROR,
                            errorMessage = event.error,
                            activeActivityLabel = null
                        )
                    )
                }
            }
        }
    }

    private fun generateEventsForPrompt(prompt: String, attachments: List<Attachment>): List<ClaudeUIEvent> {
        val hasAttachments = attachments.isNotEmpty()
        val events = mutableListOf<ClaudeUIEvent>()

        events.add(ClaudeUIEvent.ActivityChanged("Planning analysis for: ${prompt.take(24)}...", "clock"))
        events.add(ClaudeUIEvent.StepStarted("step-u1", "Parsing intent and constraint boundaries", ToolType.THOUGHT))
        events.add(ClaudeUIEvent.StepCompleted("step-u1"))
        
        if (hasAttachments) {
            events.add(ClaudeUIEvent.ActivityChanged("Running command", "terminal"))
            events.add(ClaudeUIEvent.StepStarted("step-u2", "Inspect attachment manifest: ${attachments[0].name}", ToolType.COMMAND, "Unpacking headers for ${attachments[0].name}"))
            events.add(ClaudeUIEvent.StepCompleted("step-u2"))
        }

        events.add(ClaudeUIEvent.StepStarted("step-u3", "Recalled 2 memories", ToolType.MEMORY, "Memory: Project coding standards\nMemory: Architecture domain boundaries"))
        events.add(ClaudeUIEvent.StepCompleted("step-u3"))

        events.add(ClaudeUIEvent.ActivityChanged("Running command", "terminal"))
        events.add(ClaudeUIEvent.StepStarted("step-u4", "Search project references matching query", ToolType.COMMAND, "ripgrep --glob '!build' -i '${prompt.take(12)}'"))
        events.add(ClaudeUIEvent.StepCompleted("step-u4"))

        // Phase 1 Bridge
        events.add(ClaudeUIEvent.BridgeEmitted("Inspecting target implementation and validating proposed modifications:"))

        // Phase 2
        events.add(ClaudeUIEvent.ActivityChanged("Editing file", "pencil"))
        events.add(ClaudeUIEvent.StepStarted("step-u5", "Refactor core implementation for requested logic", ToolType.EDIT, "Applied non-breaking updates to solution handler"))
        events.add(ClaudeUIEvent.StepCompleted("step-u5"))
        events.add(ClaudeUIEvent.StepStarted("step-u6", "Run automated validation checks", ToolType.COMMAND, "gradle test --no-daemon"))
        events.add(ClaudeUIEvent.StepCompleted("step-u6"))

        val deliverableFile = "solution_${System.currentTimeMillis() % 10000}.kt"
        events.add(
            ClaudeUIEvent.DeliverableAdded(
                Deliverable(
                    id = "del-${System.currentTimeMillis()}",
                    filename = deliverableFile,
                    size = "18.4 KB",
                    extension = "kt",
                    description = "Completed solution files addressing: $prompt",
                    previewContent = """
                    // Generated solution for: $prompt
                    class SolutionHandler {
                        fun execute(): Result<String> {
                            // Verified logic matching specifications
                            return Result.success("Operation completed successfully")
                        }
                    }
                    """.trimIndent()
                )
            )
        )

        events.add(
            ClaudeUIEvent.FinalResponseChunk(
                title = "Analysis & Solution Breakdown",
                delta = """
                Here is the verified solution for your request: **"$prompt"**.

                ### Implementation Details
                1. **Isolated Root Constraints**: Confirmed operational requirements and ensured zero side-effects across existing state flows.
                2. **Optimized Execution**: Implemented reactive data streams that eliminate redundant recalculations.
                3. **Safety & Stability**: Added defensive boundary checks for edge case invalid inputs.
                """.trimIndent()
            )
        )

        events.add(
            ClaudeUIEvent.TurnCompleted(
                TurnSummary(
                    whatWasDone = listOf(
                        "Scanned dependencies and parsed constraints",
                        "Implemented refactored logic in $deliverableFile",
                        "Verified test suite execution"
                    ),
                    deliverablesProduced = listOf(
                        "$deliverableFile (18.4 KB)"
                    ),
                    nextSteps = listOf(
                        "Review $deliverableFile and test locally",
                        "Deploy changes into target pipeline"
                    )
                )
            )
        )

        return events
    }
}
