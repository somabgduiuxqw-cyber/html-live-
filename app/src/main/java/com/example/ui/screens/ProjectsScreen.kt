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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

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

    Column(
        modifier = modifier
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Folder, null, tint = Color(0xFF38BDF8), modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Projects Manager", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Text("${projects.size} local projects • Offline & autosaved", fontSize = 12.sp, color = Color(0xFF94A3B8))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = { zipPickerLauncher.launch("application/zip") }, modifier = Modifier.testTag("import_zip_btn")) {
                    Icon(Icons.Default.FileOpen, contentDescription = "Import ZIP", tint = Color(0xFF38BDF8))
                }
                Button(
                    onClick = { showNewProjectDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8), contentColor = Color(0xFF0F172A)),
                    modifier = Modifier.testTag("new_project_btn")
                ) {
                    Icon(Icons.Default.Add, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("New", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Projects List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(projects, key = { it.id }) { project ->
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
                        if (file != null) {
                            shareExportedZip(file)
                        } else {
                            Toast.makeText(context, "Exporting...", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onSnapshot = {
                        onCreateSnapshot(project.id)
                        Toast.makeText(context, "Snapshot saved!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    // New Project Dialog (Templates or Blank)
    if (showNewProjectDialog) {
        var projectName by remember { mutableStateOf("My Website") }
        var selectedTemplate by remember { mutableStateOf<TemplateProject?>(null) }

        AlertDialog(
            onDismissRequest = { showNewProjectDialog = false },
            title = { Text("Create New Project") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = projectName,
                        onValueChange = { projectName = it },
                        label = { Text("Project Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("new_project_name_input")
                    )

                    Text("Choose Starter Template", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        TemplateRepository.templates.take(5).forEach { tmpl ->
                            Surface(
                                onClick = {
                                    selectedTemplate = tmpl
                                    projectName = tmpl.title
                                },
                                color = if (selectedTemplate == tmpl) Color(0xFF0284C7) else Color(0xFF1E293B),
                                shape = MaterialTheme.shapes.small,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(tmpl.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                        Text(tmpl.type.name, fontSize = 10.sp, color = Color(0xFF38BDF8))
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (projectName.isNotBlank()) {
                            onCreateProject(projectName, selectedTemplate)
                            showNewProjectDialog = false
                        }
                    },
                    modifier = Modifier.testTag("confirm_create_project_btn")
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewProjectDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Rename Dialog
    if (projectToRename != null) {
        AlertDialog(
            onDismissRequest = { projectToRename = null },
            title = { Text("Rename Project") },
            text = {
                OutlinedTextField(
                    value = renameInput,
                    onValueChange = { renameInput = it },
                    singleLine = true,
                    label = { Text("New Name") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (renameInput.isNotBlank()) {
                        onRenameProject(projectToRename!!, renameInput)
                        projectToRename = null
                    }
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { projectToRename = null }) { Text("Cancel") }
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
    onSnapshot: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val dateStr = remember(project.updatedAt) {
        SimpleDateFormat("MMM d, yyyy • HH:mm", Locale.getDefault()).format(Date(project.updatedAt))
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) Color(0xFF1E293B) else Color(0xFF161F30)
        ),
        border = if (isActive) androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF38BDF8)) else null,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("project_item_${project.id}"),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(project.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                    if (isActive) {
                        Spacer(Modifier.width(6.dp))
                        Surface(color = Color(0xFF10B981), shape = MaterialTheme.shapes.extraSmall) {
                            Text("ACTIVE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A), modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(project.description.ifEmpty { "HTML/CSS/JavaScript Project" }, fontSize = 12.sp, color = Color(0xFF94A3B8), maxLines = 1)
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(project.type.name, fontSize = 10.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.width(8.dp))
                    Text(dateStr, fontSize = 10.sp, color = Color(0xFF64748B))
                }
            }

            Box {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.testTag("project_menu_${project.id}")) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = Color.LightGray)
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Rename") },
                        onClick = { showMenu = false; onRename() },
                        leadingIcon = { Icon(Icons.Default.Edit, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Duplicate") },
                        onClick = { showMenu = false; onDuplicate() },
                        leadingIcon = { Icon(Icons.Default.ContentCopy, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Save Snapshot (Backup)") },
                        onClick = { showMenu = false; onSnapshot() },
                        leadingIcon = { Icon(Icons.Default.History, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Export ZIP Archive") },
                        onClick = { showMenu = false; onExport() },
                        leadingIcon = { Icon(Icons.Default.Share, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = Color(0xFFEF4444)) },
                        onClick = { showMenu = false; onDelete() },
                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color(0xFFEF4444)) }
                    )
                }
            }
        }
    }
}
