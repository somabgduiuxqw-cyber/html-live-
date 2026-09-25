package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AiChangeProposal
import com.example.model.AiChatMessage
import com.example.model.AiSettings
import com.example.model.ProjectFile
import com.example.model.TeachingStep

enum class AiContextScope(val label: String) {
    FULL_PROJECT("Full Project"),
    CURRENT_FILE("Current File"),
    ERROR_ONLY("Error Only"),
    PROMPT_ONLY("No Code")
}

@Composable
fun AiAssistantScreen(
    settings: AiSettings,
    chatMessages: List<AiChatMessage>,
    activeFiles: List<ProjectFile>,
    currentFileName: String,
    isLoading: Boolean,
    onSendMessage: (prompt: String, contextFiles: Map<String, String>, scope: AiContextScope) -> Unit,
    onApplyProposal: (AiChangeProposal) -> Unit,
    onOpenSettings: () -> Unit,
    onClearChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inputPrompt by remember { mutableStateOf("") }
    var selectedScope by remember { mutableStateOf(AiContextScope.FULL_PROJECT) }

    val promptLibrary = listOf(
        "Make the buttons rounded and animated",
        "Add a dark/light mode toggle",
        "Create a mobile touch platformer game",
        "Make this layout responsive for phones and tablets",
        "Add touch controls to this canvas game",
        "Add sound effects using Web Audio API",
        "Why is my button not working?",
        "Explain this code to me like a beginner"
    )

    // Calculate approximate context size
    val contextCharCount = remember(selectedScope, activeFiles, currentFileName) {
        when (selectedScope) {
            AiContextScope.FULL_PROJECT -> activeFiles.sumOf { it.content.length }
            AiContextScope.CURRENT_FILE -> activeFiles.find { it.name == currentFileName }?.content?.length ?: 0
            AiContextScope.ERROR_ONLY -> 250
            AiContextScope.PROMPT_ONLY -> 0
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        // Top Header
        Surface(
            color = Color(0xFF1E293B),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, null, tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("AI Coding Assistant", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                        Text(
                            text = "${settings.provider.displayName} • ${settings.selectedModel}",
                            fontSize = 11.sp,
                            color = Color(0xFF38BDF8)
                        )
                    }
                }

                Row {
                    IconButton(onClick = onClearChat, modifier = Modifier.testTag("ai_clear_chat_btn")) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear Chat", tint = Color.LightGray)
                    }
                    IconButton(onClick = onOpenSettings, modifier = Modifier.testTag("ai_open_settings_btn")) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color(0xFF38BDF8))
                    }
                }
            }
        }

        // Context Attachment Control Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF161F30))
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AiContextScope.values().forEach { scope ->
                    FilterChip(
                        selected = selectedScope == scope,
                        onClick = { selectedScope = scope },
                        label = { Text(scope.label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF38BDF8),
                            selectedLabelColor = Color(0xFF0F172A),
                            containerColor = Color(0xFF1E293B),
                            labelColor = Color(0xFFCBD5E1)
                        )
                    )
                }
            }
            Text(
                text = "~${contextCharCount / 4} tokens",
                fontSize = 10.sp,
                color = Color(0xFF94A3B8),
                fontFamily = FontFamily.Monospace
            )
        }

        // Messages List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (chatMessages.isEmpty()) {
                item {
                    EmptyChatGreeting(onSelectPrompt = { inputPrompt = it })
                }
            }

            items(chatMessages, key = { it.id }) { msg ->
                ChatMessageItem(
                    message = msg,
                    onApplyProposal = onApplyProposal
                )
            }

            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color(0xFF38BDF8), strokeWidth = 2.dp)
                        Spacer(Modifier.width(10.dp))
                        Text("HTML Live AI is generating code and explanations...", fontSize = 12.sp, color = Color(0xFF94A3B8))
                    }
                }
            }
        }

        // Prompt Library Chips Carousel
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            promptLibrary.forEach { p ->
                Surface(
                    onClick = { inputPrompt = p },
                    color = Color(0xFF1E293B),
                    shape = MaterialTheme.shapes.extraSmall,
                    modifier = Modifier.testTag("prompt_chip_${p.take(5)}")
                ) {
                    Text(
                        text = p,
                        fontSize = 11.sp,
                        color = Color(0xFFCBD5E1),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Bottom Input Bar
        Surface(
            color = Color(0xFF1E293B),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputPrompt,
                    onValueChange = { inputPrompt = it },
                    placeholder = { Text("Ask AI to build, explain, fix, or optimize...", fontSize = 13.sp) },
                    modifier = Modifier.weight(1f).testTag("ai_input_text_field"),
                    maxLines = 4
                )

                Button(
                    onClick = {
                        val text = inputPrompt.trim()
                        if (text.isNotBlank() && !isLoading) {
                            val contextMap = when (selectedScope) {
                                AiContextScope.FULL_PROJECT -> activeFiles.associate { it.name to it.content }
                                AiContextScope.CURRENT_FILE -> activeFiles.find { it.name == currentFileName }?.let { mapOf(it.name to it.content) } ?: emptyMap()
                                else -> emptyMap()
                            }
                            onSendMessage(text, contextMap, selectedScope)
                            inputPrompt = ""
                        }
                    },
                    enabled = inputPrompt.isNotBlank() && !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8), contentColor = Color(0xFF0F172A)),
                    modifier = Modifier.size(48.dp).testTag("ai_send_btn")
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun EmptyChatGreeting(onSelectPrompt: (String) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161F30)),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Psychology, null, tint = Color(0xFF38BDF8), modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                Text("AI Development Engine", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Describe websites, canvas games, or styling in natural language. HTML Live will generate real code, explain mechanics, or debug runtime errors.",
                fontSize = 13.sp,
                color = Color(0xFF94A3B8),
                lineHeight = 18.sp
            )
            Spacer(Modifier.height(12.dp))
            Text("Try one of these:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
            Spacer(Modifier.height(6.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(
                    "Create a 2D canvas platformer with jump physics and touch controls",
                    "Build a modern SaaS landing page with pricing cards",
                    "Fix this error and explain why it happened"
                ).forEach { sample ->
                    Text(
                        text = "• $sample",
                        fontSize = 12.sp,
                        color = Color(0xFFCBD5E1),
                        modifier = Modifier.testTag("sample_prompt_${sample.take(6)}")
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatMessageItem(
    message: AiChatMessage,
    onApplyProposal: (AiChangeProposal) -> Unit
) {
    val isUser = message.role == "user"

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (isUser) Color(0xFF0284C7) else Color(0xFF1E293B),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth(if (isUser) 0.85f else 0.98f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = if (isUser) "You" else "HTML Live AI",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (isUser) Color.White else Color(0xFF38BDF8)
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = message.content,
                    fontSize = 13.sp,
                    color = Color.White,
                    lineHeight = 19.sp
                )

                // Teaching Steps if in teach-while-building mode
                if (!message.teachingSteps.isNullOrEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    Text("Step-by-Step Educational Breakdown:", fontWeight = FontWeight.Bold, color = Color(0xFFFBBF24), fontSize = 12.sp)
                    Spacer(Modifier.height(4.dp))
                    message.teachingSteps.forEach { step ->
                        Surface(
                            color = Color(0xFF0F172A),
                            shape = MaterialTheme.shapes.extraSmall,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Step ${step.stepNumber}: ${step.title}", fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8), fontSize = 12.sp)
                                Text(step.explanation, fontSize = 11.sp, color = Color(0xFFCBD5E1))
                            }
                        }
                    }
                }

                // Change Proposal Card (Diff & Apply)
                if (message.changeProposal != null) {
                    val proposal = message.changeProposal
                    Spacer(Modifier.height(10.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Code, null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(proposal.title, fontWeight = FontWeight.Bold, color = Color(0xFF10B981), fontSize = 13.sp)
                            }
                            Text(proposal.description, fontSize = 11.sp, color = Color(0xFF94A3B8))

                            Spacer(Modifier.height(6.dp))
                            proposal.files.keys.forEach { fname ->
                                Text("✓ $fname (+${proposal.files[fname]?.length ?: 0} bytes)", fontSize = 11.sp, color = Color(0xFF38BDF8), fontFamily = FontFamily.Monospace)
                            }

                            Spacer(Modifier.height(10.dp))
                            if (proposal.isApplied) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Check, null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Changes Applied to Project", color = Color(0xFF10B981), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = {
                                        proposal.isApplied = true
                                        onApplyProposal(proposal)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981), contentColor = Color(0xFF0F172A)),
                                    modifier = Modifier.fillMaxWidth().testTag("apply_ai_changes_btn")
                                ) {
                                    Icon(Icons.Default.Check, null, Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Apply Changes to Project", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
