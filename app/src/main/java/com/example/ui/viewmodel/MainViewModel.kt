package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AiResponseResult
import com.example.data.ai.AiService
import com.example.data.repository.ProjectRepository
import com.example.data.repository.TemplateProject
import com.example.data.storage.SecurePreferences
import com.example.model.AiChangeProposal
import com.example.model.AiChatMessage
import com.example.model.AiSettings
import com.example.model.ConsoleLevel
import com.example.model.ConsoleMessage
import com.example.model.EditorSettings
import com.example.model.Project
import com.example.model.ProjectFile
import com.example.model.ProjectType
import com.example.ui.screens.AiContextScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

enum class NavDestination(val title: String, val iconName: String) {
    HOME("Home", "home"),
    EDITOR("Editor", "code"),
    AI("AI", "auto_awesome"),
    LEARN("Learn", "school"),
    GAMES("Game Studio", "sports_esports"),
    WEBSITES("Website Builder", "web"),
    PROJECTS("Projects", "folder"),
    COLORS("Color Lab", "palette"),
    SETTINGS("Settings", "settings")
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val repository = ProjectRepository(application)
    val preferences = SecurePreferences(application)
    val aiService = AiService()

    val allProjects: StateFlow<List<Project>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentNav = MutableStateFlow(NavDestination.HOME)
    val currentNav: StateFlow<NavDestination> = _currentNav.asStateFlow()

    private val navStack = mutableListOf(NavDestination.HOME)

    private val _activeProject = MutableStateFlow<Project?>(null)
    val activeProject: StateFlow<Project?> = _activeProject.asStateFlow()

    private val _activeFiles = MutableStateFlow<List<ProjectFile>>(emptyList())
    val activeFiles: StateFlow<List<ProjectFile>> = _activeFiles.asStateFlow()

    private val _currentFileName = MutableStateFlow("index.html")
    val currentFileName: StateFlow<String> = _currentFileName.asStateFlow()

    private val _consoleMessages = MutableStateFlow<List<ConsoleMessage>>(emptyList())
    val consoleMessages: StateFlow<List<ConsoleMessage>> = _consoleMessages.asStateFlow()

    private val _aiSettings = MutableStateFlow(preferences.getAiSettings())
    val aiSettings: StateFlow<AiSettings> = _aiSettings.asStateFlow()

