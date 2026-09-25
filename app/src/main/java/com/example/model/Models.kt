package com.example.model

enum class ProjectType {
    WEBSITE,
    GAME,
    APP,
    CANVAS,
    EMPTY
}

data class Project(
    val id: String,
    val name: String,
    val description: String = "",
    val type: ProjectType = ProjectType.WEBSITE,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastOpenedFile: String = "index.html",
    val isFavorite: Boolean = false
)

data class ProjectFile(
    val id: String,
    val projectId: String,
    val name: String,
    val path: String,
    val content: String,
    val isMain: Boolean = false
) {
    val extension: String
        get() = name.substringAfterLast('.', "").lowercase()
}

enum class ConsoleLevel {
    LOG,
    INFO,
    WARN,
    ERROR
}

data class ConsoleMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val level: ConsoleLevel,
    val message: String,
    val sourceFile: String = "script.js",
    val lineNumber: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val stackTrace: String? = null
)

enum class ViewportPreset(val title: String, val widthDp: Int, val heightDp: Int) {
    RESPONSIVE("Responsive", 0, 0),
    SMALL_PHONE("Small Phone", 360, 640),
    LARGE_PHONE("Large Phone", 412, 892),
    TABLET("Tablet", 768, 1024),
    DESKTOP("Desktop", 1024, 768)
}

data class EditorSettings(
    val fontSizeSp: Float = 14f,
    val tabSize: Int = 2,
    val wordWrap: Boolean = false,
    val autoIndent: Boolean = true,
    val bracketMatching: Boolean = true,
    val showLineNumbers: Boolean = true,
    val theme: String = "dark",
    val beginnerMode: Boolean = true
)

enum class AiProvider(val displayName: String) {
    GEMINI("Google Gemini"),
    OPENAI("OpenAI (ChatGPT)"),
    CUSTOM("Custom Endpoint")
}

data class AiSettings(
    val provider: AiProvider = AiProvider.GEMINI,
    val geminiApiKey: String = "",
    val openAiApiKey: String = "",
    val customEndpoint: String = "",
    val customApiKey: String = "",
    val customModel: String = "gpt-4o",
    val selectedModel: String = "gemini-2.5-flash",
    val mode: String = "build_explain", // "just_build", "build_explain", "teach_build", "hints"
    val experienceLevel: String = "beginner" // "beginner", "intermediate", "advanced"
)

data class AiChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val role: String, // "user", "assistant", "system"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val changeProposal: AiChangeProposal? = null,
    val teachingSteps: List<TeachingStep>? = null,
    val isThinking: Boolean = false
)

data class TeachingStep(
    val stepNumber: Int,
    val title: String,
    val explanation: String,
    val codeSnippet: String? = null
)

data class AiChangeProposal(
    val title: String,
    val description: String,
    val files: Map<String, String>, // fileName to newContent
    val explanation: String,
    var isApplied: Boolean = false
)

data class ProjectSnapshot(
    val id: String,
    val projectId: String,
    val versionNumber: Int,
    val label: String,
    val createdAt: Long,
    val filesJson: String
)
