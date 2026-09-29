package com.example.data.apk

import java.io.File

data class ApkConfig(
    val projectId: String,
    val appName: String,
    val packageName: String,
    val versionName: String = "1.0",
    val versionCode: Int = 1,
    val username: String = "Developer",
    val entryFile: String = "index.html",
    val internetAccess: Boolean = true,
    val showStartupScreen: Boolean = true,
    val customIconName: String? = null
) {
    fun validate(): String? {
        if (appName.isBlank()) return "App name cannot be empty."
        if (packageName.isBlank()) return "Package name cannot be empty."
        val parts = packageName.split(".")
        if (parts.size < 2) return "Package name must contain at least two segments (e.g. com.example.mygame)."
        for (part in parts) {
            if (part.isEmpty() || !part[0].isLetter()) {
                return "Package name segments must start with a letter: '$part'."
            }
            if (!part.all { it.isLetterOrDigit() || it == '_' }) {
                return "Package name segments can only contain letters, digits, and underscores: '$part'."
            }
        }
        if (versionName.isBlank()) return "Version name cannot be empty."
        if (versionCode <= 0) return "Version code must be greater than 0."
        if (entryFile.isBlank()) return "Entry HTML file cannot be empty."
        return null
    }
}

enum class WarningLevel {
    INFO,
    WARNING,
    ERROR
}

data class ResourceWarning(
    val level: WarningLevel,
    val title: String,
    val description: String,
    val fileName: String? = null
)

data class ResourceAnalysisResult(
    val htmlSizeBytes: Long = 0L,
    val cssSizeBytes: Long = 0L,
    val jsSizeBytes: Long = 0L,
    val imagesSizeBytes: Long = 0L,
    val audioSizeBytes: Long = 0L,
    val videoSizeBytes: Long = 0L,
    val fontsSizeBytes: Long = 0L,
    val otherSizeBytes: Long = 0L,
    val totalSizeBytes: Long = 0L,
    val totalFilesCount: Int = 0,
    val warnings: List<ResourceWarning> = emptyList()
) {
    val formattedTotalSize: String
        get() = formatBytes(totalSizeBytes)

    val formattedHtmlSize: String
        get() = formatBytes(htmlSizeBytes)

    val formattedCssSize: String
        get() = formatBytes(cssSizeBytes)

    val formattedJsSize: String
        get() = formatBytes(jsSizeBytes)

    val formattedImagesSize: String
        get() = formatBytes(imagesSizeBytes)

    val formattedAudioSize: String
        get() = formatBytes(audioSizeBytes)

    val formattedFontsSize: String
        get() = formatBytes(fontsSizeBytes)

    private fun formatBytes(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val kb = bytes / 1024.0
        if (kb < 1024) return String.format("%.1f KB", kb)
        val mb = kb / 1024.0
        return String.format("%.2f MB", mb)
    }
}

enum class FeatureStatus {
    SUPPORTED,
    WARNING,
    UNSUPPORTED
}

data class CompatibilityFeature(
    val name: String,
    val status: FeatureStatus,
    val details: String
)

data class CompatibilityAnalysisResult(
    val features: List<CompatibilityFeature>,
    val warningsCount: Int
)

data class ProjectErrorCheckResult(
    val hasEntryFile: Boolean,
    val entryFileName: String,
    val cssValid: Boolean,
    val jsValid: Boolean,
    val localAssetsFound: Boolean,
    val externalResourcesFound: Boolean,
    val externalUrls: List<String> = emptyList(),
    val missingFiles: List<String> = emptyList(),
    val canPackage: Boolean,
    val fatalMessage: String? = null
)

enum class ApkBuildStep(val stepNumber: Int, val title: String) {
    SELECT_PROJECT(1, "Select Project"),
    CHECK_PROJECT(2, "Check Project"),
    SETTINGS(3, "APK Settings"),
    RESOURCE_CHECK(4, "Resource Check"),
    COMPATIBILITY_CHECK(5, "Compatibility Check"),
    PACKAGING(6, "Package APK"),
    VERIFYING(7, "Verify APK"),
    READY(8, "APK Ready"),
    FAILED(0, "Build Failed")
}

data class ApkBuildProgress(
    val step: ApkBuildStep = ApkBuildStep.SELECT_PROJECT,
    val statusMessage: String = "",
    val percentage: Float = 0f,
    val isPackaging: Boolean = false,
    val error: String? = null
)

data class ApkValidationResult(
    val isValid: Boolean,
    val outputFile: File? = null,
    val fileSizeBytes: Long = 0L,
    val hasManifest: Boolean = false,
    val hasClassesDex: Boolean = false,
    val hasWebsiteAssets: Boolean = false,
    val hasEntryHtml: Boolean = false,
    val isSigned: Boolean = false,
    val localFilesCount: Int = 0,
    val internetEnabled: Boolean = true,
    val errorMessage: String? = null
) {
    val formattedSize: String
        get() {
            if (fileSizeBytes < 1024) return "$fileSizeBytes B"
            val kb = fileSizeBytes / 1024.0
            if (kb < 1024) return String.format("%.1f KB", kb)
            val mb = kb / 1024.0
            return String.format("%.2f MB", mb)
        }
}
