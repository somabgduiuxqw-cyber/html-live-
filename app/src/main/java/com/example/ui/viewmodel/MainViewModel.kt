package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AiResponseResult
import com.example.data.ai.AiService
import com.example.data.git.DetectedProjectType
import com.example.data.git.GitCredentials
import com.example.data.git.GitFileDiff
import com.example.data.git.GitMergeConflict
import com.example.data.git.GitProgressUpdate
import com.example.data.git.GitRepositoryInfo
import com.example.data.git.GitRepositoryManager
import com.example.data.git.GitStatusResult
import com.example.data.repository.ProjectRepository
import com.example.data.repository.TemplateProject
import com.example.data.storage.GitCredentialStore
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
import android.content.Context
import android.content.Intent
import com.example.data.apk.ApkBuilderManager
import com.example.data.apk.ApkBuildProgress
import com.example.data.apk.ApkConfig
import com.example.data.apk.ApkValidationResult
import com.example.data.apk.ApkBuildStep
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

enum class NavDestination(val title: String, val iconName: String) {
    HOME("Home", "home"),
    EDITOR("Editor", "code"),
    APK_BUILDER("HTML → APK", "android"),
    AI("AI", "auto_awesome"),
    GIT_CLONE("Git Clone", "download"),
    GIT_PROJECT("Git Hub", "commit"),
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
    val apkBuilderManager = ApkBuilderManager(application)

    private val _apkBuildProgress = MutableStateFlow(ApkBuildProgress())
    val apkBuildProgress: StateFlow<ApkBuildProgress> = _apkBuildProgress.asStateFlow()

    private val _apkValidationResult = MutableStateFlow<ApkValidationResult?>(null)
    val apkValidationResult: StateFlow<ApkValidationResult?> = _apkValidationResult.asStateFlow()

    private val _savedUsername = MutableStateFlow(preferences.getApkUsername())
    val savedUsername: StateFlow<String> = _savedUsername.asStateFlow()

    val aiDebugInfo = aiService.lastDebugInfo

    fun saveApkUsername(username: String) {
        _savedUsername.value = username
        preferences.saveApkUsername(username)
    }

    fun buildApk(config: ApkConfig) {
        val files = _activeFiles.value
        viewModelScope.launch {
            _apkBuildProgress.value = ApkBuildProgress(
                step = ApkBuildStep.PACKAGING,
                statusMessage = "Starting APK packaging...",
                percentage = 0.05f,
                isPackaging = true
            )
            val result = apkBuilderManager.buildApk(config, files) { progress ->
                _apkBuildProgress.value = progress
            }
            _apkValidationResult.value = result
        }
    }

