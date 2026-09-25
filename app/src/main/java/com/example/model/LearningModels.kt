package com.example.model

enum class CourseCategory(val title: String, val description: String, val icon: String) {
    HTML("HTML Mastery", "Structure, tags, forms, media, and semantics", "html"),
    CSS("CSS & Styling", "Layouts, flexbox, grid, animations, responsive design", "css"),
    JAVASCRIPT("JavaScript Logic", "Core syntax, DOM manipulation, events, async, and canvas", "js"),
    GAME_DEV("2D Game Studio", "Game loops, physics, collision detection, and canvas arcade", "game"),
    WEB_PROJECTS("Real Web Projects", "Calculators, to-dos, portfolios, and interactive apps", "web")
}

data class Lesson(
    val id: String,
    val category: CourseCategory,
    val order: Int,
    val title: String,
    val level: String, // "Beginner", "Intermediate", "Advanced"
    val durationMinutes: Int,
    val summary: String,
    val theoryMarkdown: String,
    val defaultHtml: String,
    val defaultCss: String,
    val defaultJs: String,
    val commonMistakes: List<String>,
    val challengeQuestion: String,
    val challengeHint: String,
    val isCompleted: Boolean = false,
    val moduleId: String = ""
)

data class TutorialModule(
    val id: String,
    val category: CourseCategory,
    val title: String,
    val description: String,
    val level: String, // "Beginner", "Intermediate", "Advanced"
    val estimatedMinutes: Int,
    val sizeKb: Int, // Simulated offline package size, e.g. 140 KB
    val isDownloaded: Boolean = true,
    val downloadDate: String = "Offline Ready",
    val lessonIds: List<String> = emptyList(),
    val tags: List<String> = emptyList()
)

data class OfflineStorageStats(
    val totalModulesCount: Int,
    val downloadedModulesCount: Int,
    val totalLessonsCount: Int,
    val completedLessonsCount: Int,
    val usedOfflineStorageKb: Int,
    val totalOfflineStorageKb: Int
)

data class CourseProgress(
    val category: CourseCategory,
    val completedLessonIds: Set<String> = emptySet(),
    val totalLessons: Int = 0
) {
    val percentage: Int
        get() = if (totalLessons > 0) ((completedLessonIds.size.toFloat() / totalLessons) * 100).toInt() else 0
}
