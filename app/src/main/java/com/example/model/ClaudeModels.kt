package com.example.model

enum class ToolType {
    COMMAND,
    EDIT,
    SEARCH,
    MEMORY,
    THOUGHT
}

enum class StepStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED
}

enum class TurnStatus {
    IDLE,
    THINKING,
    EXECUTING,
    STREAMING_RESPONSE,
    COMPLETED,
    ERROR
}

data class Step(
    val id: String,
    val label: String,
    val toolType: ToolType,
    val status: StepStatus,
    val timestamp: Long = System.currentTimeMillis(),
    val details: String = ""
)

data class StepGroup(
    val id: String,
    val steps: List<Step> = emptyList(),
    val bridgeText: String? = null,
    val isClosed: Boolean = false
)

data class Deliverable(
    val id: String,
    val filename: String,
    val size: String,
    val extension: String = "tar.gz",
    val description: String = "",
    val previewContent: String = ""
)

data class TurnSummary(
    val whatWasDone: List<String> = emptyList(),
    val deliverablesProduced: List<String> = emptyList(),
    val nextSteps: List<String> = emptyList()
)

data class Attachment(
    val id: String,
    val name: String,
    val size: String,
    val extension: String
)

data class ClaudeModel(
    val id: String,
    val name: String,
    val badge: String,
    val description: String,
    val isDefault: Boolean = false
) {
    companion object {
        val ALL_MODELS = listOf(
            ClaudeModel(
                id = "sonnet-5-medium",
                name = "Sonnet 5 Medium",
                badge = "Sonnet 5 Medium",
                description = "Balanced high-performance model for complex reasoning and coding",
                isDefault = true
            ),
            ClaudeModel(
                id = "claude-3-7-sonnet",
                name = "Claude 3.7 Sonnet",
                badge = "Sonnet 3.7",
                description = "Hybrid reasoning model with extended thinking capabilities"
            ),
            ClaudeModel(
                id = "claude-3-5-sonnet",
                name = "Claude 3.5 Sonnet",
                badge = "Sonnet 3.5",
                description = "Industry standard for code synthesis and architectural refactoring"
            ),
            ClaudeModel(
                id = "claude-3-5-haiku",
                name = "Claude 3.5 Haiku",
                badge = "Haiku 3.5",
                description = "Ultra-fast response model for quick lookups and streaming"
            ),
            ClaudeModel(
                id = "claude-3-5-opus",
                name = "Claude 3.5 Opus",
                badge = "Opus 3.5",
                description = "Deep reasoning model for exhaustive multi-file codebases"
            )
        )
    }
}

data class TurnState(
    val turnId: String,
    val userPrompt: String,
    val promptAttachments: List<Attachment> = emptyList(),
    val activeActivityLabel: String? = null,
    val activeActivityIcon: String = "clock", // "clock", "terminal", "pencil", "thinking"
    val stepGroups: List<StepGroup> = emptyList(),
    val finalResponseTitle: String = "",
    val finalResponseText: String = "",
    val deliverables: List<Deliverable> = emptyList(),
    val turnSummary: TurnSummary? = null,
    val status: TurnStatus = TurnStatus.IDLE,
    val errorMessage: String? = null,
    val selectedGroupIndexForSummary: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

sealed interface ClaudeUIEvent {
    data class TurnStarted(val turnId: String, val prompt: String, val attachments: List<Attachment> = emptyList()) : ClaudeUIEvent
    data class ActivityChanged(val label: String, val iconType: String) : ClaudeUIEvent
    data class StepStarted(val stepId: String, val label: String, val toolType: ToolType, val details: String = "") : ClaudeUIEvent
    data class StepCompleted(val stepId: String, val resultSummary: String? = null) : ClaudeUIEvent
    data class BridgeEmitted(val text: String) : ClaudeUIEvent
    data class FinalResponseChunk(val title: String? = null, val delta: String) : ClaudeUIEvent
    data class DeliverableAdded(val deliverable: Deliverable) : ClaudeUIEvent
    data class TurnCompleted(val summary: TurnSummary) : ClaudeUIEvent
    data class TurnFailed(val error: String) : ClaudeUIEvent
}
