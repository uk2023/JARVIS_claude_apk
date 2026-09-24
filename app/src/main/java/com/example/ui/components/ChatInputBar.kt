package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Attachment
import com.example.model.ClaudeModel
import com.example.ui.theme.ClaudeTerracotta

@Composable
fun ChatInputBar(
    selectedModel: ClaudeModel,
    onModelClick: () -> Unit,
    onSendPrompt: (String, List<Attachment>) -> Unit,
    isAudioRecording: Boolean,
    onToggleAudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    var textInput by remember { mutableStateOf("") }
    var attachedFiles by remember { mutableStateOf<List<Attachment>>(emptyList()) }
    var showAttachmentMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("chat_input_bar"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Attachment thumbnails above input if any
        if (attachedFiles.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                attachedFiles.forEach { att ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.padding(bottom = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = att.name,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1
                            )
                            IconButton(
                                onClick = { attachedFiles = attachedFiles - att },
                                modifier = Modifier.size(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove",
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Main input container
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outline
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                // Text entry area
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Attachment (+) button
                    IconButton(
                        onClick = { showAttachmentMenu = true },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("attachment_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add attachment",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    ) {
                        if (textInput.isEmpty()) {
                            Text(
                                text = if (isAudioRecording) "Listening..." else "Reply to Claude...",
                                style = TextStyle(
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            )
                        }

                        BasicTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            textStyle = TextStyle(
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            cursorBrush = SolidColor(ClaudeTerracotta),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("message_input_field")
                        )
                    }

                    // Voice / Audio input button (Screenshot 83448_2)
                    IconButton(
                        onClick = onToggleAudio,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("audio_input_button")
                    ) {
                        Icon(
                            imageVector = if (isAudioRecording) Icons.Default.GraphicEq else Icons.Default.Mic,
                            contentDescription = "Voice input",
                            tint = if (isAudioRecording) ClaudeTerracotta else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Send Button
                    val canSend = textInput.isNotBlank() || attachedFiles.isNotEmpty()
                    IconButton(
                        onClick = {
                            if (canSend) {
                                onSendPrompt(textInput, attachedFiles)
                                textInput = ""
                                attachedFiles = emptyList()
                            }
                        },
                        enabled = canSend,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (canSend) ClaudeTerracotta else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                            .testTag("send_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = androidx.compose.ui.graphics.Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Bottom bar: Model selection pill (as seen in Screenshot 84591.jpg: "Sonnet 5 Medium")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(
                            0.5.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .clickable(onClick = onModelClick)
                            .testTag("model_selector_pill")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(ClaudeTerracotta)
                            )
                            Text(
                                text = selectedModel.badge,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }
        }

        // Quick attachment chooser dropdown / menu
        DropdownMenu(
            expanded = showAttachmentMenu,
            onDismissRequest = { showAttachmentMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Attach codebase (.tar.gz)") },
                onClick = {
                    attachedFiles = attachedFiles + Attachment("att-${System.currentTimeMillis()}", "service_dump.tar.gz", "3.4 MB", "tar.gz")
                    showAttachmentMenu = false
                }
            )
            DropdownMenuItem(
                text = { Text("Attach patch file (.kt)") },
                onClick = {
                    attachedFiles = attachedFiles + Attachment("att-${System.currentTimeMillis()}", "PatchValidator.kt", "18 KB", "kt")
                    showAttachmentMenu = false
                }
            )
            DropdownMenuItem(
                text = { Text("Attach error log (.log)") },
                onClick = {
                    attachedFiles = attachedFiles + Attachment("att-${System.currentTimeMillis()}", "stacktrace.log", "84 KB", "log")
                    showAttachmentMenu = false
                }
            )
        }
    }
}
