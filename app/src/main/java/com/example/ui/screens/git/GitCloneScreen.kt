package com.example.ui.screens.git

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.git.DetectedProjectType
import com.example.data.git.GitCredentials
import com.example.data.git.GitProgressUpdate
import com.example.model.Project

@Composable
fun GitCloneScreen(
    existingProjects: List<Project>,
    progress: GitProgressUpdate?,
    detectedType: DetectedProjectType?,
    clonedProject: Project?,
    onCloneRequest: (repoUrl: String, projectName: String, credentials: GitCredentials?) -> Unit,
    onOpenProject: (Project) -> Unit,
    onRunProjectPreview: (Project) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var repoUrl by remember { mutableStateOf("") }
    var projectName by remember { mutableStateOf("") }
    var showAuthDialog by remember { mutableStateOf(false) }
    var authUsername by remember { mutableStateOf("") }
    var authPassword by remember { mutableStateOf("") }
    var showCollisionDialog by remember { mutableStateOf(false) }
    var pendingCloneName by remember { mutableStateOf("") }

    val isCloning = progress != null && !progress.isCompleted && progress.error == null

    // Popular sample web repositories for quick 1-click test
    val presets = listOf(
        "Canvas 2D Game Demo" to "https://github.com/mdn/canvas-raycaster.git",
        "HTML5 Flappy Bird" to "https://github.com/nebez/floppybird.git",
        "Responsive Portfolio" to "https://github.com/BlackrockDigital/startbootstrap-creative.git"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("git_clone_back_btn")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    text = "Git Clone Project",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Import real repository from GitHub or any Git remote",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }

        // Clone Form Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Repository URL",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = Color.White
                )

                OutlinedTextField(
                    value = repoUrl,
                    onValueChange = {
                        repoUrl = it
                        // Auto-populate project name if empty
                        if (projectName.isBlank()) {
                            val candidate = it.removeSuffix(".git").removeSuffix("/").substringAfterLast("/")
                            if (candidate.isNotBlank() && candidate != "http:" && candidate != "https:") {
                                projectName = candidate
                            }
                        }
                    },
                    placeholder = { Text("https://github.com/user/project.git", color = Color(0xFF64748B), fontSize = 13.sp) },
                    singleLine = true,
                    enabled = !isCloning,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedContainerColor = Color(0xFF0F172A),
                        unfocusedContainerColor = Color(0xFF0F172A)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("git_repo_url_input")
                )

                Text(
                    text = "Supports standard HTTPS URLs: https://github.com/user/project.git or https://github.com/user/project (auto-normalized).",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "Project Name",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = Color.White
                )

                OutlinedTextField(
                    value = projectName,
                    onValueChange = { projectName = it },
                    placeholder = { Text("MyWebsite", color = Color(0xFF64748B), fontSize = 13.sp) },
                    singleLine = true,
                    enabled = !isCloning,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedContainerColor = Color(0xFF0F172A),
                        unfocusedContainerColor = Color(0xFF0F172A)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("git_project_name_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Folder, null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Destination: HTML Live Projects",
                            fontSize = 12.sp,
                            color = Color(0xFFCBD5E1)
                        )
                    }

                    TextButton(
                        onClick = { showAuthDialog = true },
                        enabled = !isCloning
                    ) {
                        Icon(Icons.Default.Lock, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Private Repo Auth", fontSize = 11.sp, color = Color(0xFFF59E0B))
                    }
                }

                Button(
                    onClick = {
                        val trimmedUrl = repoUrl.trim()
                        val finalName = projectName.trim().ifEmpty {
                            trimmedUrl.removeSuffix(".git").removeSuffix("/").substringAfterLast("/")
                        }.ifEmpty { "ClonedProject" }

                        // Check collision
                        val exists = existingProjects.any { it.name.equals(finalName, ignoreCase = true) }
                        if (exists) {
                            pendingCloneName = finalName
                            showCollisionDialog = true
                        } else {
                            val creds = if (authUsername.isNotBlank() || authPassword.isNotBlank()) {
                                GitCredentials(authUsername, authPassword)
                            } else null
                            onCloneRequest(trimmedUrl, finalName, creds)
                        }
                    },
                    enabled = repoUrl.isNotBlank() && !isCloning,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth().height(46.dp).testTag("git_clone_submit_btn")
                ) {
                    if (isCloning) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(10.dp))
                        Text("Cloning Repository...", fontSize = 14.sp)
                    } else {
                        Icon(Icons.Default.Code, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Clone Project", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Live Real Progress Panel (Requirements 42 & 60)
        AnimatedVisibility(visible = progress != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161F30)),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isCloning) {
                                CircularProgressIndicator(color = Color(0xFF38BDF8), modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else if (progress?.isCompleted == true) {
                                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                            } else if (progress?.error != null) {
                                Icon(Icons.Default.ErrorOutline, null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = progress?.stage ?: "Operation",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }

                        if (progress != null && progress.total > 0) {
                            Text(
                                text = "${(progress.percentage * 100).toInt()}%",
                                color = Color(0xFF38BDF8),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    if (progress != null) {
                        LinearProgressIndicator(
                            progress = { progress.percentage.coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = if (progress.error != null) Color(0xFFEF4444) else Color(0xFF38BDF8),
                            trackColor = Color(0xFF334155),
                        )

                        Text(
                            text = progress.statusMessage,
                            fontSize = 12.sp,
                            color = if (progress.error != null) Color(0xFFFCA5A5) else Color(0xFFCBD5E1),
                            fontFamily = FontFamily.Monospace
                        )

                        if (progress.error != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        val creds = if (authUsername.isNotBlank() || authPassword.isNotBlank()) {
                                            GitCredentials(authUsername, authPassword)
                                        } else null
                                        onCloneRequest(repoUrl, projectName, creds)
                                    }
                                ) {
                                    Icon(Icons.Default.Refresh, null, tint = Color(0xFF38BDF8), modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Retry Clone", fontSize = 11.sp, color = Color(0xFF38BDF8))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Cloned Project Detection & Actions (Requirements 55, 56, 57)
        if (clonedProject != null && detectedType != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF064E3B)),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF34D399), modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Project Imported Successfully!", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                    }

                    Surface(
                        color = Color(0xFF022C22),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Detected project type:", fontSize = 11.sp, color = Color(0xFF6EE7B7))
                            Text(detectedType.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(Modifier.height(4.dp))
                            Text(detectedType.recommendation, fontSize = 12.sp, color = Color(0xFFD1FAE5))
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (detectedType.canRunDirectly) {
                            Button(
                                onClick = { onRunProjectPreview(clonedProject) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                modifier = Modifier.weight(1f).testTag("git_run_preview_btn")
                            ) {
                                Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Run Website", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = { onOpenProject(clonedProject) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            modifier = Modifier.weight(1f).testTag("git_open_project_btn")
                        ) {
                            Icon(Icons.Default.Code, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Open Code", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Quick Preset Repositories
        Text("Try Sample GitHub Repositories", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
        presets.forEach { (name, url) ->
            Surface(
                onClick = {
                    repoUrl = url
                    projectName = url.removeSuffix(".git").substringAfterLast("/")
                },
                color = Color(0xFF1E293B),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(name, fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 13.sp)
                        Text(url, fontSize = 11.sp, color = Color(0xFF38BDF8), fontFamily = FontFamily.Monospace, maxLines = 1)
                    }
                    Icon(Icons.Default.Code, null, tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                }
            }
        }
    }

    // Name Collision Dialog (Requirement 59)
    if (showCollisionDialog) {
        AlertDialog(
            onDismissRequest = { showCollisionDialog = false },
            title = { Text("Project Already Exists") },
            text = {
                Text("A project named '$pendingCloneName' already exists in HTML Live. What would you like to do?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCollisionDialog = false
                        val copyName = "${pendingCloneName}_copy_${System.currentTimeMillis() % 1000}"
                        projectName = copyName
                        val creds = if (authUsername.isNotBlank() || authPassword.isNotBlank()) {
                            GitCredentials(authUsername, authPassword)
                        } else null
                        onCloneRequest(repoUrl, copyName, creds)
                    }
                ) {
                    Text("Clone Anyway as Copy")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCollisionDialog = false }) {
                    Text("Use Different Name")
                }
            }
        )
    }

    // Private Repository Authentication Dialog (Requirement 53)
    if (showAuthDialog) {
        AlertDialog(
            onDismissRequest = { showAuthDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Git Authentication")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "For private repositories, provide your GitHub Personal Access Token (PAT). Credentials are stored securely and never written to project files.",
                        fontSize = 12.sp,
                        color = Color(0xFFCBD5E1)
                    )
                    OutlinedTextField(
                        value = authUsername,
                        onValueChange = { authUsername = it },
                        label = { Text("Username") },
                        placeholder = { Text("e.g. octocat") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = authPassword,
                        onValueChange = { authPassword = it },
                        label = { Text("Personal Access Token (PAT)") },
                        placeholder = { Text("ghp_••••••••••••") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showAuthDialog = false }) {
                    Text("Save Credentials")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAuthDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