    private val _editorSettings = MutableStateFlow(preferences.getEditorSettings())
    val editorSettings: StateFlow<EditorSettings> = _editorSettings.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<AiChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<AiChatMessage>> = _chatMessages.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initDefaultProjectsIfNeeded()
            // Observe projects and select the first one if none selected
            repository.allProjects.collect { projects ->
                if (_activeProject.value == null && projects.isNotEmpty()) {
                    selectProject(projects.first())
                }
            }
        }
    }

    fun navigateTo(dest: NavDestination) {
        if (_currentNav.value != dest) {
            navStack.add(dest)
            _currentNav.value = dest
        }
    }

    fun handleBack(): Boolean {
        if (navStack.size > 1) {
            navStack.removeAt(navStack.lastIndex)
            _currentNav.value = navStack.last()
            return true
        }
        return false
    }

    fun selectProject(project: Project) {
        _activeProject.value = project
        _consoleMessages.value = emptyList()
        viewModelScope.launch {
            repository.getFilesForProject(project.id).collect { files ->
                _activeFiles.value = files
                if (files.isNotEmpty()) {
                    if (files.none { it.name == _currentFileName.value }) {
                        _currentFileName.value = files.first().name
                    }
                }
            }
        }
    }

    fun selectFile(fileName: String) {
        _currentFileName.value = fileName
    }

    fun updateFileContent(fileName: String, newContent: String) {
        val proj = _activeProject.value ?: return
        viewModelScope.launch {
            repository.saveFile(proj.id, fileName, newContent)
        }
    }

    fun addNewFile(fileName: String) {
        val proj = _activeProject.value ?: return
        viewModelScope.launch {
            val templateContent = when (fileName.substringAfterLast('.', "")) {
                "css" -> "/* $fileName */\n"
                "js" -> "// $fileName\n"
                "json" -> "{\n  \"name\": \"data\"\n}"
                else -> ""
            }
            repository.saveFile(proj.id, fileName, templateContent)
            _currentFileName.value = fileName
        }
    }

    fun deleteFile(fileName: String) {
        val proj = _activeProject.value ?: return
        viewModelScope.launch {
            repository.deleteFile(proj.id, fileName)
            if (_currentFileName.value == fileName) {
                _currentFileName.value = "index.html"
            }
        }
    }

    fun createProject(name: String, template: TemplateProject?) {
        viewModelScope.launch {
            val proj = repository.createProject(name, template = template)
            selectProject(proj)
            navigateTo(NavDestination.EDITOR)
        }
    }

    fun renameProject(project: Project, newName: String) {
        viewModelScope.launch {
            repository.updateProject(project.copy(name = newName))
        }
    }

    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            repository.deleteProject(projectId)
            if (_activeProject.value?.id == projectId) {
                val remaining = repository.allProjects
            }
        }
    }

    fun duplicateProject(project: Project) {
        viewModelScope.launch {
            val files = repository.getFilesSync(project.id)
            val newProj = repository.createProject("${project.name} (Copy)", project.description, project.type)
            for (f in files) {
                repository.saveFile(newProj.id, f.name, f.content)
            }
        }
    }

    fun exportProjectZip(projectId: String): File? {
        var exportedFile: File? = null
        viewModelScope.launch {
            exportedFile = repository.exportProjectToZip(projectId)
        }
        return exportedFile
    }

    fun importProjectZip(name: String, bytes: ByteArray) {
        viewModelScope.launch {
            val imported = repository.importProjectFromZip(name, bytes)
            selectProject(imported)
        }
    }

    fun createProjectSnapshot(projectId: String) {
        viewModelScope.launch {
            repository.createSnapshot(projectId)
        }
    }

    fun addConsoleMessage(message: ConsoleMessage) {
        _consoleMessages.value = _consoleMessages.value + message
    }

    fun clearConsole() {
        _consoleMessages.value = emptyList()
    }

    fun saveAiSettings(settings: AiSettings) {
        preferences.saveAiSettings(settings)
        _aiSettings.value = settings
    }

    fun saveEditorSettings(settings: EditorSettings) {
        preferences.saveEditorSettings(settings)
        _editorSettings.value = settings
    }

    fun setExperienceLevel(level: String) {
        val updated = _aiSettings.value.copy(experienceLevel = level)
        saveAiSettings(updated)
    }

    // AI Communication
    fun sendAiPrompt(
        prompt: String,
        contextFiles: Map<String, String>,
        scope: AiContextScope
    ) {
        val userMsg = AiChatMessage(role = "user", content = prompt)
        _chatMessages.value = _chatMessages.value + userMsg
        _isAiLoading.value = true

        viewModelScope.launch {
            val result = aiService.generateResponse(
                prompt = prompt,
                settings = _aiSettings.value,
                projectContextFiles = contextFiles
            )
            _isAiLoading.value = false

            if (result.isSuccess) {
                val response = result.getOrNull()!!
                val assistantMsg = AiChatMessage(
                    role = "assistant",
                    content = response.content,
                    changeProposal = response.changeProposal,
                    teachingSteps = response.teachingSteps
                )
                _chatMessages.value = _chatMessages.value + assistantMsg
            } else {
                val errorMsg = AiChatMessage(
                    role = "assistant",
                    content = "AI request failed: ${result.exceptionOrNull()?.message}\n\nPlease check your API key in Settings."
                )
                _chatMessages.value = _chatMessages.value + errorMsg
            }
        }
    }

    fun applyChangeProposal(proposal: AiChangeProposal) {
        val proj = _activeProject.value ?: return
        viewModelScope.launch {
            for ((fileName, content) in proposal.files) {
                repository.saveFile(proj.id, fileName, content)
            }
            proposal.isApplied = true
        }
    }

    fun clearChat() {
        _chatMessages.value = emptyList()
    }

    fun openSandboxInEditor(title: String, html: String, css: String, js: String) {
        viewModelScope.launch {
            val proj = repository.createProject("Lesson Sandbox: $title")
            repository.saveFile(proj.id, "index.html", html)
            repository.saveFile(proj.id, "style.css", css)
            repository.saveFile(proj.id, "script.js", js)
            selectProject(proj)
            navigateTo(NavDestination.EDITOR)
        }
    }

    fun injectLevelMapIntoScript(json: String) {
        val scriptFile = _activeFiles.value.find { it.name == "script.js" }
        if (scriptFile != null) {
            val updated = scriptFile.content + "\n\n// Injected Level Map Data\nconst LEVEL_MAP = $json;\n"
            updateFileContent("script.js", updated)
        }
    }
}
