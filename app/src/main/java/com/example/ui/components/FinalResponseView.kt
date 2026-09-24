package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TurnSummary
import com.example.ui.theme.ClaudeCodeBgLight
import com.example.ui.theme.ClaudeCodeTextLight
import com.example.ui.theme.ClaudeTerracotta

@Composable
fun FinalResponseView(
    title: String,
    content: String,
    turnSummary: TurnSummary?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("final_response_view"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Large Serif Title
        if (title.isNotBlank()) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 21.sp,
                    lineHeight = 28.sp,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }

        // Render sections and code blocks formatted cleanly
        MarkdownProseRenderer(markdown = content)

        // Whole-Turn Summary Card (as defined in Section 11)
        if (turnSummary != null) {
            WholeTurnSummaryCard(summary = turnSummary)
        }
    }
}

@Composable
private fun MarkdownProseRenderer(markdown: String) {
    val paragraphs = markdown.split("\n\n")

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        paragraphs.forEach { para ->
            val trimmed = para.trim()
            when {
                trimmed.startsWith("### ") -> {
                    Text(
                        text = trimmed.removePrefix("### ").trim(),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            lineHeight = 23.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                }
                trimmed.startsWith("```") -> {
                    // Code block
                    val lines = trimmed.lines()
                    val lang = lines.firstOrNull()?.removePrefix("```")?.trim() ?: ""
                    val codeContent = if (lines.size > 2) {
                        lines.subList(1, lines.lastIndex).joinToString("\n")
                    } else trimmed
                    CodeBlockCard(language = lang, code = codeContent)
                }
                else -> {
                    // Regular serif body prose with inline token highlights
                    InlineFormattedSerifText(text = trimmed)
                }
            }
        }
    }
}

@Composable
fun InlineFormattedSerifText(
    text: String,
    modifier: Modifier = Modifier
) {
    // Parse backtick code spans like `isse`, `ne`, `resolve_pronoun`
    val parts = text.split("`")
    if (parts.size == 1) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = FontFamily.Serif,
                fontSize = 14.5.sp,
                lineHeight = 22.5.sp,
                color = MaterialTheme.colorScheme.onBackground
            ),
            modifier = modifier
        )
    } else {
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Render styled text flow
            Text(
                text = androidx.compose.ui.text.buildAnnotatedString {
                    for (i in parts.indices) {
                        if (i % 2 == 1) {
                            // Inline code token: dark blue on light blue
                            pushStyle(
                                androidx.compose.ui.text.SpanStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = ClaudeCodeTextLight,
                                    background = ClaudeCodeBgLight
                                )
                            )
                            append(" ${parts[i]} ")
                            pop()
                        } else {
                            pushStyle(
                                androidx.compose.ui.text.SpanStyle(
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 14.5.sp,
                                    color = androidx.compose.ui.graphics.Color.Unspecified
                                )
                            )
                            append(parts[i])
                            pop()
                        }
                    }
                },
                style = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = 22.5.sp
                )
            )
        }
    }
}

@Composable
fun CodeBlockCard(
    language: String,
    code: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp)),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
        )
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (language.isNotBlank()) language else "code",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy code",
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Code Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(12.dp)
            ) {
                Text(
                    text = code,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.5.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }
    }
}

@Composable
fun WholeTurnSummaryCard(
    summary: TurnSummary,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DoneAll,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = ClaudeTerracotta
                )
                Text(
                    text = "Turn Execution Summary",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            // 1. What was done
            if (summary.whatWasDone.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "1. What Was Done",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = ClaudeTerracotta
                        )
                    )
                    summary.whatWasDone.forEach { item ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Text("•", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = item,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 13.5.sp,
                                    lineHeight = 19.sp
                                )
                            )
                        }
                    }
                }
            }

            // 2. Deliverables produced
            if (summary.deliverablesProduced.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "2. Deliverables Produced",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = ClaudeTerracotta
                        )
                    )
                    summary.deliverablesProduced.forEach { item ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(start = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderZip,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = ClaudeTerracotta
                            )
                            Text(
                                text = item,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }
            }

            // 3. Conclusion / Next steps
            if (summary.nextSteps.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "3. Conclusion / Next Steps",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = ClaudeTerracotta
                        )
                    )
                    summary.nextSteps.forEach { item ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Text("→", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = item,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 13.5.sp,
                                    lineHeight = 19.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
