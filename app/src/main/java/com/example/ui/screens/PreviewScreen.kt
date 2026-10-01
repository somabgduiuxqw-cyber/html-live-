package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Source
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ConsoleLevel
import com.example.model.ConsoleMessage
import com.example.model.Project
import com.example.model.ProjectFile
import com.example.ui.components.DeveloperConsoleView
import com.example.ui.components.LivePreviewView

@Composable
fun PreviewScreen(
    project: Project?,
    files: List<ProjectFile>,
    consoleMessages: List<ConsoleMessage>,
    onConsoleMessage: (ConsoleMessage) -> Unit,
    onClearConsole: () -> Unit,
    onOpenEditor: () -> Unit,
    onOpenProjects: () -> Unit,
    onInspectSource: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFullscreen by remember { mutableStateOf(false) }
    var showConsoleDrawer by remember { mutableStateOf(false) }

    val errorCount = consoleMessages.count { it.level == ConsoleLevel.ERROR }

    BackHandler(enabled = isFullscreen) {
        isFullscreen = false
    }

    if (project == null || files.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    Icons.Default.Visibility,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    text = "No Project Loaded for Preview",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Select an existing project or create a new one to run and preview HTML, CSS & JavaScript in real time.",
                    fontSize = 13.sp,
                    color = Color(0xFF94A3B8),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 18.sp
                )
                Button(
                    onClick = onOpenProjects,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    modifier = Modifier.testTag("preview_open_projects_btn")
                ) {
                    Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Browse Projects")
                }
            }
        }
        return
    }

    if (isFullscreen) {
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
                onToggleFullscreen = { isFullscreen = false },
                onOpenSourceViewer = onInspectSource,
                modifier = Modifier.fillMaxSize()
            )
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A))
        ) {
            // Preview Top Bar
            Surface(
                color = Color(0xFF1E293B),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = project.name,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp,
                            maxLines = 1
                        )
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            color = Color(0xFF10B981).copy(alpha = 0.2f),
                            shape = MaterialTheme.shapes.extraSmall
                        ) {
                            Text(
                                "LIVE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF34D399),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        // Open in Editor
                        IconButton(
                            onClick = onOpenEditor,
                            modifier = Modifier.size(32.dp).testTag("preview_to_editor_btn")
                        ) {
                            Icon(
                                Icons.Default.Code,
                                contentDescription = "Open in Editor",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Inspect Source
                        IconButton(
                            onClick = onInspectSource,
                            modifier = Modifier.size(32.dp).testTag("preview_inspect_source_btn")
                        ) {
                            Icon(
                                Icons.Default.Source,
                                contentDescription = "Inspect Source",
                                tint = Color(0xFFF97316),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Console Drawer
                        IconButton(
                            onClick = { showConsoleDrawer = !showConsoleDrawer },
                            modifier = Modifier.size(32.dp).testTag("preview_console_btn")
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

                        // Fullscreen
                        IconButton(
                            onClick = { isFullscreen = true },
                            modifier = Modifier.size(32.dp).testTag("preview_fullscreen_btn")
                        ) {
                            Icon(
                                Icons.Default.Fullscreen,
                                contentDescription = "Fullscreen",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Main Preview Area
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                LivePreviewView(
                    files = files,
                    consoleMessages = consoleMessages,
                    onConsoleMessage = onConsoleMessage,
                    onOpenConsole = { showConsoleDrawer = true },
                    isFullscreen = false,
                    onToggleFullscreen = { isFullscreen = true },
                    onOpenSourceViewer = onInspectSource,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Console Drawer overlay if opened
            if (showConsoleDrawer) {
                Surface(
                    color = Color(0xFF1E293B),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                ) {
                    DeveloperConsoleView(
                        messages = consoleMessages,
                        onClear = onClearConsole,
                        onClose = { showConsoleDrawer = false },
                        onFixWithAi = {},
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
