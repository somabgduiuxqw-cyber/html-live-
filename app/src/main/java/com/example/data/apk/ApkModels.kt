package com.example.data.apk

import java.io.File
import java.util.UUID

enum class StartupType(val displayName: String) {
    NONE("None"),
    HTML_LIVE("HTML Live Startup"),
    CUSTOM("Custom Project Startup"),
    USERNAME("Username Startup")
}

enum class ScreenOrientation(val displayName: String, val manifestValue: String) {
    AUTOMATIC("Automatic", "unspecified"),
    PORTRAIT("Portrait", "portrait"),
    LANDSCAPE("Landscape", "landscape"),
    SENSOR("Sensor", "sensor")
}

enum class DisplayMode(val displayName: String) {
    NORMAL("Normal"),
    FULLSCREEN("Fullscreen"),
    IMMERSIVE("Immersive")
}

enum class InternetAccessMode(val displayName: String) {
    OFFLINE_ONLY("Offline only"),
    INTERNET_ENABLED("Internet enabled"),
    ASK_WHEN_REQUIRED("Ask when required")
}

enum class IconMode(val displayName: String) {
    DEFAULT("Default HTML Live Icon"),
    PROJECT_IMAGE("Use Project Image"),
    GENERATED("Generate from Project Info"),
    CUSTOM("Choose Custom Image")
}

enum class ExternalLinkBehavior(val displayName: String) {
    OPEN_INSIDE("Open inside APK"),
    OPEN_BROWSER("Open Android browser"),
    ASK_EVERY_TIME("Ask every time")
}

enum class BackButtonBehavior(val displayName: String) {
    BROWSER_HISTORY("Navigate browser history"),
    CONFIRM_EXIT("Show exit confirmation"),
    EXIT_IMMEDIATELY("Exit immediately")
}

enum class ApkBuildProfile(val displayName: String) {
    DEBUG("Debug / Test"),
    RELEASE("Release")
}

data class WebRuntimeConfig(
    val javascript: Boolean = true,
    val domStorage: Boolean = true,
    val localStorage: Boolean = true,
    val indexedDb: Boolean = true,
    val canvas: Boolean = true,
    val webGl: Boolean = true,
    val webAudio: Boolean = true,
    val mediaPlayback: Boolean = true,
    val fileAccess: Boolean = true,
    val safeMixedContent: Boolean = true
)

data class ApkPreset(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val appName: String,
    val packageName: String,
    val username: String,
    val orientation: ScreenOrientation = ScreenOrientation.SENSOR,
    val displayMode: DisplayMode = DisplayMode.FULLSCREEN,
    val internetAccess: InternetAccessMode = InternetAccessMode.OFFLINE_ONLY,
    val startupType: StartupType = StartupType.USERNAME,
    val entryFile: String = "index.html",
    val profile: ApkBuildProfile = ApkBuildProfile.RELEASE
)

