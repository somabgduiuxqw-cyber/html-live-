package com.example.ui.screens.git

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Commit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Difference
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.git.DiffLineType
import com.example.data.git.GitFileDiff
import com.example.data.git.GitMergeConflict
import com.example.data.git.GitProgressUpdate
import com.example.data.git.GitRepositoryInfo
import com.example.data.git.GitStatusResult
import com.example.data.git.GitTutorialRepository
import com.example.model.Project
import com.example.model.ProjectFile

enum class GitTab(val title: String) {
    STATUS("Status"),
    DIFF("Diff"),
    COMMIT("Commit & Push"),
    BRANCHES("Branches"),
    PULL("Pull Updates"),
    FILES("Files Tree"),
    README("README"),
    INFO("Repo Info"),
    AI_GIT("Git + AI"),
    TUTORIAL("Git Tutorial")
}

@Composable
fun GitProjectScreen(
    project: Project,
    files: List<ProjectFile>,
    repoInfo: GitRepositoryInfo?,
    status: GitStatusResult,
    diffs: List<GitFileDiff>,
    branches: List<String>,
    readmeContent: String?,
    conflicts: List<GitMergeConflict>,
    isOperating: Boolean,
    operationProgress: GitProgressUpdate?,
    onSwitchBranch: (String) -> Unit,
    onPull: () -> Unit,
    onCommit: (message: String, files: List<String>) -> Unit,
    onPush: () -> Unit,
    onResolveConflict: (filePath: String, chosenContent: String) -> Unit,
    onRefreshStatus: () -> Unit,
    onSaveReadme: (String) -> Unit,
    onOpenFile: (ProjectFile) -> Unit,
    onDeleteFile: (String) -> Unit,
    onCreateFile: (String) -> Unit,
    onOpenInEditor: () -> Unit,
    onRunPreview: () -> Unit,
    onAiPrompt: (prompt: String, contextFiles: Map<String, String>) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(GitTab.STATUS) }
    var commitMessage by remember { mutableStateOf("") }
    val selectedFilesForCommit = remember { mutableStateMapOf<String, Boolean>() }
    var showPushConfirmDialog by remember { mutableStateOf(false) }
    var activeConflictToEdit by remember { mutableStateOf<GitMergeConflict?>(null) }
    var newBranchName by remember { mutableStateOf("") }
    var showNewBranchDialog by remember { mutableStateOf(false) }
    var isEditingReadme by remember { mutableStateOf(false) }
    var editableReadme by remember { mutableStateOf(readmeContent ?: "") }
    var fileSearchQuery by remember { mutableStateOf("") }
    var showNewFileDialog by remember { mutableStateOf(false) }
    var newFileNameInput by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        // App Header & Quick Action Menu (Requirement 58)
        Surface(
            color = Color(0xFF1E293B),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("git_screen_back_btn")) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Spacer(Modifier.width(6.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = project.name,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1
                                )
                                Spacer(Modifier.width(8.dp))
                                Surface(
                                    color = Color(0xFF0284C7),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                ) {
                                    Text(
                                        text = repoInfo?.currentBranch ?: "main",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = repoInfo?.remoteUrl ?: "Local Git Repo",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8),
                                maxLines = 1
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = onRunPreview, modifier = Modifier.testTag("git_header_run_btn")) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Run", tint = Color(0xFF10B981))
                        }
                        IconButton(onClick = onOpenInEditor, modifier = Modifier.testTag("git_header_code_btn")) {
                            Icon(Icons.Default.Code, contentDescription = "Code", tint = Color(0xFF38BDF8))
                        }
                        IconButton(onClick = onRefreshStatus) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color(0xFFCBD5E1))
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Scrollable Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    containerColor = Color(0xFF1E293B),
                    contentColor = Color(0xFF38BDF8),
                    edgePadding = 8.dp,
                    divider = {}
                ) {
                    GitTab.values().forEach { tab ->
                        val isSelected = selectedTab == tab
                        Tab(
                            selected = isSelected,
                            onClick = { selectedTab = tab },
                            text = {
                                if (tab == GitTab.STATUS && status.totalChangedCount > 0) {
                                    BadgedBox(badge = {
                                        Badge(containerColor = Color(0xFFF59E0B)) {
                                            Text("${status.totalChangedCount}", fontSize = 9.sp)
                                        }
                                    }) {
                                        Text(tab.title, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                    }
                                } else {
                                    Text(tab.title, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                }
                            }
                        )
                    }
                }
            }
        }

        // Operation progress bar
        if (isOperating && operationProgress != null) {
            Surface(color = Color(0xFF161F30), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(operationProgress.stage, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        Text("${(operationProgress.percentage * 100).toInt()}%", fontSize = 12.sp, color = Color(0xFF38BDF8))
                    }
                    Spacer(Modifier.height(6.dp))
                    androidx.compose.material3.LinearProgressIndicator(
                        progress = { operationProgress.percentage.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFF38BDF8),
                        trackColor = Color(0xFF334155)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(operationProgress.statusMessage, fontSize = 11.sp, color = Color(0xFFCBD5E1), fontFamily = FontFamily.Monospace)
                }
            }
        }

        // Main Tab Content
        Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            when (selectedTab) {
                GitTab.STATUS -> {
                    GitStatusTab(
                        status = status,
                        selectedFiles = selectedFilesForCommit,
                        onFileToggled = { file, selected -> selectedFilesForCommit[file] = selected },
                        onNavigateToDiff = { selectedTab = GitTab.DIFF },
                        onNavigateToCommit = { selectedTab = GitTab.COMMIT }
                    )
                }

                GitTab.DIFF -> {
                    GitDiffTab(diffs = diffs)
                }

                GitTab.COMMIT -> {
                    GitCommitPushTab(
                        status = status,
                        diffs = diffs,
                        commitMessage = commitMessage,
                        onCommitMessageChange = { commitMessage = it },
                        selectedFiles = selectedFilesForCommit,
                        onFileToggled = { file, sel -> selectedFilesForCommit[file] = sel },
                        onCommit = { msg, filesToCommit -> onCommit(msg, filesToCommit) },
                        onRequestPush = { showPushConfirmDialog = true },
                        currentBranch = repoInfo?.currentBranch ?: "main"
                    )
                }

                GitTab.BRANCHES -> {
                    GitBranchesTab(
                        branches = branches,
                        currentBranch = repoInfo?.currentBranch ?: "main",
                        onSwitchBranch = onSwitchBranch,
                        onOpenCreateBranch = { showNewBranchDialog = true }
                    )
                }

                GitTab.PULL -> {
                    GitPullTab(
                        conflicts = conflicts,
                        onPull = onPull,
                        onOpenConflictEditor = { activeConflictToEdit = it }
                    )
                }

                GitTab.FILES -> {
                    GitFileTreeTab(
                        files = files,
                        searchQuery = fileSearchQuery,
                        onSearchChange = { fileSearchQuery = it },
                        onOpenFile = onOpenFile,
                        onDeleteFile = onDeleteFile,
                        onCreateFileRequest = { showNewFileDialog = true }
                    )
                }

                GitTab.README -> {
                    GitReadmeTab(
                        readme = readmeContent,
                        isEditing = isEditingReadme,
                        editableText = editableReadme,
                        onEditTextChange = { editableReadme = it },
                        onStartEdit = {
                            editableReadme = readmeContent ?: "# ${project.name}\n\nProject created with HTML Live."
                            isEditingReadme = true
                        },
                        onSaveEdit = {
                            onSaveReadme(editableReadme)
                            isEditingReadme = false
                        },
                        onCancelEdit = { isEditingReadme = false }
                    )
                }

                GitTab.INFO -> {
                    GitRepoInfoTab(repoInfo = repoInfo, project = project)
                }

                GitTab.AI_GIT -> {
                    GitAiTab(
                        project = project,
                        files = files,
                        status = status,
                        diffs = diffs,
                        onAiPrompt = onAiPrompt
                    )
                }

                GitTab.TUTORIAL -> {
                    GitTutorialTab()
                }
            }
        }
    }

    // Push Confirmation Dialog (Requirement 51 & 67)
    if (showPushConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showPushConfirmDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Upload, null, tint = Color(0xFF38BDF8), modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Confirm Git Push")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Pushing local commits to remote repository.", fontSize = 13.sp)
                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Remote: ${repoInfo?.remoteName ?: "origin"}", fontSize = 12.sp, color = Color.White)
                            Text("Branch: ${repoInfo?.currentBranch ?: "main"}", fontSize = 12.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                            Text("Changed files in repo: ${status.totalChangedCount}", fontSize = 12.sp, color = Color(0xFF94A3B8))
                        }
                    }
                    Text(
                        "Are you sure you want to push? Your changes will be updated on the remote repository.",
                        fontSize = 12.sp,
                        color = Color(0xFFCBD5E1)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPushConfirmDialog = false
                        onPush()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                ) {
                    Text("Push to Origin")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPushConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Conflict Editor Modal (Requirement 52)
    if (activeConflictToEdit != null) {
        val conflict = activeConflictToEdit!!
        var manualText by remember { mutableStateOf(conflict.conflictMarkerText) }

        AlertDialog(
            onDismissRequest = { activeConflictToEdit = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, null, tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Resolve Conflict: ${conflict.filePath}")
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().height(360.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = {
                                onResolveConflict(conflict.filePath, conflict.localVersion)
                                activeConflictToEdit = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Keep Local", fontSize = 11.sp)
                        }
                        Button(
                            onClick = {
                                onResolveConflict(conflict.filePath, conflict.remoteVersion)
                                activeConflictToEdit = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Keep Remote", fontSize = 11.sp)
                        }
                    }

                    Text("Or Edit Manually:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    OutlinedTextField(
                        value = manualText,
                        onValueChange = { manualText = it },
                        modifier = Modifier.fillMaxWidth().height(220.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResolveConflict(conflict.filePath, manualText)
                        activeConflictToEdit = null
                    }
                ) {
                    Text("Apply Resolution")
                }
            },
            dismissButton = {
                TextButton(onClick = { activeConflictToEdit = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // New Branch Dialog
    if (showNewBranchDialog) {
        AlertDialog(
            onDismissRequest = { showNewBranchDialog = false },
            title = { Text("Create New Branch") },
            text = {
                OutlinedTextField(
                    value = newBranchName,
                    onValueChange = { newBranchName = it },
                    placeholder = { Text("e.g. feature/mobile-ui") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newBranchName.isNotBlank()) {
                            onSwitchBranch(newBranchName.trim())
                            newBranchName = ""
                            showNewBranchDialog = false
                        }
                    }
                ) {
                    Text("Create & Switch")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewBranchDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // New File Dialog
    if (showNewFileDialog) {
        AlertDialog(
            onDismissRequest = { showNewFileDialog = false },
            title = { Text("Create New File in Repo") },
            text = {
                OutlinedTextField(
                    value = newFileNameInput,
                    onValueChange = { newFileNameInput = it },
                    placeholder = { Text("e.g. components.js or utils.css") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFileNameInput.isNotBlank()) {
                            onCreateFile(newFileNameInput.trim())
                            newFileNameInput = ""
                            showNewFileDialog = false
                        }
                    }
                ) {
                    Text("Create File")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewFileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// -------------------------------------------------------------
// Sub-Composables for each Git Tab
// -------------------------------------------------------------

@Composable
private fun GitStatusTab(
    status: GitStatusResult,
    selectedFiles: Map<String, Boolean>,
    onFileToggled: (String, Boolean) -> Unit,
    onNavigateToDiff: () -> Unit,
    onNavigateToCommit: () -> Unit
) {
    if (status.isClean) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF10B981), modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(12.dp))
            Text("Working Tree Clean", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(Modifier.height(6.dp))
            Text("No modified, untracked, or deleted files found.", fontSize = 13.sp, color = Color(0xFF94A3B8))
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Local Git Status", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onNavigateToDiff) {
                        Icon(Icons.Default.Difference, null, modifier = Modifier.size(14.dp), tint = Color(0xFF38BDF8))
                        Spacer(Modifier.width(4.dp))
                        Text("View Diff", fontSize = 11.sp, color = Color(0xFF38BDF8))
                    }
                    Button(
                        onClick = onNavigateToCommit,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                    ) {
                        Icon(Icons.Default.Commit, null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Commit", fontSize = 11.sp)
                    }
                }
            }

            if (status.modified.isNotEmpty()) {
                StatusSection(
                    title = "Modified (${status.modified.size})",
                    badge = "M",
                    badgeColor = Color(0xFF38BDF8),
                    items = status.modified,
                    selectedFiles = selectedFiles,
                    onFileToggled = onFileToggled
                )
            }

            if (status.untracked.isNotEmpty()) {
                StatusSection(
                    title = "Untracked (${status.untracked.size})",
                    badge = "U",
                    badgeColor = Color(0xFF10B981),
                    items = status.untracked,
                    selectedFiles = selectedFiles,
                    onFileToggled = onFileToggled
                )
            }

            if (status.added.isNotEmpty()) {
                StatusSection(
                    title = "Added (${status.added.size})",
                    badge = "A",
                    badgeColor = Color(0xFF10B981),
                    items = status.added,
                    selectedFiles = selectedFiles,
                    onFileToggled = onFileToggled
                )
            }

            if (status.deleted.isNotEmpty()) {
                StatusSection(
                    title = "Deleted (${status.deleted.size})",
                    badge = "D",
                    badgeColor = Color(0xFFEF4444),
                    items = status.deleted,
                    selectedFiles = selectedFiles,
                    onFileToggled = onFileToggled
                )
            }

            if (status.conflicting.isNotEmpty()) {
                StatusSection(
                    title = "Conflicting (${status.conflicting.size})",
                    badge = "C",
                    badgeColor = Color(0xFFF59E0B),
                    items = status.conflicting,
                    selectedFiles = selectedFiles,
                    onFileToggled = onFileToggled
                )
            }
        }
    }
}

@Composable
private fun StatusSection(
    title: String,
    badge: String,
    badgeColor: Color,
    items: List<String>,
    selectedFiles: Map<String, Boolean>,
    onFileToggled: (String, Boolean) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
            items.forEach { file ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = selectedFiles[file] ?: true,
                        onCheckedChange = { onFileToggled(file, it) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF0284C7),
                            uncheckedColor = Color(0xFF64748B)
                        )
                    )
                    Surface(
                        color = badgeColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = badge,
                            color = badgeColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = file,
                        fontSize = 13.sp,
                        color = Color(0xFFCBD5E1),
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun GitDiffTab(diffs: List<GitFileDiff>) {
    if (diffs.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("No modified diffs to display.", color = Color(0xFF94A3B8), fontSize = 14.sp)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(diffs) { diff ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Difference, null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = diff.filePath,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        // Diff Lines
                        Surface(
                            color = Color(0xFF0B1120),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp).horizontalScroll(rememberScrollState())) {
                                diff.lines.forEach { line ->
                                    val (bgColor, textColor, prefix) = when (line.type) {
                                        DiffLineType.ADDED -> Triple(Color(0xFF064E3B).copy(alpha = 0.4f), Color(0xFF34D399), "+ ")
                                        DiffLineType.DELETED -> Triple(Color(0xFF7F1D1D).copy(alpha = 0.4f), Color(0xFFF87171), "- ")
                                        DiffLineType.CONTEXT -> Triple(Color.Transparent, Color(0xFF94A3B8), "  ")
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth().background(bgColor).padding(vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = prefix + line.text,
                                            color = textColor,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace
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
}

@Composable
private fun GitCommitPushTab(
    status: GitStatusResult,
    diffs: List<GitFileDiff>,
    commitMessage: String,
    onCommitMessageChange: (String) -> Unit,
    selectedFiles: Map<String, Boolean>,
    onFileToggled: (String, Boolean) -> Unit,
    onCommit: (String, List<String>) -> Unit,
    onRequestPush: () -> Unit,
    currentBranch: String
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Commit Changes", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)

                OutlinedTextField(
                    value = commitMessage,
                    onValueChange = onCommitMessageChange,
                    placeholder = { Text("e.g. Added mobile game controls", color = Color(0xFF64748B), fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedContainerColor = Color(0xFF0F172A),
                        unfocusedContainerColor = Color(0xFF0F172A)
                    )
                )

                Text("Changed Files to Stage:", fontSize = 12.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.SemiBold)
                val allChanged = (status.modified + status.added + status.untracked + status.deleted).distinct()

                if (allChanged.isEmpty()) {
                    Text("No changed files to commit.", fontSize = 12.sp, color = Color.Gray)
                } else {
                    allChanged.forEach { file ->
                        val isChecked = selectedFiles[file] ?: true
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clickable { onFileToggled(file, !isChecked) }
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { onFileToggled(file, it) },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF0284C7))
                            )
                            Text(file, fontSize = 12.sp, color = Color.White, fontFamily = FontFamily.Monospace)
                        }
                    }
                }

                Button(
                    onClick = {
                        val chosen = allChanged.filter { selectedFiles[it] ?: true }
                        onCommit(commitMessage, chosen)
                    },
                    enabled = commitMessage.isNotBlank() && allChanged.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    modifier = Modifier.fillMaxWidth().height(42.dp)
                ) {
                    Icon(Icons.Default.Commit, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Commit to Local Repository", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Push Section
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Push to Remote", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(
                    text = "Uploads committed changes to origin/$currentBranch. Explicit confirmation required.",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )

                Button(
                    onClick = onRequestPush,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                    modifier = Modifier.fillMaxWidth().height(42.dp)
                ) {
                    Icon(Icons.Default.Upload, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Push Changes", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun GitBranchesTab(
    branches: List<String>,
    currentBranch: String,
    onSwitchBranch: (String) -> Unit,
    onOpenCreateBranch: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Repository Branches", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Button(
                onClick = onOpenCreateBranch,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("New Branch", fontSize = 11.sp)
            }
        }

        branches.forEach { branch ->
            val isCurrent = branch == currentBranch
            Surface(
                onClick = { if (!isCurrent) onSwitchBranch(branch) },
                color = if (isCurrent) Color(0xFF0369A1) else Color(0xFF1E293B),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isCurrent) Icons.Default.CheckCircle else Icons.Default.Code,
                            contentDescription = null,
                            tint = if (isCurrent) Color.White else Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = branch,
                            fontSize = 14.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    if (isCurrent) {
                        Text("Current", fontSize = 11.sp, color = Color(0xFFBAE6FD), fontWeight = FontWeight.Bold)
                    } else {
                        Text("Switch", fontSize = 11.sp, color = Color(0xFF38BDF8))
                    }
                }
            }
        }
    }
}

@Composable
private fun GitPullTab(
    conflicts: List<GitMergeConflict>,
    onPull: () -> Unit,
    onOpenConflictEditor: (GitMergeConflict) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Git Pull Updates", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(
                    text = "Checks remote repository for latest commits and merges them into your local project files.",
                    fontSize = 12.sp,
                    color = Color(0xFFCBD5E1)
                )
                Button(
                    onClick = onPull,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    modifier = Modifier.fillMaxWidth().height(42.dp)
                ) {
                    Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Pull from Remote", fontWeight = FontWeight.Bold)
                }
            }
        }

        if (conflicts.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF7F1D1D)),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, null, tint = Color(0xFFFCA5A5), modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Merge Conflicts (${conflicts.size})",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Text(
                        text = "Files were modified in both local and remote branches. Resolve conflicts to complete merge:",
                        fontSize = 12.sp,
                        color = Color(0xFFFECACA)
                    )

                    conflicts.forEach { conflict ->
                        Surface(
                            color = Color(0xFF450A0A),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(conflict.filePath, fontSize = 12.sp, color = Color.White, fontFamily = FontFamily.Monospace)
                                Button(
                                    onClick = { onOpenConflictEditor(conflict) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                                ) {
                                    Text("Open Conflict Editor", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GitFileTreeTab(
    files: List<ProjectFile>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onOpenFile: (ProjectFile) -> Unit,
    onDeleteFile: (String) -> Unit,
    onCreateFileRequest: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search repo files...", fontSize = 12.sp, color = Color(0xFF64748B)) },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF38BDF8),
                    unfocusedBorderColor = Color(0xFF334155),
                    focusedContainerColor = Color(0xFF1E293B),
                    unfocusedContainerColor = Color(0xFF1E293B)
                ),
                modifier = Modifier.weight(1f).height(46.dp)
            )
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = onCreateFileRequest,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                modifier = Modifier.height(46.dp)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("New File", fontSize = 11.sp)
            }
        }

        val filteredFiles = files.filter { it.name.contains(searchQuery, ignoreCase = true) }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(filteredFiles) { file ->
                Surface(
                    onClick = { onOpenFile(file) },
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val iconColor = when (file.extension) {
                                "html" -> Color(0xFFF97316)
                                "css" -> Color(0xFF38BDF8)
                                "js" -> Color(0xFFFACC15)
                                "json" -> Color(0xFFA855F7)
                                "md" -> Color(0xFF10B981)
                                else -> Color(0xFF94A3B8)
                            }
                            Icon(Icons.Default.Code, null, tint = iconColor, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(file.name, fontSize = 13.sp, color = Color.White, fontFamily = FontFamily.Monospace)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${file.content.length} B", fontSize = 10.sp, color = Color(0xFF64748B))
                            Spacer(Modifier.width(8.dp))
                            IconButton(
                                onClick = { onDeleteFile(file.name) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Delete, null, tint = Color(0xFFEF4444), modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GitReadmeTab(
    readme: String?,
    isEditing: Boolean,
    editableText: String,
    onEditTextChange: (String) -> Unit,
    onStartEdit: () -> Unit,
    onSaveEdit: () -> Unit,
    onCancelEdit: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("README.md", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            if (!isEditing) {
                Button(
                    onClick = onStartEdit,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                ) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Edit README", fontSize = 11.sp)
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(onClick = onCancelEdit) {
                        Text("Cancel", fontSize = 11.sp)
                    }
                    Button(
                        onClick = onSaveEdit,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Text("Save", fontSize = 11.sp)
                    }
                }
            }
        }

        if (isEditing) {
            OutlinedTextField(
                value = editableText,
                onValueChange = onEditTextChange,
                modifier = Modifier.fillMaxWidth().height(380.dp),
                textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp)
            )
        } else {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = readme ?: "No README.md found in repository.",
                        fontSize = 13.sp,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 20.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
private fun GitRepoInfoTab(repoInfo: GitRepositoryInfo?, project: Project) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Git Project Information", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)

                InfoRow("Repository Name", repoInfo?.repoName ?: project.name)
                InfoRow("Owner / Org", repoInfo?.owner ?: "Unknown")
                InfoRow("Default Branch", repoInfo?.defaultBranch ?: "main")
                InfoRow("Current Branch", repoInfo?.currentBranch ?: "main")
                InfoRow("Remote URL", repoInfo?.remoteUrl ?: "Local")
                InfoRow("Local Project Size", repoInfo?.formattedSize ?: "Calculated on sync")
                InfoRow("Detected Project Type", repoInfo?.detectedType?.title ?: "HTML/CSS/JavaScript")
                InfoRow("Contains index.html", if (repoInfo?.hasIndexHtml == true) "Yes (Ready to Run)" else "No")
                InfoRow("Node / Bundler", if (repoInfo?.hasPackageJson == true) "Yes (package.json present)" else "None")
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 12.sp, color = Color(0xFF94A3B8))
        Text(value, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun GitAiTab(
    project: Project,
    files: List<ProjectFile>,
    status: GitStatusResult,
    diffs: List<GitFileDiff>,
    onAiPrompt: (String, Map<String, String>) -> Unit
) {
    val quickAiActions = listOf(
        "Explain this project architecture and dependencies",
        "Review my modified files and suggest improvements",
        "Explain the Git changes in detail",
        "Fix errors and optimize JavaScript performance",
        "Make this web project mobile responsive",
        "Add touch controls for mobile screens"
    )

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Git + AI Assistant", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Text(
                    text = "Let AI inspect your cloned files, explain Git changes, and propose code updates with diff previews.",
                    fontSize = 12.sp,
                    color = Color(0xFFCBD5E1)
                )
            }
        }

        Text("Select AI Assistance Task:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)

        quickAiActions.forEach { action ->
            Surface(
                onClick = {
                    val contextMap = files.associate { it.name to it.content }
                    onAiPrompt(action, contextMap)
                },
                color = Color(0xFF161F30),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(action, fontSize = 12.sp, color = Color.White, modifier = Modifier.weight(1f))
                    Icon(Icons.Default.AutoAwesome, null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun GitTutorialTab() {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Git Learning & Tutorials for Beginners", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(
            text = "Master core version control concepts interactively:",
            fontSize = 12.sp,
            color = Color(0xFF94A3B8)
        )

        GitTutorialRepository.topics.forEach { topic ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(topic.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                    Text(topic.subtitle, fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.SemiBold)
                    Text(topic.explanation, fontSize = 12.sp, color = Color(0xFFCBD5E1), lineHeight = 18.sp)

                    Surface(
                        color = Color(0xFF0F172A),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = topic.commandExample,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color(0xFF34D399),
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    Text("Tip: " + topic.practicalTip, fontSize = 11.sp, color = Color(0xFFFDE68A))
                }
            }
        }
    }
}
