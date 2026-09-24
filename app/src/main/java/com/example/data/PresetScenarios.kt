package com.example.data

import com.example.model.*

object PresetScenarios {

    data class ScenarioScript(
        val id: String,
        val title: String,
        val prompt: String,
        val attachments: List<Attachment>,
        val events: List<ClaudeUIEvent>
    )

    val JARVIS_PRONOUN_BUG = ScenarioScript(
        id = "jarvis_pronoun",
        title = "Pronoun Resolution (\"Ye, wo, usse\") Bug Fix",
        prompt = "Jarvis NLP pipeline me pronouns like 'ye, wo, usse, isse' resolution break ho rahi hai context drift ki wajah se. Uploaded tarball me check karo aur test case ke saath root cause fix karo.",
        attachments = listOf(
            Attachment("att-1", "jarvis_nlp_core_dump.tar.gz", "4.2 MB", "tar.gz")
        ),
        events = listOf(
            ClaudeUIEvent.ActivityChanged("Planning consistent context analysis...", "clock"),
            ClaudeUIEvent.StepStarted("step-1", "JARVIS नाम के सेल्फ-इवॉल्विंग AI प्रोजेक्ट का ओवरव्यू", ToolType.THOUGHT, "Inspecting repository metadata and NLP module manifest"),
            ClaudeUIEvent.StepCompleted("step-1"),
            ClaudeUIEvent.StepStarted("step-2", "अपलोड की गई tar.gz फाइल और यूज़र की क्वेरी का मिलान", ToolType.THOUGHT, "Verifying checksums and archive validity"),
            ClaudeUIEvent.StepCompleted("step-2"),
            ClaudeUIEvent.StepStarted("step-3", "Recalled 3 memories", ToolType.MEMORY, "Memory 1: Hindi stemmer token offsets\nMemory 2: Coreference pronoun chain cache\nMemory 3: Fallback noun phrase resolver"),
            ClaudeUIEvent.StepCompleted("step-3"),
            ClaudeUIEvent.ActivityChanged("Running command", "terminal"),
            ClaudeUIEvent.StepStarted("step-4", "List contents of uploaded tarball", ToolType.COMMAND, "$ tar -ztvf jarvis_nlp_core_dump.tar.gz\nsrc/coreference/resolver.py\nsrc/tokenizers/hindi_parser.py\ntests/test_pronouns.py"),
            ClaudeUIEvent.StepCompleted("step-4"),
            ClaudeUIEvent.StepStarted("step-5", "Scoping a large, complex codebase with multi-tier pipelines", ToolType.THOUGHT, "Located 14 coreference rule files across 3 submodules"),
            ClaudeUIEvent.StepCompleted("step-5"),
            ClaudeUIEvent.StepStarted("step-6", "Extract tarball and check top-level structure", ToolType.COMMAND, "$ tar -xzf jarvis_nlp_core_dump.tar.gz -C /workspace/jarvis/\nArchive extracted in 480ms"),
            ClaudeUIEvent.StepCompleted("step-6"),
            ClaudeUIEvent.StepStarted("step-7", "Run initial pytest suite on coreference", ToolType.COMMAND, "$ pytest tests/test_pronouns.py\nFAILED tests/test_pronouns.py::test_hindi_oblique_pronouns - KeyError: 'isse'"),
            ClaudeUIEvent.StepCompleted("step-7"),
            
            // First context bridge emitted inline in stream:
            ClaudeUIEvent.BridgeEmitted("Let's verify with a real test reproducing the exact scenario:"),
            
            // Phase 2
            ClaudeUIEvent.ActivityChanged("Running command", "terminal"),
            ClaudeUIEvent.StepStarted("step-8", "Reproduce oblique pronoun binding with isolate sandbox", ToolType.COMMAND, "$ python -m jarvis.debug --sentence \"Ramesh ne Mohan ko bulaya aur usse bataya\""),
            ClaudeUIEvent.StepCompleted("step-8"),
            ClaudeUIEvent.StepStarted("step-9", "Inspect AST of pronoun_binder.py", ToolType.COMMAND, "Analyzing scope stack for token 'usse' (antecedent candidate: Mohan vs Ramesh)"),
            ClaudeUIEvent.StepCompleted("step-9"),
            
            // Second context bridge emitted inline in stream:
            ClaudeUIEvent.BridgeEmitted("Found a deeper root cause — entity extraction only catches direct subject antecedents, bypassing indirect Hindi oblique cases (isse/usse) when postpositions ('ne', 'ko', 'se') follow the noun phrase."),
            
            // Phase 3
            ClaudeUIEvent.ActivityChanged("Editing file", "pencil"),
            ClaudeUIEvent.StepStarted("step-10", "Patch pronoun_binder.py with postposition-aware antecedent matcher", ToolType.EDIT, "Updated PronounResolver.bind_oblique_reference() to include accusative & dative case markers"),
            ClaudeUIEvent.StepCompleted("step-10"),
            ClaudeUIEvent.StepStarted("step-11", "Add comprehensive Hindi coreference test cases in test_pronouns.py", ToolType.EDIT, "Added 18 test cases covering 'ye', 'wo', 'usse', 'isse', 'unhe'"),
            ClaudeUIEvent.StepCompleted("step-11"),
            ClaudeUIEvent.ActivityChanged("Running command", "terminal"),
            ClaudeUIEvent.StepStarted("step-12", "Execute full regression test suite", ToolType.COMMAND, "$ pytest tests/test_pronouns.py\n================ 19 passed in 1.42s ================"),
            ClaudeUIEvent.StepCompleted("step-12"),
            ClaudeUIEvent.StepStarted("step-13", "Package updated fix into deliverable archive", ToolType.COMMAND, "$ tar -czf jarvis_v25_pronoun_fix.tar.gz dist/jarvis/"),
            ClaudeUIEvent.StepCompleted("step-13"),
            
            ClaudeUIEvent.DeliverableAdded(
                Deliverable(
                    id = "del-1",
                    filename = "Jarvis Cloud Work v25 pronoun resolution.tar.gz",
                    size = "3.8 MB",
                    extension = "tar.gz",
                    description = "Contains patched PronounResolver, postposition parser, and verified test suite",
                    previewContent = """
                    // Patch summary: PronounResolver.kt / resolver.py
                    + fun resolveObliquePronoun(token: String, history: List<EntityToken>): Entity? {
                    +     val candidate = history.lastOrNull { it.hasPostposition("ko", "se", "ne") }
                    +     return candidate ?: history.lastOrNull()
                    + }
                    """.trimIndent()
                )
            ),
            
            ClaudeUIEvent.FinalResponseChunk(
                title = "\"Ye, wo, usse, isse\" samajhna — 3 chhupe hue bugs fix ho gaye",
                delta = """
                NLP engine me teen interconnected flaws the jo multi-lingual context binding ko silently corrupt kar rahe the. Humne un teeno ko identify karke permanent resolution apply kar diya hai.

                ### Bug A — Pronoun galat antecedent ko point karta tha
                Code mein pehle se hi ek comment tha ki oblique case markers (`ne`, `ko`, `se`) extract hone par coreference table prune ho jata tha. Iski wajah se jab sentence `"Ramesh ne Mohan ko dekha aur usse bulaya"` aata tha, system `usse` ko Mohan ke bajay Ramesh par map karta tha.
                ```python
                # Fixed antecedent resolution in resolver.py:
                def resolve_pronoun(token, context_stack):
                    if token.is_oblique():
                        return context_stack.get_recent_object() or context_stack.get_subject()
                ```

                ### Bug B — Gender & Number Agreement Drift
                `wo` aur `ye` singular tatha plural dono ke roop me prayog ho sakte hain. Tokenizer singular assume karke plural predicates ko drop kar deta tha. Humne dynamic agreement validator add kiya hai jo verb inflection se number deduce karta hai.

                ### Bug C — Cache Invalidation on Turn Boundaries
                Session history update hone par pronoun references clear nahi ho rahe the, jiski wajah se purane context ke pronouns nayi query me leak ho rahe the. Naye cache invalidator ne turn-boundary isolation guarantee kar di hai.
                """.trimIndent()
            ),
            
            ClaudeUIEvent.TurnCompleted(
                TurnSummary(
                    whatWasDone = listOf(
                        "Root-cause analysis on Hindi coreference token binder",
                        "Patched postposition parser for 'ne', 'ko', 'se' markers",
                        "Added 18 regression test cases in test_pronouns.py",
                        "Verified 19/19 passing test suite"
                    ),
                    deliverablesProduced = listOf(
                        "Jarvis Cloud Work v25 pronoun resolution.tar.gz (3.8 MB)"
                    ),
                    nextSteps = listOf(
                        "Extract the deliverable archive into your microservice directory",
                        "Run `python -m pytest tests/` to confirm environment parity",
                        "Deploy patched container to staging cluster"
                    )
                )
            )
        )
    )

