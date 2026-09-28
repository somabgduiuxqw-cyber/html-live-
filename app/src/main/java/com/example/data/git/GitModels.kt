package com.example.data.git

enum class DetectedProjectType(
    val title: String,
    val description: String,
    val canRunDirectly: Boolean,
    val recommendation: String
) {
    STATIC_HTML(
        "Static HTML",
        "Classic HTML web page with standard structure.",
        true,
        "Ready to run immediately in HTML Live preview."
    ),
    HTML_CSS_JS(
        "HTML/CSS/JavaScript",
        "Full frontend web project with HTML markup, CSS styling, and JavaScript logic.",
        true,
        "Ready to run, edit, and preview with full console logging."
    ),
    JS_WEB_APP(
        "JavaScript Web App",
        "Single Page Application or interactive dynamic web application.",
        true,
        "Can run directly if bundled or uses standard ESM/vanilla JS scripts."
    ),
    CANVAS_GAME(
        "Canvas Game",
        "HTML5 2D/3D Canvas game with animation loop and interactive controls.",
        true,
        "Ready to play in HTML Live game mode with touch and virtual controls."
    ),
    NODE_WEB_PROJECT(
        "Node-based Web Project",
        "Modern web project utilizing package.json, Vite, or Webpack bundler.",
        false,
        "This project contains npm dependencies or build scripts (package.json/vite.config). Pre-built dist/index.html files can run directly, or source files can be edited and previewed."
    ),
    UNKNOWN(
        "Generic Repository",
        "Repository with mixed or non-standard web files.",
        false,
        "Contains source code. You can browse, edit, and create an index.html entry point."
    )
}

data class GitRepositoryInfo(
    val projectId: String,
    val repoUrl: String,
    val owner: String,
    val repoName: String,
    val defaultBranch: String,
    val currentBranch: String,
    val remoteUrl: String,
    val lastSyncTime: Long = System.currentTimeMillis(),
    val localProjectSizeBytes: Long = 0L,
    val remoteName: String = "origin",
    val detectedType: DetectedProjectType = DetectedProjectType.HTML_CSS_JS,
    val hasIndexHtml: Boolean = false,
    val hasPackageJson: Boolean = false,
    val hasReadme: Boolean = false
) {
    val formattedSize: String
        get() {
            if (localProjectSizeBytes < 1024) return "$localProjectSizeBytes B"
            val kb = localProjectSizeBytes / 1024.0
            if (kb < 1024) return String.format("%.1f KB", kb)
            val mb = kb / 1024.0
            return String.format("%.2f MB", mb)
        }
}

data class GitStatusResult(
    val modified: List<String> = emptyList(),
    val added: List<String> = emptyList(),
    val deleted: List<String> = emptyList(),
    val untracked: List<String> = emptyList(),
    val conflicting: List<String> = emptyList()
) {
    val isClean: Boolean
        get() = modified.isEmpty() && added.isEmpty() && deleted.isEmpty() && untracked.isEmpty() && conflicting.isEmpty()

    val totalChangedCount: Int
        get() = modified.size + added.size + deleted.size + untracked.size + conflicting.size
}

enum class DiffLineType {
    ADDED,
    DELETED,
    CONTEXT
}

data class DiffLine(
    val type: DiffLineType,
    val text: String,
    val oldLineNumber: Int? = null,
    val newLineNumber: Int? = null
)

data class GitFileDiff(
    val filePath: String,
    val diffContent: String = "",
    val oldContent: String = "",
    val newContent: String = "",
    val lines: List<DiffLine> = emptyList()
)

data class GitProgressUpdate(
    val stage: String,
    val current: Int = 0,
    val total: Int = 0,
    val percentage: Float = 0f,
    val statusMessage: String = "",
    val isCompleted: Boolean = false,
    val error: String? = null
)

data class GitMergeConflict(
    val filePath: String,
    val localVersion: String,
    val remoteVersion: String,
    val conflictMarkerText: String
)

data class GitCredentials(
    val username: String = "",
    val tokenOrPassword: String = ""
)

data class GitTutorialTopic(
    val id: String,
    val title: String,
    val subtitle: String,
    val explanation: String,
    val commandExample: String,
    val practicalTip: String
)
