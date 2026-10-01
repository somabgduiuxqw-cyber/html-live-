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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Commit
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Http
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Source
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.OutlinedButton
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
import com.example.data.repository.TemplateProject
import com.example.data.repository.TemplateRepository
import com.example.model.Project
import com.example.model.ProjectType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    recentProjects: List<Project>,
    userExperienceLevel: String,
    onExperienceLevelSelected: (String) -> Unit,
    onOpenProject: (Project) -> Unit,
    // Core Project & Editor
    onMyProjects: () -> Unit,
    onQuickEditor: () -> Unit,
    onCodeEditor: () -> Unit,
    onLivePreview: () -> Unit,
    onSourceCode: () -> Unit,
    // Learning & Reference
    onHtmlExamples: () -> Unit,
    onHtmlTutorials: () -> Unit,
    onHtmlTags: () -> Unit,
    onHtmlQna: () -> Unit,
    // Git Suite
    onGitCloneProject: () -> Unit,
    onGitProjects: () -> Unit,
    onGitStatus: () -> Unit,
    // Tools
    onHttpRequest: () -> Unit,
    onPhotoToCode: () -> Unit,
    onInAppBrowser: () -> Unit,
    onColorLab: () -> Unit,
    // AI Suite
    onAiAssistant: () -> Unit,
    onAiWebsiteBuilder: () -> Unit,
    onAiGameBuilder: () -> Unit,
    // Builders
    onWebsiteBuilder: () -> Unit,
    onGameBuilder: () -> Unit,
    onGameMapEditor: () -> Unit,
    onSelectTemplate: (TemplateProject) -> Unit,
    onHtmlToApk: () -> Unit,
    onCreateProject: ((String, TemplateProject?) -> Unit)? = null,
    onDeleteProject: ((String) -> Unit)? = null,
    onDuplicateProject: ((Project) -> Unit)? = null,
    onRenameProject: ((Project, String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var newProjectName by remember { mutableStateOf("") }
    var projectToRename by remember { mutableStateOf<Project?>(null) }
    var renameInput by remember { mutableStateOf("") }

    val lastProject = recentProjects.maxByOrNull { it.updatedAt } ?: recentProjects.firstOrNull()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF38BDF8)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Code, null, tint = Color(0xFF0F172A), modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("HTML Live", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Mobile Web & Game IDE", fontSize = 11.sp, color = Color(0xFF94A3B8))
                }
            }

            Surface(
                color = Color(0xFF1E293B),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Text(
                    text = "v2.0 PRO",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF38BDF8),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // -------------------------------------------------------------
        // 1. CONTINUE WORKING (Requirement 5)
        // -------------------------------------------------------------
        if (lastProject != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = MaterialTheme.shapes.large,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_continue_working_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CONTINUE WORKING",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8),
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = formatRelativeTime(lastProject.updatedAt),
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(getProjectTypeColor(lastProject.type).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                getProjectTypeIcon(lastProject.type),
                                null,
                                tint = getProjectTypeColor(lastProject.type),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = lastProject.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = lastProject.lastOpenedFile.ifEmpty { "index.html" },
                                    fontSize = 12.sp,
                                    color = Color(0xFF38BDF8)
                                )
                                Text(" • ", color = Color.Gray, fontSize = 12.sp)
                                Text(
                                    text = lastProject.type.name.lowercase().replaceFirstChar { it.uppercase() },
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onOpenProject(lastProject) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("home_continue_open_btn")
                        ) {
                            Icon(Icons.Default.Code, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Open in Editor", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = {
                                onOpenProject(lastProject)
                                onLivePreview()
                            },
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("home_continue_preview_btn")
                        ) {
                            Icon(Icons.Default.PlayArrow, null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Live Preview", fontSize = 12.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // 2. QUICK ACTIONS (Requirement 5)
        // -------------------------------------------------------------
        Text(
            text = "Quick Actions",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF94A3B8)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickActionButton(
                label = "New Project",
                icon = Icons.Default.Add,
                color = Color(0xFF38BDF8),
                testTag = "quick_action_new_project",
                onClick = { showCreateDialog = true },
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                label = "Open Project",
                icon = Icons.Default.FolderOpen,
                color = Color(0xFFFBBF24),
                testTag = "quick_action_open_project",
                onClick = onMyProjects,
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                label = "Quick Editor",
                icon = Icons.Default.Code,
                color = Color(0xFFA78BFA),
                testTag = "quick_action_quick_editor",
                onClick = onQuickEditor,
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                label = "Live Preview",
                icon = Icons.Default.PlayArrow,
                color = Color(0xFF34D399),
                testTag = "quick_action_live_preview",
                onClick = onLivePreview,
                modifier = Modifier.weight(1f)
            )
        }

        // -------------------------------------------------------------
        // 3. RECENT PROJECTS (Compact Rows)
        // -------------------------------------------------------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Projects",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8)
            )
            TextButton(onClick = onMyProjects) {
                Text("View All (${recentProjects.size})", fontSize = 12.sp, color = Color(0xFF38BDF8))
            }
        }

        if (recentProjects.isEmpty()) {
            Surface(
                color = Color(0xFF1E293B),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "No projects yet. Create your first project using the button above!",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                recentProjects.take(4).forEach { proj ->
                    CompactProjectRow(
                        project = proj,
                        onOpen = { onOpenProject(proj) },
                        onRename = {
                            projectToRename = proj
                            renameInput = proj.name
                        },
                        onDuplicate = { onDuplicateProject?.invoke(proj) },
                        onDelete = { onDeleteProject?.invoke(proj.id) }
                    )
                }
            }
        }

        // -------------------------------------------------------------
        // 4. STARTER TEMPLATES
        // -------------------------------------------------------------
        Text(
            text = "Start from Template",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF94A3B8)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(TemplateRepository.templates) { template ->
                TemplateCard(
                    template = template,
                    onClick = { onSelectTemplate(template) }
                )
            }
        }

        // -------------------------------------------------------------
        // 5. DEVELOPER TOOLS SHORTCUTS (Organized Sections)
        // -------------------------------------------------------------
        Text(
            text = "Essential Developer Tools",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF94A3B8)
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ToolTile(
                title = "GET SOURCE CODE",
                subtitle = "Inspect raw HTTP & DOM",
                icon = Icons.Default.Source,
                accentColor = Color(0xFFF97316),
                testTag = "home_tool_source_code",
                onClick = onSourceCode,
                modifier = Modifier.weight(1f)
            )
            ToolTile(
                title = "HTML → APK",
                subtitle = "Package signed Android app",
                icon = Icons.Default.Android,
                accentColor = Color(0xFF10B981),
                testTag = "home_tool_apk",
                onClick = onHtmlToApk,
                modifier = Modifier.weight(1f)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ToolTile(
                title = "GIT REPOSITORIES",
                subtitle = "Clone, pull & commit",
                icon = Icons.Default.Commit,
                accentColor = Color(0xFF818CF8),
                testTag = "home_tool_git",
                onClick = onGitProjects,
                modifier = Modifier.weight(1f)
            )
            ToolTile(
                title = "AI ASSISTANT",
                subtitle = "Code generation & debugging",
                icon = Icons.Default.AutoAwesome,
                accentColor = Color(0xFFFBBF24),
                testTag = "home_tool_ai",
                onClick = onAiAssistant,
                modifier = Modifier.weight(1f)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ToolTile(
                title = "WEB TUTORIALS",
                subtitle = "HTML, CSS & JS lessons",
                icon = Icons.Default.School,
                accentColor = Color(0xFF38BDF8),
                testTag = "home_tool_learn",
                onClick = onHtmlTutorials,
                modifier = Modifier.weight(1f)
            )
            ToolTile(
                title = "COLOR LAB",
                subtitle = "Palettes & contrast check",
                icon = Icons.Default.Palette,
                accentColor = Color(0xFFEC4899),
                testTag = "home_tool_colors",
                onClick = onColorLab,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(16.dp))
    }

    // Create New Project Dialog
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create New Project", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newProjectName,
                    onValueChange = { newProjectName = it },
                    label = { Text("Project Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = newProjectName.trim().ifEmpty { "My Web App" }
                        onCreateProject?.invoke(name, null)
                        showCreateDialog = false
                        newProjectName = ""
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Rename Project Dialog
    if (projectToRename != null) {
        AlertDialog(
            onDismissRequest = { projectToRename = null },
            title = { Text("Rename Project", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = renameInput,
                    onValueChange = { renameInput = it },
                    label = { Text("New Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newName = renameInput.trim()
                        if (newName.isNotEmpty()) {
                            onRenameProject?.invoke(projectToRename!!, newName)
                        }
                        projectToRename = null
                    }
                ) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { projectToRename = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun QuickActionButton(
    label: String,
    icon: ImageVector,
    color: Color,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier.testTag(testTag),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun CompactProjectRow(
    project: Project,
    onOpen: () -> Unit,
    onRename: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("project_row_${project.id}"),
        onClick = onOpen
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(getProjectTypeColor(project.type).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    getProjectTypeIcon(project.type),
                    null,
                    tint = getProjectTypeColor(project.type),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = project.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = project.type.name.lowercase().replaceFirstChar { it.uppercase() },
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Text(" • ", color = Color.Gray, fontSize = 11.sp)
                    Text(
                        text = formatRelativeTime(project.updatedAt),
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Button(
                onClick = onOpen,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text("Open", fontSize = 11.sp)
            }

            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.MoreVert, null, tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.background(Color(0xFF1E293B))
                ) {
                    DropdownMenuItem(
                        text = { Text("Rename", color = Color.White, fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Edit, null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp)) },
                        onClick = {
                            menuExpanded = false
                            onRename()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Duplicate", color = Color.White, fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.ContentCopy, null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp)) },
                        onClick = {
                            menuExpanded = false
                            onDuplicate()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = Color(0xFFEF4444), fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp)) },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TemplateCard(
    template: TemplateProject,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .width(180.dp)
            .testTag("template_${template.title.lowercase().replace(" ", "_")}"),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(template.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp, maxLines = 1)
            Spacer(Modifier.height(4.dp))
            Text(template.description, color = Color(0xFF94A3B8), fontSize = 10.sp, maxLines = 2, lineHeight = 14.sp)
            Spacer(Modifier.height(8.dp))
            Surface(
                color = Color(0xFF0F172A),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "USE TEMPLATE",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF38BDF8),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun ToolTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier.testTag(testTag),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = accentColor, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp, maxLines = 1)
                Text(subtitle, color = Color(0xFF94A3B8), fontSize = 10.sp, maxLines = 1)
            }
        }
    }
}

private fun getProjectTypeIcon(type: ProjectType): ImageVector {
    return when (type) {
        ProjectType.GAME -> Icons.Default.SportsEsports
        ProjectType.CANVAS -> Icons.Default.Palette
        ProjectType.APP -> Icons.Default.Android
        else -> Icons.Default.Language
    }
}

private fun getProjectTypeColor(type: ProjectType): Color {
    return when (type) {
        ProjectType.GAME -> Color(0xFFE879F9)
        ProjectType.CANVAS -> Color(0xFFFBBF24)
        ProjectType.APP -> Color(0xFF10B981)
        else -> Color(0xFF38BDF8)
    }
}

private fun formatRelativeTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> "${diff / 3600_000}h ago"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestamp))
    }
}