    fun installCurrentApk(context: Context) {
        val file = _apkValidationResult.value?.outputFile ?: return
        try {
            val intent = apkBuilderManager.getInstallIntent(file)
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    fun shareCurrentApk(context: Context) {
        val file = _apkValidationResult.value?.outputFile ?: return
        try {
            val intent = apkBuilderManager.getShareIntent(file)
            context.startActivity(Intent.createChooser(intent, "Share APK"))
        } catch (_: Exception) {}
    }

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

    val gitManager = GitRepositoryManager(application)
    val gitCredentialStore = GitCredentialStore(application)

    private val _gitProgress = MutableStateFlow<GitProgressUpdate?>(null)
    val gitProgress: StateFlow<GitProgressUpdate?> = _gitProgress.asStateFlow()

    private val _gitDetectedType = MutableStateFlow<DetectedProjectType?>(null)
    val gitDetectedType: StateFlow<DetectedProjectType?> = _gitDetectedType.asStateFlow()

    private val _gitClonedProject = MutableStateFlow<Project?>(null)
    val gitClonedProject: StateFlow<Project?> = _gitClonedProject.asStateFlow()

    private val _gitRepoInfo = MutableStateFlow<GitRepositoryInfo?>(null)
    val gitRepoInfo: StateFlow<GitRepositoryInfo?> = _gitRepoInfo.asStateFlow()

    private val _gitStatus = MutableStateFlow(GitStatusResult())
    val gitStatus: StateFlow<GitStatusResult> = _gitStatus.asStateFlow()

    private val _gitDiffs = MutableStateFlow<List<GitFileDiff>>(emptyList())
    val gitDiffs: StateFlow<List<GitFileDiff>> = _gitDiffs.asStateFlow()

    private val _gitBranches = MutableStateFlow<List<String>>(listOf("main"))
    val gitBranches: StateFlow<List<String>> = _gitBranches.asStateFlow()

    private val _gitReadmeContent = MutableStateFlow<String?>(null)
    val gitReadmeContent: StateFlow<String?> = _gitReadmeContent.asStateFlow()

    private val _gitConflicts = MutableStateFlow<List<GitMergeConflict>>(emptyList())
    val gitConflicts: StateFlow<List<GitMergeConflict>> = _gitConflicts.asStateFlow()

    private val _isGitOperating = MutableStateFlow(false)
    val isGitOperating: StateFlow<Boolean> = _isGitOperating.asStateFlow()

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
        loadGitRepoState(project.id)
    }

    fun selectFile(fileName: String) {
        _currentFileName.value = fileName
    }

    fun updateFileContent(fileName: String, newContent: String) {
        val proj = _activeProject.value ?: return
        viewModelScope.launch {
            repository.saveFile(proj.id, fileName, newContent)
            val repoDir = gitManager.getRepoDir(proj.id)
            if (File(repoDir, ".git").exists()) {
                val f = File(repoDir, fileName)
                try {
                    f.parentFile?.mkdirs()
                    f.writeText(newContent, Charsets.UTF_8)
                    _gitStatus.value = gitManager.getStatus(repoDir)
                    _gitDiffs.value = gitManager.getDiff(repoDir)
                } catch (_: Exception) {}
            }
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

    // Git Operations
    fun cloneGitProject(repoUrl: String, projectName: String, credentials: GitCredentials?) {
        viewModelScope.launch {
            _isGitOperating.value = true
            _gitProgress.value = GitProgressUpdate(stage = "Connecting...", percentage = 0.05f, statusMessage = "Validating repository URL...")
            _gitDetectedType.value = null
            _gitClonedProject.value = null

            val projId = UUID.randomUUID().toString()
            val targetDir = gitManager.getRepoDir(projId)

            val cloneResult = gitManager.cloneRepository(
                repoUrl = repoUrl,
                targetDir = targetDir,
                credentials = credentials,
                onProgress = { update ->
                    _gitProgress.value = update
                }
            )

            if (cloneResult.isSuccess) {
                if (credentials != null) {
                    gitCredentialStore.saveCredentials(repoUrl, credentials)
                }

                val importedProj = repository.importProjectFromGitDirectory(
                    projectId = projId,
                    name = projectName,
                    directory = targetDir,
                    repoUrl = repoUrl
                )
                _gitClonedProject.value = importedProj

                val detected = gitManager.detectProjectType(targetDir)
                _gitDetectedType.value = detected

                selectProject(importedProj)
                loadGitRepoState(projId, repoUrl)
            }
            _isGitOperating.value = false
        }
    }

    fun loadGitRepoState(projectId: String, repoUrl: String = "") {
        viewModelScope.launch {
            val dir = gitManager.getRepoDir(projectId)
            if (File(dir, ".git").exists()) {
                val info = gitManager.getRepoInfo(dir, repoUrl, projectId)
                _gitRepoInfo.value = info
                _gitStatus.value = gitManager.getStatus(dir)
                _gitDiffs.value = gitManager.getDiff(dir)
                _gitBranches.value = gitManager.getBranches(dir)
                _gitReadmeContent.value = gitManager.readReadme(dir)
            }
        }
    }

    fun switchGitBranch(branchName: String) {
        val proj = _activeProject.value ?: return
        viewModelScope.launch {
            _isGitOperating.value = true
            val dir = gitManager.getRepoDir(proj.id)
            val res = gitManager.switchBranch(dir, branchName)
            if (res.isSuccess) {
                repository.syncFilesFromDirectory(proj.id, dir)
                loadGitRepoState(proj.id, _gitRepoInfo.value?.repoUrl ?: "")
            }
            _isGitOperating.value = false
        }
    }

    fun pullGitUpdates() {
        val proj = _activeProject.value ?: return
        val url = _gitRepoInfo.value?.repoUrl ?: ""
        val creds = gitCredentialStore.getCredentials(url)
        viewModelScope.launch {
            _isGitOperating.value = true
            val dir = gitManager.getRepoDir(proj.id)
            val res = gitManager.pullRepository(dir, creds) { update ->
                _gitProgress.value = update
            }
            res.onSuccess { summary ->
                _gitConflicts.value = summary.conflicts
                repository.syncFilesFromDirectory(proj.id, dir)
                loadGitRepoState(proj.id, url)
            }
            _isGitOperating.value = false
        }
    }

    fun commitGitChanges(message: String, selectedFiles: List<String>) {
        val proj = _activeProject.value ?: return
        viewModelScope.launch {
            val dir = gitManager.getRepoDir(proj.id)
            val res = gitManager.commit(dir, message, selectedFiles)
            if (res.isSuccess) {
                loadGitRepoState(proj.id, _gitRepoInfo.value?.repoUrl ?: "")
            }
        }
    }

    fun pushGitChanges() {
        val proj = _activeProject.value ?: return
        val url = _gitRepoInfo.value?.repoUrl ?: ""
        val creds = gitCredentialStore.getCredentials(url)
        viewModelScope.launch {
            _isGitOperating.value = true
            val dir = gitManager.getRepoDir(proj.id)
            gitManager.push(dir, creds)
            _isGitOperating.value = false
            loadGitRepoState(proj.id, url)
        }
    }

    fun resolveGitConflict(filePath: String, chosenContent: String) {
        val proj = _activeProject.value ?: return
        viewModelScope.launch {
            val dir = gitManager.getRepoDir(proj.id)
            gitManager.resolveConflict(dir, filePath, chosenContent)
            _gitConflicts.value = _gitConflicts.value.filter { it.filePath != filePath }
            repository.saveFile(proj.id, filePath, chosenContent)
            loadGitRepoState(proj.id, _gitRepoInfo.value?.repoUrl ?: "")
        }
    }

    fun saveGitReadme(content: String) {
        val proj = _activeProject.value ?: return
        viewModelScope.launch {
            val dir = gitManager.getRepoDir(proj.id)
            gitManager.writeReadme(dir, content)
            _gitReadmeContent.value = content
            repository.saveFile(proj.id, "README.md", content)
        }
    }

    fun createFileInGitRepo(fileName: String) {
        val proj = _activeProject.value ?: return
        viewModelScope.launch {
            val dir = gitManager.getRepoDir(proj.id)
            val file = File(dir, fileName)
            if (!file.exists()) {
                file.parentFile?.mkdirs()
                file.createNewFile()
            }
            addNewFile(fileName)
            loadGitRepoState(proj.id, _gitRepoInfo.value?.repoUrl ?: "")
        }
    }
}
