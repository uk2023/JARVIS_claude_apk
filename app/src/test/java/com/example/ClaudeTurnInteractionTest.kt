package com.example

import com.example.data.PresetScenarios
import com.example.model.*
import org.junit.Assert.*
import org.junit.Test

class ClaudeTurnInteractionTest {

    @Test
    fun testPresetScenariosContainObservableScreenshotData() {
        val jarvis = PresetScenarios.JARVIS_PRONOUN_BUG
        assertEquals("jarvis_pronoun", jarvis.id)
        assertTrue(jarvis.prompt.contains("isse"))
        assertTrue(jarvis.attachments.isNotEmpty())
        assertEquals("jarvis_nlp_core_dump.tar.gz", jarvis.attachments[0].name)

        // Verify events include memory recall, terminal commands, file edits, and bridges
        val memoryEvents = jarvis.events.filterIsInstance<ClaudeUIEvent.StepStarted>()
            .filter { it.toolType == ToolType.MEMORY }
        assertTrue("Must include memory recall step", memoryEvents.isNotEmpty())
        assertEquals("Recalled 3 memories", memoryEvents[0].label)

        val bridgeEvents = jarvis.events.filterIsInstance<ClaudeUIEvent.BridgeEmitted>()
        assertTrue("Must include phase context bridges", bridgeEvents.size >= 2)
        assertTrue(bridgeEvents[0].text.contains("Let's verify with a real test"))
    }

    @Test
    fun testStepGroupingLogicSeparatesPhasesByBridgeText() {
        val script = PresetScenarios.ROUTING_AUDIT
        val bridgeEvents = script.events.filterIsInstance<ClaudeUIEvent.BridgeEmitted>()
        assertEquals("Ab Budget section:", bridgeEvents[0].text)
        assertEquals("Ab Execution section:", bridgeEvents[1].text)
    }

    @Test
    fun testModelListHasDefaultSonnet5Medium() {
        val defaultModel = ClaudeModel.ALL_MODELS.find { it.isDefault }
        assertNotNull(defaultModel)
        assertEquals("Sonnet 5 Medium", defaultModel?.badge)
    }
}
