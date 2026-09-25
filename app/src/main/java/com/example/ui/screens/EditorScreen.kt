package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.VerticalSplit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ConsoleLevel
import com.example.model.ConsoleMessage
import com.example.model.EditorSettings
import com.example.model.Project
import com.example.model.ProjectFile
import com.example.ui.components.CodeEditorView
import com.example.ui.components.DeveloperConsoleView
import com.example.ui.components.LivePreviewView
import com.example.ui.components.SplitEditorPreview
import com.example.ui.components.ViewDisplayMode

@Composable
fun EditorScreen(
    project: Project,
    files: List<ProjectFile>,
    currentFileName: String,
    editorSettings: EditorSettings,
    consoleMessages: List<ConsoleMessage>,
    onSelectFile: (String) -> Unit,
    onCodeChanged: (fileName: String, newContent: String) -> Unit,
    onAddNewFile: (String) -> Unit,
    onDeleteFile: (String) -> Unit,
    onConsoleMessage: (ConsoleMessage) -> Unit,
    onClearConsole: () -> Unit,
    onFixWithAi: (ConsoleMessage) -> Unit,
    onOpenAiTab: () -> Unit,
    onLearnTopic: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var displayMode by remember { mutableStateOf(ViewDisplayMode.SPLIT) }
    var showConsoleDrawer by remember { mutableStateOf(false) }
    var showNewFileDialog by remember { mutableStateOf(false) }
    var newFileNameInput by remember { mutableStateOf("") }

    val activeFile = files.find { it.name == currentFileName } ?: files.firstOrNull()
    val activeContent = activeFile?.content ?: ""

    val errorCount = consoleMessages.count { it.level == ConsoleLevel.ERROR }
    val warnCount = consoleMessages.count { it.level == ConsoleLevel.WARN }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        // Project Bar & Display Mode Selector
        Surface(
            color = Color(0xFF1E293B),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Project Name & Type
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = project.name,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )
                }

                // Modes: Editor Only, Split, Preview Only
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = { displayMode = ViewDisplayMode.EDITOR_ONLY },
                        modifier = Modifier.size(32.dp).testTag("mode_editor_only_btn")
                    ) {
                        Icon(
                            Icons.Default.Code,
                            contentDescription = "Editor Only",
                            tint = if (displayMode == ViewDisplayMode.EDITOR_ONLY) Color(0xFF38BDF8) else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = { displayMode = ViewDisplayMode.SPLIT },
                        modifier = Modifier.size(32.dp).testTag("mode_split_btn")
                    ) {
                        Icon(
                            Icons.Default.VerticalSplit,
                            contentDescription = "Split View",
                            tint = if (displayMode == ViewDisplayMode.SPLIT) Color(0xFF38BDF8) else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = { displayMode = ViewDisplayMode.PREVIEW_ONLY },
                        modifier = Modifier.size(32.dp).testTag("mode_preview_only_btn")
                    ) {
                        Icon(
                            Icons.Default.Visibility,
                            contentDescription = "Preview Only",
                            tint = if (displayMode == ViewDisplayMode.PREVIEW_ONLY) Color(0xFF38BDF8) else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Console Drawer Button
                    IconButton(
                        onClick = { showConsoleDrawer = !showConsoleDrawer },
                        modifier = Modifier.size(32.dp).testTag("toolbar_console_btn")
                    ) {
                        BadgedBox(
                            badge = {
                                if (errorCount > 0) {
                                    Badge(containerColor = Color(0xFFEF4444)) { Text("$errorCount", color = Color.White, fontSize = 8.sp) }
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.Terminal,
                                contentDescription = "Console",
                                tint = if (errorCount > 0) Color(0xFFEF4444) else Color(0xFF38BDF8),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Ask AI button
                    IconButton(
                        onClick = onOpenAiTab,
                        modifier = Modifier.size(32.dp).testTag("editor_ask_ai_shortcut_btn")
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "AI Assistant", tint = Color(0xFFFBBF24), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // File Tabs Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF161F30))
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            files.forEach { file ->
                val isSelected = file.name == currentFileName
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectFile(file.name) },
                    label = { Text(file.name, fontSize = 11.sp) },
                    trailingIcon = if (!file.isMain && files.size > 1) {
                        {
                            IconButton(onClick = { onDeleteFile(file.name) }, modifier = Modifier.size(16.dp)) {
                                Icon(Icons.Default.Close, null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                            }
                        }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF0284C7),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF0F172A),
                        labelColor = Color(0xFF94A3B8)
                    )
                )
            }

            IconButton(onClick = { showNewFileDialog = true }, modifier = Modifier.size(28.dp).testTag("add_file_btn")) {
                Icon(Icons.Default.Add, contentDescription = "Add File", tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
            }
        }

        // Main Editor / Preview Panes
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            SplitEditorPreview(
                mode = displayMode,
                editorContent = {
                    if (activeFile != null) {
                        CodeEditorView(
                            code = activeContent,
                            fileName = activeFile.name,
                            settings = editorSettings,
                            onCodeChanged = { newCode ->
                                onCodeChanged(activeFile.name, newCode)
                            }
                        )
                    }
                },
                previewContent = {
                    LivePreviewView(
                        files = files,
                        consoleMessages = consoleMessages,
                        onConsoleMessage = onConsoleMessage,
                        onOpenConsole = { showConsoleDrawer = true }
                    )
                }
            )

            // Developer Console Overlay Drawer
            if (showConsoleDrawer) {
                Surface(
                    color = Color(0xFF0F172A),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .align(Alignment.BottomCenter),
                    shadowElevation = 8.dp
                ) {
                    DeveloperConsoleView(
                        messages = consoleMessages,
                        onClear = onClearConsole,
                        onClose = { showConsoleDrawer = false },
                        onErrorClicked = { fName, line ->
                            onSelectFile(fName)
                            showConsoleDrawer = false
                        },
                        onFixWithAi = { msg ->
                            onFixWithAi(msg)
                            showConsoleDrawer = false
                        },
                        onLearnTopic = { topic ->
                            onLearnTopic(topic)
                            showConsoleDrawer = false
                        }
                    )
                }
            }
        }
    }

    // New File Dialog
    if (showNewFileDialog) {
        AlertDialog(
            onDismissRequest = { showNewFileDialog = false },
            title = { Text("Add Project File") },
            text = {
                OutlinedTextField(
                    value = newFileNameInput,
                    onValueChange = { newFileNameInput = it },
                    placeholder = { Text("e.g. game.js, modal.css, level1.json") },
                    singleLine = true,
                    label = { Text("File Name") },
                    modifier = Modifier.fillMaxWidth().testTag("new_file_name_input")
                )
            },
            confirmButton = {
                Button(onClick = {
                    val name = newFileNameInput.trim()
                    if (name.isNotBlank()) {
                        onAddNewFile(name)
                        newFileNameInput = ""
                        showNewFileDialog = false
                    }
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewFileDialog = false }) { Text("Cancel") }
            }
        )
    }
}
