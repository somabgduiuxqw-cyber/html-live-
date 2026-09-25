package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.ConsoleLevel
import com.example.ui.components.ColorLabView
import com.example.ui.components.GameStudioView
import com.example.ui.components.WebsiteBuilderView
import com.example.ui.screens.AiAssistantScreen
import com.example.ui.screens.EditorScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LearningScreen
import com.example.ui.screens.ProjectsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.HtmlLiveTheme
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.NavDestination

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HtmlLiveTheme {
                MainAppContainer()
            }
        }
    }
}

@Composable
fun MainAppContainer(viewModel: MainViewModel = viewModel()) {
    val currentNav by viewModel.currentNav.collectAsState()
    val activeProject by viewModel.activeProject.collectAsState()
    val activeFiles by viewModel.activeFiles.collectAsState()
    val currentFileName by viewModel.currentFileName.collectAsState()
    val consoleMessages by viewModel.consoleMessages.collectAsState()
    val aiSettings by viewModel.aiSettings.collectAsState()
    val editorSettings by viewModel.editorSettings.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    val allProjects by viewModel.allProjects.collectAsState()

    // Handle back button across secondary screens
    BackHandler(enabled = currentNav != NavDestination.HOME) {
        viewModel.handleBack()
    }

    val errorCount = consoleMessages.count { it.level == ConsoleLevel.ERROR }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(Color(0xFF0F172A))) {
        val isWideScreen = maxWidth > 680.dp

        if (isWideScreen) {
            // Tablet / Desktop layout with Navigation Rail
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    containerColor = Color(0xFF1E293B),
                    contentColor = Color.White,
                    modifier = Modifier.fillMaxHeight().width(76.dp)
                ) {
                    Spacer(Modifier.height(16.dp))
                    NavDestination.values().forEach { destination ->
                        val isSelected = currentNav == destination
                        NavigationRailItem(
                            selected = isSelected,
                            onClick = { viewModel.navigateTo(destination) },
                            icon = {
                                NavIconWithBadge(destination, errorCount)
                            },
                            label = { Text(destination.title, fontSize = 9.sp, maxLines = 1) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = Color(0xFF0F172A),
                                selectedTextColor = Color(0xFF38BDF8),
                                indicatorColor = Color(0xFF38BDF8),
                                unselectedIconColor = Color(0xFF94A3B8),
                                unselectedTextColor = Color(0xFF94A3B8)
                            ),
                            modifier = Modifier.testTag("nav_rail_${destination.name.lowercase()}")
                        )
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    ScreenContent(
                        destination = currentNav,
                        viewModel = viewModel,
                        activeProject = activeProject,
                        activeFiles = activeFiles,
                        currentFileName = currentFileName,
                        consoleMessages = consoleMessages,
                        aiSettings = aiSettings,
                        editorSettings = editorSettings,
                        chatMessages = chatMessages,
                        isAiLoading = isAiLoading,
                        allProjects = allProjects
                    )
                }
            }
        } else {
            // Mobile layout with clean Bottom Navigation Bar
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color(0xFF0F172A),
                bottomBar = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E293B))
                            .horizontalScroll(rememberScrollState())
                    ) {
                        NavDestination.values().forEach { destination ->
                            val isSelected = currentNav == destination
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { viewModel.navigateTo(destination) },
                                icon = {
                                    NavIconWithBadge(destination, errorCount)
                                },
                                label = { Text(destination.title, fontSize = 10.sp, maxLines = 1) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF0F172A),
                                    selectedTextColor = Color(0xFF38BDF8),
                                    indicatorColor = Color(0xFF38BDF8),
                                    unselectedIconColor = Color(0xFF94A3B8),
                                    unselectedTextColor = Color(0xFF94A3B8)
                                ),
                                modifier = Modifier
                                    .width(76.dp)
                                    .testTag("nav_bottom_${destination.name.lowercase()}")
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    ScreenContent(
                        destination = currentNav,
                        viewModel = viewModel,
                        activeProject = activeProject,
                        activeFiles = activeFiles,
                        currentFileName = currentFileName,
                        consoleMessages = consoleMessages,
                        aiSettings = aiSettings,
                        editorSettings = editorSettings,
                        chatMessages = chatMessages,
                        isAiLoading = isAiLoading,
                        allProjects = allProjects
                    )
                }
            }
        }
    }
}