    val ROUTING_AUDIT = ScenarioScript(
        id = "routing_audit",
        title = "Routing Section Audit & Budget Limiter",
        prompt = "Auditing the routing section for hardcoded endpoints and build an adaptive rate limiter for budget control.",
        attachments = emptyList(),
        events = listOf(
            ClaudeUIEvent.ActivityChanged("Auditing the routing section for hardcoded paths...", "clock"),
            ClaudeUIEvent.StepStarted("step-r1", "Scan API gateway route definitions", ToolType.COMMAND, "grep -rn 'https://api.internal' ./src/gateway/"),
            ClaudeUIEvent.StepCompleted("step-r1"),
            ClaudeUIEvent.StepStarted("step-r2", "Recalled 2 memories", ToolType.MEMORY, "Memory: Gateway token bucket spec\nMemory: Fallback redis circuit breaker"),
            ClaudeUIEvent.StepCompleted("step-r2"),
            ClaudeUIEvent.StepStarted("step-r3", "Isolate hardcoded endpoints in routing tables", ToolType.EDIT, "Flagged 4 legacy direct URLs in routes.v2.json"),
            ClaudeUIEvent.StepCompleted("step-r3"),
            
            ClaudeUIEvent.BridgeEmitted("Ab Budget section:"),
            
            ClaudeUIEvent.ActivityChanged("Running command", "terminal"),
            ClaudeUIEvent.StepStarted("step-r4", "Calculate token consumption metrics across endpoints", ToolType.COMMAND, "python scripts/token_estimator.py --window 30d"),
            ClaudeUIEvent.StepCompleted("step-r4"),
            ClaudeUIEvent.StepStarted("step-r5", "Design sliding window limiter with Redis backplane", ToolType.THOUGHT, "Implemented exponential backoff penalty for tier 3 consumers"),
            ClaudeUIEvent.StepCompleted("step-r5"),
            ClaudeUIEvent.StepStarted("step-r6", "Validate rate limits against peak traffic simulations", ToolType.COMMAND, "vegeta attack -rate=500/1s -duration=10s | vegeta report"),
            ClaudeUIEvent.StepCompleted("step-r6"),
            
            ClaudeUIEvent.BridgeEmitted("Ab Execution section:"),
            
            ClaudeUIEvent.ActivityChanged("Editing file", "pencil"),
            ClaudeUIEvent.StepStarted("step-r7", "Inject BudgetLimiterMiddleware into request pipeline", ToolType.EDIT, "Added token verification before route dispatcher"),
            ClaudeUIEvent.StepCompleted("step-r7"),
            ClaudeUIEvent.StepStarted("step-r8", "Create circuit breaker for budget overflow", ToolType.EDIT, "Configured HTTP 429 Too Many Requests response with retry-after header"),
            ClaudeUIEvent.StepCompleted("step-r8"),
            
            ClaudeUIEvent.BridgeEmitted("Verified gateway routing pipeline with end-to-end integration tests."),
            
            ClaudeUIEvent.DeliverableAdded(
                Deliverable(
                    id = "del-r1",
                    filename = "route_limiter_middleware.kt",
                    size = "42 KB",
                    extension = "kt",
                    description = "Zero-dependency sliding window budget limiter with Redis failover",
                    previewContent = """
                    class BudgetLimiterMiddleware(private val quota: TokenQuota) {
                        suspend fun intercept(req: Request, next: Handler): Response {
                            if (!quota.consume(req.userId, req.cost)) {
                                return Response.Status(429).header("Retry-After", "60")
                            }
                            return next(req)
                        }
                    }
                    """.trimIndent()
                )
            ),
            
            ClaudeUIEvent.FinalResponseChunk(
                title = "Gateway Routing Audit & Budget Limiter Implementation",
                delta = """
                All 4 hardcoded gateway routes have been decoupled into configurable environment variables, and the adaptive token-bucket budget limiter has been placed in front of dispatchers.

                ### Key Improvements:
                1. **Zero Hardcoded URLs**: All upstream target URLs are now resolved dynamically via service discovery catalog.
                2. **Tiered Budget Enforcement**: Accounts with high token burn receive automatic traffic throttling before incurring overage fees.
                3. **Sub-millisecond Latency**: Local atomic memory registers handle 99.8% of requests without blocking I/O calls.
                """.trimIndent()
            ),
            
            ClaudeUIEvent.TurnCompleted(
                TurnSummary(
                    whatWasDone = listOf(
                        "Extracted hardcoded endpoints into dynamic route table",
                        "Implemented BudgetLimiterMiddleware with token buckets",
                        "Load tested at 500 RPS with zero latency degradation"
                    ),
                    deliverablesProduced = listOf(
                        "route_limiter_middleware.kt (42 KB)"
                    ),
                    nextSteps = listOf(
                        "Merge middleware branch into gateway main",
                        "Set REDIS_BUDGET_URL in your cloud secret manager"
                    )
                )
            )
        )
    )

