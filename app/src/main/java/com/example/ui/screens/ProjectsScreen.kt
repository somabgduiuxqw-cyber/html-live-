package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Commit
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.ViewList
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.repository.TemplateProject
import com.example.data.repository.TemplateRepository
import com.example.model.Project
import com.example.model.ProjectType
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ProjectSortOrder {
    RECENT,
    NAME_ASC,
    NAME_DESC
}

@Composable
fun ProjectsScreen(
    projects: List<Project>,
    activeProjectId: String,
    onSelectProject: (Project) -> Unit,
    onCreateProject: (name: String, template: TemplateProject?) -> Unit,
    onRenameProject: (project: Project, newName: String) -> Unit,
    onDeleteProject: (projectId: String) -> Unit,
    onDuplicateProject: (project: Project) -> Unit,
    onExportZip: (projectId: String) -> File?,
    onImportZip: (name: String, bytes: ByteArray) -> Unit,
    onCreateSnapshot: (projectId: String) -> Unit,
    onPackageApk: ((Project) -> Unit)? = null,
    onOpenGit: ((Project) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf<ProjectType?>(null) }
    var sortOrder by remember { mutableStateOf(ProjectSortOrder.RECENT) }
    var isGridView by remember { mutableStateOf(false) }

    var showNewProjectDialog by remember { mutableStateOf(false) }
    var projectToRename by remember { mutableStateOf<Project?>(null) }
    var renameInput by remember { mutableStateOf("") }

    // Launcher for ZIP import
    val zipPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                try {
                    val bytes = context.contentResolver.openInputStream(it)?.readBytes()
                    if (bytes != null) {
                        val name = it.lastPathSegment?.substringAfterLast('/')?.removeSuffix(".zip") ?: "Imported Project"
                        onImportZip(name, bytes)
                        Toast.makeText(context, "Project imported successfully!", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Import failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun shareExportedZip(file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/zip"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Export Project ZIP"))
    }

    // Filter & Sort
    val filteredProjects = remember(projects, searchQuery, selectedTypeFilter, sortOrder) {
        var list = projects
        if (searchQuery.isNotBlank()) {
            list = list.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.description.contains(searchQuery, ignoreCase = true)
            }
        }
        if (selectedTypeFilter != null) {
            list = list.filter { it.type == selectedTypeFilter }
        }
        when (sortOrder) {
            ProjectSortOrder.RECENT -> list.sortedByDescending { it.updatedAt }
            ProjectSortOrder.NAME_ASC -> list.sortedBy { it.name.lowercase() }
            ProjectSortOrder.NAME_DESC -> list.sortedByDescending { it.name.lowercase() }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(16.dp)
    ) {
        // Top Toolbar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Folder, null, tint = Color(0xFF38BDF8), modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Projects Manager", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Text("${filteredProjects.size} of ${projects.size} projects", fontSize = 11.sp, color = Color(0xFF94A3B8))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                // View toggle
                IconButton(
                    onClick = { isGridView = !isGridView },
                    modifier = Modifier.size(36.dp).testTag("projects_toggle_view_btn")
                ) {
                    Icon(
                        if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                        contentDescription = "Toggle Grid/List",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Import ZIP
                IconButton(
                    onClick = { zipPickerLauncher.launch("application/zip") },
                    modifier = Modifier.size(36.dp).testTag("import_zip_btn")
                ) {
                    Icon(Icons.Default.FileOpen, contentDescription = "Import ZIP", tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
                }

                // New Project
                Button(
                    onClick = { showNewProjectDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp).testTag("new_project_btn")
                ) {
                    Icon(Icons.Default.Add, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("New", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search projects...", fontSize = 12.sp, color = Color(0xFF64748B)) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp)) },
            trailingIcon = if (searchQuery.isNotEmpty()) {
                {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, null, tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                    }
                }
            } else null,
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF1E293B),
                unfocusedContainerColor = Color(0xFF1E293B),
                focusedBorderColor = Color(0xFF38BDF8),
                unfocusedBorderColor = Color(0xFF334155),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("projects_search_input")
        )

        Spacer(Modifier.height(10.dp))

        // Filter and Sort Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = selectedTypeFilter == null,
                onClick = { selectedTypeFilter = null },
                label = { Text("All", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF0284C7),
                    selectedLabelColor = Color.White,
                    containerColor = Color(0xFF1E293B),
                    labelColor = Color(0xFF94A3B8)
                )
            )
            FilterChip(
                selected = selectedTypeFilter == ProjectType.WEBSITE,
                onClick = { selectedTypeFilter = if (selectedTypeFilter == ProjectType.WEBSITE) null else ProjectType.WEBSITE },
                label = { Text("Websites", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF0284C7),
                    selectedLabelColor = Color.White,
                    containerColor = Color(0xFF1E293B),
                    labelColor = Color(0xFF94A3B8)
                )
            )
            FilterChip(
                selected = selectedTypeFilter == ProjectType.GAME,
                onClick = { selectedTypeFilter = if (selectedTypeFilter == ProjectType.GAME) null else ProjectType.GAME },
                label = { Text("Games", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF0284C7),
                    selectedLabelColor = Color.White,
                    containerColor = Color(0xFF1E293B),
                    labelColor = Color(0xFF94A3B8)
                )
            )
            Spacer(Modifier.weight(1f))
            // Sort toggle
            IconButton(
                onClick = {
                    sortOrder = when (sortOrder) {
                        ProjectSortOrder.RECENT -> ProjectSortOrder.NAME_ASC
                        ProjectSortOrder.NAME_ASC -> ProjectSortOrder.NAME_DESC
                        ProjectSortOrder.NAME_DESC -> ProjectSortOrder.RECENT
                    }
                },
                modifier = Modifier.size(32.dp).testTag("projects_sort_btn")
            ) {
                Icon(
                    Icons.Default.Sort,
                    contentDescription = "Sort (${sortOrder.name})",
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        // Projects List / Grid
        if (filteredProjects.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (searchQuery.isNotEmpty()) "No matching projects found." else "No projects yet.",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
            }
        } else if (isGridView) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredProjects, key = { it.id }) { project ->
                    ProjectGridCard(
                        project = project,
                        isActive = project.id == activeProjectId,
                        onClick = { onSelectProject(project) },
                        onRename = {
                            projectToRename = project
                            renameInput = project.name
                        },
                        onDuplicate = { onDuplicateProject(project) },
                        onDelete = { onDeleteProject(project.id) },
                        onExport = {
                            val file = onExportZip(project.id)
                            if (file != null) shareExportedZip(file)
                        },
                        onPackageApk = { onPackageApk?.invoke(project) },
                        onOpenGit = { onOpenGit?.invoke(project) }
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredProjects, key = { it.id }) { project ->
                    ProjectItemCard(
                        project = project,
                        isActive = project.id == activeProjectId,
                        onClick = { onSelectProject(project) },
                        onRename = {
                            projectToRename = project
                            renameInput = project.name
                        },
                        onDuplicate = { onDuplicateProject(project) },
                        onDelete = { onDeleteProject(project.id) },
                        onExport = {
                            val file = onExportZip(project.id)
                            if (file != null) shareExportedZip(file)
                        },
                        onSnapshot = {
                            onCreateSnapshot(project.id)
                            Toast.makeText(context, "Snapshot saved!", Toast.LENGTH_SHORT).show()
                        },
                        onPackageApk = { onPackageApk?.invoke(project) },
                        onOpenGit = { onOpenGit?.invoke(project) }
                    )
                }
            }
        }
    }

    // New Project Dialog
    if (showNewProjectDialog) {
        var projectName by remember { mutableStateOf("My Website") }
        var selectedTemplate by remember { mutableStateOf<TemplateProject?>(null) }

        AlertDialog(
            onDismissRequest = { showNewProjectDialog = false },
            title = { Text("Create New Project", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = projectName,
                        onValueChange = { projectName = it },
                        label = { Text("Project Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("new_project_name_input")
                    )

                    Text("Choose Starter Template:", fontSize = 12.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            color = if (selectedTemplate == null) Color(0xFF0369A1) else Color(0xFF1E293B),
                            shape = RoundedCornerShape(6.dp),
                            onClick = { selectedTemplate = null },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("Blank HTML5 Project", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        TemplateRepository.templates.forEach { tmpl ->
                            Surface(
                                color = if (selectedTemplate == tmpl) Color(0xFF0369A1) else Color(0xFF1E293B),
                                shape = RoundedCornerShape(6.dp),
                                onClick = { selectedTemplate = tmpl },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(tmpl.title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = projectName.trim().ifEmpty { "New Project" }
                        onCreateProject(name, selectedTemplate)
                        showNewProjectDialog = false
                    },
                    modifier = Modifier.testTag("dialog_create_project_confirm")
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewProjectDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Rename Dialog
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
                            onRenameProject(projectToRename!!, newName)
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
private fun ProjectItemCard(
    project: Project,
    isActive: Boolean,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onExport: () -> Unit,
    onSnapshot: () -> Unit,
    onPackageApk: () -> Unit,
    onOpenGit: (() -> Unit)? = null
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) Color(0xFF1E293B) else Color(0xFF131C2E)
        ),
        shape = MaterialTheme.shapes.medium,
        border = if (isActive) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8)) else null,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("project_item_${project.id}"),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(getProjectTypeColor(project.type).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    getProjectTypeIcon(project.type),
                    null,
                    tint = getProjectTypeColor(project.type),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        project.name,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    if (isActive) {
                        Spacer(Modifier.width(6.dp))
                        Surface(
                            color = Color(0xFF38BDF8),
                            shape = MaterialTheme.shapes.extraSmall
                        ) {
                            Text(
                                "ACTIVE",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        project.type.name.lowercase().replaceFirstChar { it.uppercase() },
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                    Text(" • ", color = Color.Gray, fontSize = 11.sp)
                    Text(
                        formatRelativeTime(project.updatedAt),
                        color = Color(0xFF64748B),
                        fontSize = 11.sp
                    )
                }
            }

            Box {
                IconButton(onClick = { menuExpanded = true }, modifier = Modifier.testTag("project_menu_${project.id}")) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = Color.Gray)
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.background(Color(0xFF1E293B))
                ) {
                    DropdownMenuItem(
                        text = { Text("Open in Editor", color = Color.White, fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Folder, null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp)) },
                        onClick = {
                            menuExpanded = false
                            onClick()
                        }
                    )
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
                        text = { Text("Export ZIP", color = Color.White, fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Archive, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp)) },
                        onClick = {
                            menuExpanded = false
                            onExport()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Package APK", color = Color.White, fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Android, null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp)) },
                        onClick = {
                            menuExpanded = false
                            onPackageApk()
                        }
                    )
                    if (onOpenGit != null) {
                        DropdownMenuItem(
                            text = { Text("Git Repository", color = Color.White, fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Commit, null, tint = Color(0xFFA78BFA), modifier = Modifier.size(16.dp)) },
                            onClick = {
                                menuExpanded = false
                                onOpenGit()
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Save Snapshot", color = Color.White, fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.History, null, tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp)) },
                        onClick = {
                            menuExpanded = false
                            onSnapshot()
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
private fun ProjectGridCard(
    project: Project,
    isActive: Boolean,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onExport: () -> Unit,
    onPackageApk: () -> Unit,
    onOpenGit: (() -> Unit)? = null
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = if (isActive) Color(0xFF1E293B) else Color(0xFF131C2E)),
        shape = RoundedCornerShape(10.dp),
        border = if (isActive) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8)) else null,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("project_grid_${project.id}"),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
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

                Box {
                    IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.MoreVert, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier.background(Color(0xFF1E293B))
                    ) {
                        DropdownMenuItem(
                            text = { Text("Open", color = Color.White, fontSize = 12.sp) },
                            onClick = { menuExpanded = false; onClick() }
                        )
                        DropdownMenuItem(
                            text = { Text("Rename", color = Color.White, fontSize = 12.sp) },
                            onClick = { menuExpanded = false; onRename() }
                        )
                        DropdownMenuItem(
                            text = { Text("Duplicate", color = Color.White, fontSize = 12.sp) },
                            onClick = { menuExpanded = false; onDuplicate() }
                        )
                        DropdownMenuItem(
                            text = { Text("Export ZIP", color = Color.White, fontSize = 12.sp) },
                            onClick = { menuExpanded = false; onExport() }
                        )
                        DropdownMenuItem(
                            text = { Text("Package APK", color = Color.White, fontSize = 12.sp) },
                            onClick = { menuExpanded = false; onPackageApk() }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = Color(0xFFEF4444), fontSize = 12.sp) },
                            onClick = { menuExpanded = false; onDelete() }
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = project.name,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 13.sp,
                maxLines = 1
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = formatRelativeTime(project.updatedAt),
                color = Color(0xFF64748B),
                fontSize = 10.sp
            )
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
        else -> SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(timestamp))
    }
}