@Composable
private fun NavIconWithBadge(destination: NavDestination, errorCount: Int) {
    val icon = when (destination) {
        NavDestination.HOME -> Icons.Default.Home
        NavDestination.EDITOR -> Icons.Default.Code
        NavDestination.AI -> Icons.Default.AutoAwesome
        NavDestination.LEARN -> Icons.Default.School
        NavDestination.GAMES -> Icons.Default.SportsEsports
        NavDestination.WEBSITES -> Icons.Default.Web
        NavDestination.PROJECTS -> Icons.Default.Folder
        NavDestination.COLORS -> Icons.Default.Palette
        NavDestination.SETTINGS -> Icons.Default.Settings
    }

    if (destination == NavDestination.EDITOR && errorCount > 0) {
        BadgedBox(
            badge = {
                Badge(containerColor = Color(0xFFEF4444)) {
                    Text("$errorCount", color = Color.White, fontSize = 8.sp)
                }
            }
        ) {
            Icon(icon, contentDescription = destination.title, modifier = Modifier.size(20.dp))
        }
    } else {
        Icon(icon, contentDescription = destination.title, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun ScreenContent(
    destination: NavDestination,
    viewModel: MainViewModel,
    activeProject: com.example.model.Project?,
    activeFiles: List<com.example.model.ProjectFile>,
    currentFileName: String,
    consoleMessages: List<com.example.model.ConsoleMessage>,
    aiSettings: com.example.model.AiSettings,
    editorSettings: com.example.model.EditorSettings,
    chatMessages: List<com.example.model.AiChatMessage>,
    isAiLoading: Boolean,
    allProjects: List<com.example.model.Project>
) {
    when (destination) {
        NavDestination.HOME -> {
            HomeScreen(
                recentProjects = allProjects,
                userExperienceLevel = aiSettings.experienceLevel,
                onExperienceLevelSelected = { viewModel.setExperienceLevel(it) },
                onOpenProject = {
                    viewModel.selectProject(it)
                    viewModel.navigateTo(NavDestination.EDITOR)
                },
                onStartLearning = { viewModel.navigateTo(NavDestination.LEARN) },
                onCreateWebsite = { viewModel.navigateTo(NavDestination.WEBSITES) },
                onCreateGame = { viewModel.navigateTo(NavDestination.GAMES) },
                onAskAi = { viewModel.navigateTo(NavDestination.AI) },
                onExploreProjects = { viewModel.navigateTo(NavDestination.PROJECTS) },
                onSelectTemplate = { template ->
                    viewModel.createProject(template.title, template)
                }
            )
        }

        NavDestination.EDITOR -> {
            if (activeProject != null) {
                EditorScreen(
                    project = activeProject,
                    files = activeFiles,
                    currentFileName = currentFileName,
                    editorSettings = editorSettings,
                    consoleMessages = consoleMessages,
                    onSelectFile = { viewModel.selectFile(it) },
                    onCodeChanged = { fname, code -> viewModel.updateFileContent(fname, code) },
                    onAddNewFile = { viewModel.addNewFile(it) },
                    onDeleteFile = { viewModel.deleteFile(it) },
                    onConsoleMessage = { viewModel.addConsoleMessage(it) },
                    onClearConsole = { viewModel.clearConsole() },
                    onFixWithAi = { msg ->
                        viewModel.navigateTo(NavDestination.AI)
                        val fixPrompt = "Fix this runtime error in ${msg.sourceFile} at line ${msg.lineNumber}:\n\nError: ${msg.message}\n\nPlease explain what happened, why it happened, and update the code."
                        val contextMap = activeFiles.associate { it.name to it.content }
                        viewModel.sendAiPrompt(fixPrompt, contextMap, com.example.ui.screens.AiContextScope.FULL_PROJECT)
                    },
                    onOpenAiTab = { viewModel.navigateTo(NavDestination.AI) },
                    onLearnTopic = {
                        viewModel.navigateTo(NavDestination.LEARN)
                    }
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No project opened. Select or create a project.", color = Color.Gray)
                }
            }
        }

        NavDestination.AI -> {
            AiAssistantScreen(
                settings = aiSettings,
                chatMessages = chatMessages,
                activeFiles = activeFiles,
                currentFileName = currentFileName,
                isLoading = isAiLoading,
                onSendMessage = { prompt, contextFiles, scope ->
                    viewModel.sendAiPrompt(prompt, contextFiles, scope)
                },
                onApplyProposal = { proposal ->
                    viewModel.applyChangeProposal(proposal)
                },
                onOpenSettings = { viewModel.navigateTo(NavDestination.SETTINGS) },
                onClearChat = { viewModel.clearChat() }
            )
        }

        NavDestination.LEARN -> {
            LearningScreen(
                onOpenInEditor = { title, html, css, js ->
                    viewModel.openSandboxInEditor(title, html, css, js)
                }
            )
        }

        NavDestination.GAMES -> {
            GameStudioView(
                onGenerateGameWithAi = { gamePrompt ->
                    viewModel.navigateTo(NavDestination.AI)
                    val contextMap = activeFiles.associate { it.name to it.content }
                    viewModel.sendAiPrompt(gamePrompt, contextMap, com.example.ui.screens.AiContextScope.FULL_PROJECT)
                },
                onInjectLevelMapIntoProject = { json ->
                    viewModel.injectLevelMapIntoScript(json)
                }
            )
        }

        NavDestination.WEBSITES -> {
            WebsiteBuilderView(
                onExportToCode = { html, css ->
                    viewModel.createProject("Custom Visual Website", null)
                    viewModel.updateFileContent("index.html", html)
                    viewModel.updateFileContent("style.css", css)
                    viewModel.navigateTo(NavDestination.EDITOR)
                }
            )
        }

        NavDestination.PROJECTS -> {
            ProjectsScreen(
                projects = allProjects,
                activeProjectId = activeProject?.id ?: "",
                onSelectProject = {
                    viewModel.selectProject(it)
                    viewModel.navigateTo(NavDestination.EDITOR)
                },
                onCreateProject = { name, tmpl ->
                    viewModel.createProject(name, tmpl)
                },
                onRenameProject = { proj, newName -> viewModel.renameProject(proj, newName) },
                onDeleteProject = { viewModel.deleteProject(it) },
                onDuplicateProject = { viewModel.duplicateProject(it) },
                onExportZip = { viewModel.exportProjectZip(it) },
                onImportZip = { name, bytes -> viewModel.importProjectZip(name, bytes) },
                onCreateSnapshot = { viewModel.createProjectSnapshot(it) }
            )
        }

        NavDestination.COLORS -> {
            ColorLabView()
        }

        NavDestination.SETTINGS -> {
            SettingsScreen(
                aiSettings = aiSettings,
                editorSettings = editorSettings,
                onSaveAiSettings = { viewModel.saveAiSettings(it) },
                onSaveEditorSettings = { viewModel.saveEditorSettings(it) },
                aiService = viewModel.aiService
            )
        }
    }
}