    val CONTEXT_INTELLIGENCE = ScenarioScript(
        id = "context_intel",
        title = "Context Intelligence Layer & Tarball Scoping",
        prompt = "Building a context intelligence layer that indexes multi-repository tarballs and extracts cross-file dependency callgraphs.",
        attachments = listOf(
            Attachment("att-c1", "enterprise_repo_snapshot.tar.gz", "12.4 MB", "tar.gz")
        ),
        events = listOf(
            ClaudeUIEvent.ActivityChanged("Building a context intelligence lay...", "clock"),
            ClaudeUIEvent.StepStarted("step-c1", "Recalled 3 memories", ToolType.MEMORY, "Index format specs, token limits, parser rules"),
            ClaudeUIEvent.StepCompleted("step-c1"),
            ClaudeUIEvent.ActivityChanged("Running command", "terminal"),
            ClaudeUIEvent.StepStarted("step-c2", "List contents of uploaded tarball", ToolType.COMMAND, "tar -ztvf enterprise_repo_snapshot.tar.gz | head -n 40"),
            ClaudeUIEvent.StepCompleted("step-c2"),
            ClaudeUIEvent.StepStarted("step-c3", "Scoping a large, complex codebase...", ToolType.THOUGHT, "Found 182 source files across 8 modules"),
            ClaudeUIEvent.StepCompleted("step-c3"),
            ClaudeUIEvent.StepStarted("step-c4", "Extract tarball and check top-level structure", ToolType.COMMAND, "tar -xzf enterprise_repo_snapshot.tar.gz -C /tmp/workspace/"),
            ClaudeUIEvent.StepCompleted("step-c4"),
            
            ClaudeUIEvent.BridgeEmitted("Synthesizing symbol reference graph across all packages:"),
            
            ClaudeUIEvent.ActivityChanged("Running command", "terminal"),
            ClaudeUIEvent.StepStarted("step-c5", "Parse Kotlin and Python AST nodes into vector embeddings", ToolType.COMMAND, "python -m context_engine.indexer --dir /tmp/workspace/"),
            ClaudeUIEvent.StepCompleted("step-c5"),
            ClaudeUIEvent.StepStarted("step-c6", "Build bidirectional callgraph index", ToolType.COMMAND, "context_graph_build --output index.bin"),
            ClaudeUIEvent.StepCompleted("step-c6"),
            
            ClaudeUIEvent.DeliverableAdded(
                Deliverable(
                    id = "del-c1",
                    filename = "context_engine_v1.tar.gz",
                    size = "5.1 MB",
                    extension = "tar.gz",
                    description = "Complete compiled symbol index, semantic embeddings, and lookup engine",
                    previewContent = """
                    // Context Engine Manifest
                    {
                      "version": "1.0",
                      "indexed_symbols": 4920,
                      "graph_edges": 18230,
                      "query_p99_latency_ms": 2.4
                    }
                    """.trimIndent()
                )
            ),
            
            ClaudeUIEvent.FinalResponseChunk(
                title = "Context Intelligence Layer Initialized",
                delta = """
                The context intelligence layer has scanned the entire tarball and constructed a cross-module AST graph.
                
                ### Architecture Highlights:
                - **Zero Cold Start**: Pre-computed graph serialized into memory-mapped index.
                - **Cross-Language Resolution**: Identifies when Kotlin endpoints invoke Python backend services through gRPC interfaces.
                """.trimIndent()
            ),
            
            ClaudeUIEvent.TurnCompleted(
                TurnSummary(
                    whatWasDone = listOf(
                        "Extracted and scoped 182 source files",
                        "Generated semantic callgraph with 18,230 relations",
                        "Packaged context engine binary into tarball"
                    ),
                    deliverablesProduced = listOf(
                        "context_engine_v1.tar.gz (5.1 MB)"
                    ),
                    nextSteps = listOf(
                        "Integrate context_engine daemon into developer workspace"
                    )
                )
            )
        )
    )

    val ALL_SCENARIOS = listOf(JARVIS_PRONOUN_BUG, ROUTING_AUDIT, CONTEXT_INTELLIGENCE)
}
