package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Commit
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Source
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.VerticalSplit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

enum class WorkspaceView {
    FILES,
    EDITOR,
    SPLIT,
    PREVIEW
}

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
    onRenameFile: ((oldName: String, newName: String) -> Unit)? = null,
    onDuplicateFile: ((fileName: String) -> Unit)? = null,
    onConsoleMessage: (ConsoleMessage) -> Unit,
    onClearConsole: () -> Unit,
    onFixWithAi: (ConsoleMessage) -> Unit,
    onOpenAiTab: () -> Unit,
    onLearnTopic: (String) -> Unit,
    onPackageApk: (() -> Unit)? = null,
    onInspectSource: (() -> Unit)? = null,
    onOpenGit: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var workspaceView by remember { mutableStateOf(WorkspaceView.SPLIT) }
    var isEditorFullscreen by remember { mutableStateOf(false) }
    var isPreviewFullscreen by remember { mutableStateOf(false) }
    var showConsoleDrawer by remember { mutableStateOf(false) }
    var showNewFileDialog by remember { mutableStateOf(false) }
    var newFileNameInput by remember { mutableStateOf("") }
    var fileToRename by remember { mutableStateOf<ProjectFile?>(null) }
    var renameFileInput by remember { mutableStateOf("") }

    val activeFile = files.find { it.name == currentFileName } ?: files.firstOrNull()
    val activeContent = activeFile?.content ?: ""

    val errorCount = consoleMessages.count { it.level == ConsoleLevel.ERROR }

    // Intercept Back button when in any fullscreen mode to exit fullscreen smoothly
    BackHandler(enabled = isEditorFullscreen || isPreviewFullscreen) {
        if (isEditorFullscreen) isEditorFullscreen = false
        if (isPreviewFullscreen) isPreviewFullscreen = false
    }

    if (isEditorFullscreen && activeFile != null) {
        // FULLSCREEN EDITOR MODE
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A))
        ) {
            CodeEditorView(
                code = activeContent,
                fileName = activeFile.name,
                settings = editorSettings,
                onCodeChanged = { newCode ->
                    onCodeChanged(activeFile.name, newCode)
                },
                isFullscreen = true,
                onToggleFullscreen = { isEditorFullscreen = false },
                modifier = Modifier.fillMaxSize()
            )
        }
    } else if (isPreviewFullscreen) {
        // FULLSCREEN PREVIEW MODE
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A))
        ) {
            LivePreviewView(
                files = files,
                consoleMessages = consoleMessages,
                onConsoleMessage = onConsoleMessage,
                onOpenConsole = { showConsoleDrawer = true },
                isFullscreen = true,
                onToggleFullscreen = { isPreviewFullscreen = false },
                onOpenSourceViewer = onInspectSource,
                modifier = Modifier.fillMaxSize()
            )
        }
    } else {
        // UNIFIED WORKSPACE
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A))
        ) {
            // Workspace Top Toolbar: Project Name + Workspace Tabs + Actions
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
                    // Project Title
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = project.name,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )
                    }

                    // Workspace Mode Selector: FILES | EDITOR | SPLIT | PREVIEW
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        IconButton(
                            onClick = { workspaceView = WorkspaceView.FILES },
                            modifier = Modifier.size(32.dp).testTag("mode_files_btn")
                        ) {
                            Icon(
                                Icons.Default.Folder,
                                contentDescription = "Files Manager",
                                tint = if (workspaceView == WorkspaceView.FILES) Color(0xFF38BDF8) else Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = { workspaceView = WorkspaceView.EDITOR },
                            modifier = Modifier.size(32.dp).testTag("mode_editor_only_btn")
                        ) {
                            Icon(
                                Icons.Default.Code,
                                contentDescription = "Editor Only",
                                tint = if (workspaceView == WorkspaceView.EDITOR) Color(0xFF38BDF8) else Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = { workspaceView = WorkspaceView.SPLIT },
                            modifier = Modifier.size(32.dp).testTag("mode_split_btn")
                        ) {
                            Icon(
                                Icons.Default.VerticalSplit,
                                contentDescription = "Split View",
                                tint = if (workspaceView == WorkspaceView.SPLIT) Color(0xFF38BDF8) else Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = { workspaceView = WorkspaceView.PREVIEW },
                            modifier = Modifier.size(32.dp).testTag("mode_preview_only_btn")
                        ) {
                            Icon(
                                Icons.Default.Visibility,
                                contentDescription = "Preview Only",
                                tint = if (workspaceView == WorkspaceView.PREVIEW) Color(0xFF38BDF8) else Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Inspect Source Code button
                        if (onInspectSource != null) {
                            IconButton(
                                onClick = onInspectSource,
                                modifier = Modifier.size(32.dp).testTag("editor_inspect_source_btn")
                            ) {
                                Icon(
                                    Icons.Default.Source,
                                    contentDescription = "Inspect Source Code",
                                    tint = Color(0xFFF97316),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Console Drawer Button
                        IconButton(
                            onClick = { showConsoleDrawer = !showConsoleDrawer },
                            modifier = Modifier.size(32.dp).testTag("toolbar_console_btn")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (errorCount > 0) {
                                        Badge(containerColor = Color(0xFFEF4444)) {
                                            Text("$errorCount", color = Color.White, fontSize = 8.sp)
                                        }
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

                        // Git Repo button
                        if (onOpenGit != null) {
                            IconButton(
                                onClick = onOpenGit,
                                modifier = Modifier.size(32.dp).testTag("editor_git_btn")
                            ) {
                                Icon(Icons.Default.Commit, contentDescription = "Git Repository", tint = Color(0xFFA78BFA), modifier = Modifier.size(18.dp))
                            }
                        }

                        // Package APK button
                        if (onPackageApk != null) {
                            IconButton(
                                onClick = onPackageApk,
                                modifier = Modifier.size(32.dp).testTag("editor_package_apk_btn")
                            ) {
                                Icon(Icons.Default.Android, contentDescription = "Package to APK", tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            // File Tabs Strip (shown when in editor or split mode)
            if (workspaceView != WorkspaceView.FILES) {
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
            }

            // Main Workspace Content
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (workspaceView) {
                    WorkspaceView.FILES -> {
                        ProjectFileManagerView(
                            files = files,
                            activeFileName = currentFileName,
                            onSelectFile = { fileName ->
                                onSelectFile(fileName)
                                workspaceView = WorkspaceView.EDITOR
                            },
                            onAddNewFile = { showNewFileDialog = true },
                            onDeleteFile = onDeleteFile,
                            onRenameFile = { file ->
                                fileToRename = file
                                renameFileInput = file.name
                            },
                            onDuplicateFile = { fileName ->
                                onDuplicateFile?.invoke(fileName)
                            }
                        )
                    }

                    WorkspaceView.EDITOR -> {
                        if (activeFile != null) {
                            CodeEditorView(
                                code = activeContent,
                                fileName = activeFile.name,
                                settings = editorSettings,
                                onCodeChanged = { newCode ->
                                    onCodeChanged(activeFile.name, newCode)
                                },
                                isFullscreen = false,
                                onToggleFullscreen = { isEditorFullscreen = true },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    WorkspaceView.SPLIT -> {
                        SplitEditorPreview(
                            mode = ViewDisplayMode.SPLIT,
                            editorContent = {
                                if (activeFile != null) {
                                    CodeEditorView(
                                        code = activeContent,
                                        fileName = activeFile.name,
                                        settings = editorSettings,
                                        onCodeChanged = { newCode ->
                                            onCodeChanged(activeFile.name, newCode)
                                        },
                                        isFullscreen = false,
                                        onToggleFullscreen = { isEditorFullscreen = true }
                                    )
                                }
                            },
                            previewContent = {
                                LivePreviewView(
                                    files = files,
                                    consoleMessages = consoleMessages,
                                    onConsoleMessage = onConsoleMessage,
                                    onOpenConsole = { showConsoleDrawer = true },
                                    isFullscreen = false,
                                    onToggleFullscreen = { isPreviewFullscreen = true },
                                    onOpenSourceViewer = onInspectSource
                                )
                            }
                        )
                    }

                    WorkspaceView.PREVIEW -> {
                        LivePreviewView(
                            files = files,
                            consoleMessages = consoleMessages,
                            onConsoleMessage = onConsoleMessage,
                            onOpenConsole = { showConsoleDrawer = true },
                            isFullscreen = false,
                            onToggleFullscreen = { isPreviewFullscreen = true },
                            onOpenSourceViewer = onInspectSource,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Developer Console Drawer
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
                            onErrorClicked = { fName, _ ->
                                onSelectFile(fName)
                                workspaceView = WorkspaceView.EDITOR
                                showConsoleDrawer = false
                            },
                            onFixWithAi = { msg ->
                                onFixWithAi(msg)
                                showConsoleDrawer = false
                            },
                            onLearnTopic = onLearnTopic,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }

    // New File Dialog
    if (showNewFileDialog) {
        AlertDialog(
            onDismissRequest = { showNewFileDialog = false },
            title = { Text("Create File in Project", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Enter file name with extension (.html, .css, .js, .json):",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                    OutlinedTextField(
                        value = newFileNameInput,
                        onValueChange = { newFileNameInput = it },
                        label = { Text("File Name (e.g., about.html)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("new_file_input")
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(".html", ".css", ".js", ".json").forEach { ext ->
                            Surface(
                                color = Color(0xFF1E293B),
                                shape = RoundedCornerShape(4.dp),
                                onClick = {
                                    val base = newFileNameInput.substringBeforeLast('.')
                                    newFileNameInput = if (base.isNotEmpty()) "$base$ext" else "new_file$ext"
                                }
                            ) {
                                Text(
                                    ext,
                                    fontSize = 11.sp,
                                    color = Color(0xFF38BDF8),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = newFileNameInput.trim()
                        if (name.isNotEmpty()) {
                            onAddNewFile(name)
                            newFileNameInput = ""
                            showNewFileDialog = false
                        }
                    },
                    modifier = Modifier.testTag("dialog_create_file_confirm")
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewFileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Rename File Dialog
    if (fileToRename != null) {
        AlertDialog(
            onDismissRequest = { fileToRename = null },
            title = { Text("Rename File", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = renameFileInput,
                    onValueChange = { renameFileInput = it },
                    label = { Text("File Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newName = renameFileInput.trim()
                        if (newName.isNotEmpty() && onRenameFile != null) {
                            onRenameFile(fileToRename!!.name, newName)
                        }
                        fileToRename = null
                    }
                ) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { fileToRename = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ProjectFileManagerView(
    files: List<ProjectFile>,
    activeFileName: String,
    onSelectFile: (String) -> Unit,
    onAddNewFile: () -> Unit,
    onDeleteFile: (String) -> Unit,
    onRenameFile: (ProjectFile) -> Unit,
    onDuplicateFile: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Project Files Tree", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("${files.size} files in project", fontSize = 11.sp, color = Color(0xFF94A3B8))
            }
            Button(
                onClick = onAddNewFile,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Add File", fontSize = 11.sp)
            }
        }

        Spacer(Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(files, key = { it.id }) { file ->
                var menuExpanded by remember { mutableStateOf(false) }
                val isSelected = file.name == activeFileName
                val lineCount = file.content.lines().size
                val charCount = file.content.length

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFF1E293B) else Color(0xFF141D2B)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8)) else null,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onSelectFile(file.name) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(getFileColor(file.extension).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = file.extension.uppercase().take(4).ifEmpty { "FILE" },
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = getFileColor(file.extension)
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = file.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                if (file.isMain) {
                                    Spacer(Modifier.width(6.dp))
                                    Surface(
                                        color = Color(0xFF10B981).copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            "ENTRY",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF34D399),
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "$lineCount lines • $charCount characters",
                                fontSize = 10.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        Button(
                            onClick = { onSelectFile(file.name) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Edit", fontSize = 11.sp)
                        }

                        Box {
                            IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(30.dp)) {
                                Icon(Icons.Default.MoreVert, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                            }
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false },
                                modifier = Modifier.background(Color(0xFF1E293B))
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Open in Editor", color = Color.White, fontSize = 12.sp) },
                                    leadingIcon = { Icon(Icons.Default.Code, null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp)) },
                                    onClick = {
                                        menuExpanded = false
                                        onSelectFile(file.name)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Rename", color = Color.White, fontSize = 12.sp) },
                                    leadingIcon = { Icon(Icons.Default.Edit, null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp)) },
                                    onClick = {
                                        menuExpanded = false
                                        onRenameFile(file)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Duplicate", color = Color.White, fontSize = 12.sp) },
                                    leadingIcon = { Icon(Icons.Default.ContentCopy, null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp)) },
                                    onClick = {
                                        menuExpanded = false
                                        onDuplicateFile(file.name)
                                    }
                                )
                                if (!file.isMain && files.size > 1) {
                                    DropdownMenuItem(
                                        text = { Text("Delete", color = Color(0xFFEF4444), fontSize = 12.sp) },
                                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp)) },
                                        onClick = {
                                            menuExpanded = false
                                            onDeleteFile(file.name)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun getFileColor(ext: String): Color {
    return when (ext.lowercase()) {
        "html", "htm" -> Color(0xFFF97316)
        "css" -> Color(0xFF38BDF8)
        "js", "javascript" -> Color(0xFFFBBF24)
        "json" -> Color(0xFF34D399)
        "md", "markdown" -> Color(0xFFA78BFA)
        else -> Color(0xFF94A3B8)
    }
}