data class ApkConfig(
    val projectId: String,
    val appName: String,
    val packageName: String,
    val versionName: String = "1.0",
    val versionCode: Int = 1,
    val username: String = "Developer",
    val entryFile: String = "index.html",
    val internetAccess: Boolean = true, // for backwards compat
    val internetMode: InternetAccessMode = if (internetAccess) InternetAccessMode.INTERNET_ENABLED else InternetAccessMode.OFFLINE_ONLY,
    val showStartupScreen: Boolean = true,
    val startupType: StartupType = StartupType.USERNAME,
    val startupDurationSeconds: Int = 2,
    val startupBackgroundColor: String = "#0F172A",
    val orientation: ScreenOrientation = ScreenOrientation.SENSOR,
    val displayMode: DisplayMode = DisplayMode.FULLSCREEN,
    val bundleAssets: Boolean = true,
    val iconMode: IconMode = IconMode.DEFAULT,
    val customIconName: String? = null,
    val customIconBytes: ByteArray? = null,
    val webRuntime: WebRuntimeConfig = WebRuntimeConfig(),
    val externalLinkBehavior: ExternalLinkBehavior = ExternalLinkBehavior.OPEN_BROWSER,
    val backButtonBehavior: BackButtonBehavior = BackButtonBehavior.BROWSER_HISTORY,
    val profile: ApkBuildProfile = ApkBuildProfile.RELEASE
) {
    fun validate(): String? {
        if (appName.isBlank()) return "App name cannot be empty."
        val pkgError = validatePackageName(packageName)
        if (pkgError != null) return pkgError
        if (versionName.isBlank()) return "Version name cannot be empty."
        if (versionCode <= 0) return "Version code must be greater than 0."
        if (entryFile.isBlank()) return "Entry HTML file cannot be empty."
        if (username.isBlank()) return "Creator/Username cannot be empty."
        return null
    }

    companion object {
        fun validatePackageName(pkg: String): String? {
            if (pkg.isBlank()) return "Package name cannot be empty."
            if (pkg.contains(" ")) return "Package name cannot contain spaces."
            if (pkg != pkg.lowercase()) return "Package name must be entirely lowercase."
            val parts = pkg.split(".")
            if (parts.size < 2) return "Package name must contain at least two segments (e.g. com.atp.mygame)."
            for (part in parts) {
                if (part.isEmpty()) return "Package name contains empty segment between dots."
                if (!part[0].isLetter()) {
                    return "Package name segments must start with a lowercase letter: '$part'."
                }
                if (!part.all { it.isLetterOrDigit() || it == '_' }) {
                    return "Package name segments can only contain letters, digits, and underscores: '$part'."
                }
            }
            return null
        }

        fun autoGeneratePackage(username: String, appName: String): String {
            val userClean = username.lowercase().replace("[^a-z0-9]".toRegex(), "").ifEmpty { "developer" }
            val appClean = appName.lowercase().replace("[^a-z0-9]".toRegex(), "").ifEmpty { "game" }
            return "com.$userClean.$appClean"
        }
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

    val formattedVideoSize: String
        get() = formatBytes(videoSizeBytes)

    val formattedFontsSize: String
        get() = formatBytes(fontsSizeBytes)

    val formattedOtherSize: String
        get() = formatBytes(otherSizeBytes)

    val estimatedApkSizeBytes: Long
        get() {
            val baseTemplateBytes = 2_450_000L
            val compressedAssets = (totalSizeBytes * 0.75).toLong()
            return baseTemplateBytes + compressedAssets
        }

    val formattedEstimatedApkSize: String
        get() = formatBytes(estimatedApkSizeBytes)

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
    val syntaxErrors: List<String> = emptyList(),
    val canPackage: Boolean,
    val fatalMessage: String? = null
)

enum class ApkBuildStep(val stepNumber: Int, val title: String) {
    SELECT_PROJECT(1, "Select Project"),
    CHECK_PROJECT(2, "Check Project"),
    SETTINGS(3, "APK Settings"),
    RESOURCE_CHECK(4, "Resource Check"),
    COMPATIBILITY_CHECK(5, "Compatibility Check"),
    PREVIEW(6, "Build Preview"),
    PACKAGING(7, "Package APK"),
    VERIFYING(8, "Verify APK"),
    READY(9, "APK Ready"),
    FAILED(0, "Build Failed")
}

data class ApkBuildProgress(
    val step: ApkBuildStep = ApkBuildStep.SELECT_PROJECT,
    val statusMessage: String = "",
    val percentage: Float = 0f,
    val isPackaging: Boolean = false,
    val error: String? = null
)

data class VerificationCheck(
    val title: String,
    val passed: Boolean,
    val details: String
)

data class ApkValidationResult(
    val isValid: Boolean,
    val outputFile: File? = null,
    val fileSizeBytes: Long = 0L,
    val hasManifest: Boolean = false,
    val hasClassesDex: Boolean = false,
    val hasWebsiteAssets: Boolean = false,
    val hasEntryHtml: Boolean = false,
    val hasUsernameConfig: Boolean = false,
    val isSigned: Boolean = false,
    val isParseablePackage: Boolean = false,
    val localFilesCount: Int = 0,
    val internetEnabled: Boolean = true,
    val errorMessage: String? = null,
    val verificationChecks: List<VerificationCheck> = emptyList()
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
