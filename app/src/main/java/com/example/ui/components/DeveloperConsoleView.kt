package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.model.ConsoleLevel
import com.example.model.ConsoleMessage

@Composable
fun DeveloperConsoleView(
    messages: List<ConsoleMessage>,
    onClear: () -> Unit,
    onClose: () -> Unit,
    onErrorClicked: (fileName: String, line: Int) -> Unit,
    onFixWithAi: (ConsoleMessage) -> Unit,
    onLearnTopic: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf<ConsoleLevel?>(null) }
    var selectedMessageForDetail by remember { mutableStateOf<ConsoleMessage?>(null) }

    val filteredMessages = remember(messages, selectedFilter) {
        if (selectedFilter == null) messages else messages.filter { it.level == selectedFilter }
    }

    val errorCount = messages.count { it.level == ConsoleLevel.ERROR }
    val warnCount = messages.count { it.level == ConsoleLevel.WARN }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        // Console Header Bar
        Surface(
            color = Color(0xFF1E293B),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Console",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    Spacer(Modifier.width(8.dp))
                    if (errorCount == 0 && warnCount == 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("No errors", color = Color(0xFF10B981), fontSize = 12.sp)
                        }
                    } else {
                        if (errorCount > 0) {
                            Text("❌ $errorCount", color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(6.dp))
                        }
                        if (warnCount > 0) {
                            Text("⚠ $warnCount", color = Color(0xFFF59E0B), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Row {
                    IconButton(onClick = onClear, modifier = Modifier.testTag("console_clear_btn")) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear", tint = Color.LightGray)
                    }
                    IconButton(onClick = onClose, modifier = Modifier.testTag("console_close_btn")) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
            }
        }

        // Filter Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedFilter == null,
                onClick = { selectedFilter = null },
                label = { Text("All (${messages.size})", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF38BDF8),
                    selectedLabelColor = Color(0xFF0F172A),
                    containerColor = Color(0xFF1E293B),
                    labelColor = Color(0xFF94A3B8)
                )
            )
            FilterChip(
                selected = selectedFilter == ConsoleLevel.ERROR,
                onClick = { selectedFilter = if (selectedFilter == ConsoleLevel.ERROR) null else ConsoleLevel.ERROR },
                label = { Text("Errors ($errorCount)", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFEF4444),
                    selectedLabelColor = Color.White,
                    containerColor = Color(0xFF1E293B),
                    labelColor = Color(0xFFEF4444)
                )
            )
            FilterChip(
                selected = selectedFilter == ConsoleLevel.WARN,
                onClick = { selectedFilter = if (selectedFilter == ConsoleLevel.WARN) null else ConsoleLevel.WARN },
                label = { Text("Warnings ($warnCount)", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFF59E0B),
                    selectedLabelColor = Color.Black,
                    containerColor = Color(0xFF1E293B),
                    labelColor = Color(0xFFF59E0B)
                )
            )
        }

        // Beginner Safety Net: Explain the latest error in friendly terms
        val latestError = messages.lastOrNull { it.level == ConsoleLevel.ERROR }
        if (latestError != null) {
            BeginnerErrorCard(
                error = latestError,
                onFixWithAi = { onFixWithAi(latestError) },
                onLearn = { onLearnTopic(getTopicFromError(latestError.message)) }
            )
        }

        // Message List
        if (filteredMessages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Console is empty.\nJavaScript logs and runtime errors will appear here.",
                    color = Color(0xFF64748B),
                    fontSize = 13.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(filteredMessages, key = { it.id }) { msg ->
                    ConsoleRow(
                        message = msg,
                        onClick = { onErrorClicked(msg.sourceFile, msg.lineNumber) },
                        onFixWithAi = { onFixWithAi(msg) }
                    )
                }
            }
        }
    }
}

@Composable
fun ConsoleRow(
    message: ConsoleMessage,
    onClick: () -> Unit,
    onFixWithAi: () -> Unit
) {
    val (bgColor, textColor, icon) = when (message.level) {
        ConsoleLevel.ERROR -> Triple(Color(0xFF451A1A), Color(0xFFFCA5A5), Icons.Default.Error)
        ConsoleLevel.WARN -> Triple(Color(0xFF452E10), Color(0xFFFDE68A), Icons.Default.Warning)
        ConsoleLevel.INFO -> Triple(Color(0xFF0F2942), Color(0xFFBAE6FD), Icons.Default.Info)
        ConsoleLevel.LOG -> Triple(Color(0xFF0F172A), Color(0xFFE2E8F0), Icons.Default.Info)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                icon,
                contentDescription = message.level.name,
                tint = textColor,
                modifier = Modifier.size(16.dp).padding(top = 2.dp)
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = message.message,
                    color = textColor,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = if (message.level == ConsoleLevel.ERROR) FontWeight.SemiBold else FontWeight.Normal
                )
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${message.sourceFile}:${message.lineNumber}",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    if (message.level == ConsoleLevel.ERROR) {
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "⚡ Fix with AI",
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable(onClick = onFixWithAi)
                                .testTag("row_fix_ai_btn")
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BeginnerErrorCard(
    error: ConsoleMessage,
    onFixWithAi: () -> Unit,
    onLearn: () -> Unit
) {
    val friendly = translateErrorForBeginners(error.message)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Error, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Something went wrong",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 14.sp
                )
            }

            Spacer(Modifier.height(6.dp))
            Text(
                text = "What it means:\n${friendly.meaning}",
                fontSize = 12.sp,
                color = Color(0xFFCBD5E1)
            )

            Spacer(Modifier.height(4.dp))
            Text(
                text = "Possible reason:\n${friendly.reason}",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
            )

            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ElevatedButton(
                    onClick = onFixWithAi,
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = Color(0xFF38BDF8),
                        contentColor = Color(0xFF0F172A)
                    ),
                    modifier = Modifier.testTag("beginner_ask_ai_btn")
                ) {
                    Icon(Icons.Default.AutoAwesome, null, Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Ask AI to Fix", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onLearn,
                    modifier = Modifier.testTag("beginner_learn_btn")
                ) {
                    Text("Learn this topic", fontSize = 11.sp, color = Color(0xFF38BDF8))
                }
            }
        }
    }
}

data class FriendlyErrorExplanation(val meaning: String, val reason: String)

fun translateErrorForBeginners(rawError: String): FriendlyErrorExplanation {
    return when {
        rawError.contains("ReferenceError", ignoreCase = true) -> FriendlyErrorExplanation(
            meaning = "JavaScript tried to use a variable or function that does not exist yet.",
            reason = "A typo in a name, or you used a variable before declaring it with 'const' or 'let'."
        )
        rawError.contains("SyntaxError", ignoreCase = true) -> FriendlyErrorExplanation(
            meaning = "JavaScript found code structure it could not understand.",
            reason = "A missing bracket { }, parenthesis ( ), or quotation mark \" \"."
        )
        rawError.contains("TypeError", ignoreCase = true) -> FriendlyErrorExplanation(
            meaning = "An operation was performed on an unexpected type or null object.",
            reason = "Often caused by calling getElementById() on an ID that doesn't exist in index.html."
        )
        else -> FriendlyErrorExplanation(
            meaning = "A runtime error occurred in your script.",
            reason = "An unhandled condition occurred during execution."
        )
    }
}

fun getTopicFromError(errorMsg: String): String {
    return when {
        errorMsg.contains("ReferenceError") -> "Variables & Scope"
        errorMsg.contains("SyntaxError") -> "Document Structure & Syntax"
        errorMsg.contains("TypeError") -> "DOM Selection & Manipulation"
        else -> "JavaScript Logic"
    }
}
