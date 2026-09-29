package com.example.data.apk

import com.example.model.ProjectFile
import java.util.regex.Pattern

object ProjectAnalyzer {

    fun analyzeResources(files: List<ProjectFile>): ResourceAnalysisResult {
        var htmlBytes = 0L
        var cssBytes = 0L
        var jsBytes = 0L
        var imgBytes = 0L
        var audioBytes = 0L
        var videoBytes = 0L
        var fontBytes = 0L
        var otherBytes = 0L
        val warnings = mutableListOf<ResourceWarning>()

        val fileNames = files.map { it.name }.toSet()

        for (file in files) {
            val size = file.content.toByteArray(Charsets.UTF_8).size.toLong()
            val ext = file.extension.lowercase()

            when (ext) {
                "html", "htm" -> {
                    htmlBytes += size
                }
                "css" -> {
                    cssBytes += size
                    if (size > 1024 * 1024) {
                        warnings.add(
                            ResourceWarning(
                                WarningLevel.WARNING,
                                "Large CSS File",
                                "Stylesheet '${file.name}' is ${formatSize(size)}. Consider minifying.",
                                file.name
                            )
                        )
                    }
                }
                "js", "mjs", "ts" -> {
                    jsBytes += size
                    if (size > 2 * 1024 * 1024) {
                        warnings.add(
                            ResourceWarning(
                                WarningLevel.WARNING,
                                "Large JavaScript File",
                                "Script '${file.name}' is ${formatSize(size)}. Parsing may be slow on budget devices.",
                                file.name
                            )
                        )
                    }
                }
                "png", "jpg", "jpeg", "webp", "gif", "svg", "ico" -> {
                    imgBytes += size
                    if (size > 5 * 1024 * 1024) {
                        warnings.add(
                            ResourceWarning(
                                WarningLevel.WARNING,
                                "Large Image Asset",
                                "Image '${file.name}' is ${formatSize(size)}. Recommend compressing for mobile WebView.",
                                file.name
                            )
                        )
                    }
                }
                "mp3", "wav", "ogg", "m4a", "aac" -> {
                    audioBytes += size
                    if (size > 10 * 1024 * 1024) {
                        warnings.add(
                            ResourceWarning(
                                WarningLevel.WARNING,
                                "Large Audio Track",
                                "Audio track '${file.name}' is ${formatSize(size)}.",
                                file.name
                            )
                        )
                    }
                }
                "mp4", "webm", "ogv" -> {
                    videoBytes += size
                    if (size > 25 * 1024 * 1024) {
                        warnings.add(
                            ResourceWarning(
                                WarningLevel.WARNING,
                                "Large Video Asset",
                                "Video '${file.name}' is ${formatSize(size)}. Mobile WebView has limited video buffers.",
                                file.name
                            )
                        )
                    }
                }
                "ttf", "otf", "woff", "woff2" -> {
                    fontBytes += size
                }
                else -> {
                    otherBytes += size
                }
            }
        }

        if (files.size > 200) {
            warnings.add(
                ResourceWarning(
                    WarningLevel.INFO,
                    "High File Count",
                    "Project contains ${files.size} files. Packaging may take a few seconds."
                )
            )
        }

        // Check broken local references in HTML files
        for (file in files.filter { it.extension.equals("html", ignoreCase = true) }) {
            val broken = findBrokenReferences(file.content, fileNames)
            for (missing in broken) {
                warnings.add(
                    ResourceWarning(
                        WarningLevel.WARNING,
                        "Missing Referenced Asset",
                        "File '${file.name}' references '$missing', which is not in the project files.",
                        file.name
                    )
                )
            }
        }

        val total = htmlBytes + cssBytes + jsBytes + imgBytes + audioBytes + videoBytes + fontBytes + otherBytes

        return ResourceAnalysisResult(
            htmlSizeBytes = htmlBytes,
            cssSizeBytes = cssBytes,
            jsSizeBytes = jsBytes,
            imagesSizeBytes = imgBytes,
            audioSizeBytes = audioBytes,
            videoSizeBytes = videoBytes,
            fontsSizeBytes = fontBytes,
            otherSizeBytes = otherBytes,
            totalSizeBytes = total,
            totalFilesCount = files.size,
            warnings = warnings
        )
    }

    fun checkCompatibility(files: List<ProjectFile>): CompatibilityAnalysisResult {
        val features = mutableListOf<CompatibilityFeature>()
        var warningsCount = 0

        val allContent = files.joinToString("\n") { it.content }

        // 1. HTML5 Core
        features.add(
            CompatibilityFeature(
                "HTML5 & DOM Engine",
                FeatureStatus.SUPPORTED,
                "Full standard DOM, semantic tags, and layout rendering."
            )
        )

        // 2. CSS3 & Animations
        features.add(
            CompatibilityFeature(
                "CSS3 & Flexbox/Grid",
                FeatureStatus.SUPPORTED,
                "Full support for Flexbox, CSS Grid, Transitions, Keyframe Animations, and Media Queries."
            )
        )

        // 3. JavaScript Runtime
        features.add(
            CompatibilityFeature(
                "JavaScript (ES6+)",
                FeatureStatus.SUPPORTED,
                "Modern V8 JavaScript engine enabled inside WebView."
            )
        )

        // 4. Touch & Pointer Events
        features.add(
            CompatibilityFeature(
                "Touch & Gesture Input",
                FeatureStatus.SUPPORTED,
                "Hardware touch events (touchstart, touchmove, touchend) and pointer events supported."
            )
        )

        // 5. Local Assets
        features.add(
            CompatibilityFeature(
                "Bundled Local Assets",
                FeatureStatus.SUPPORTED,
                "Direct asset loading from APK website package without network requirements."
            )
        )

        // 6. Canvas 2D
        if (allContent.contains("<canvas", ignoreCase = true) || allContent.contains("getContext('2d')", ignoreCase = true) || allContent.contains("getContext(\"2d\")", ignoreCase = true)) {
            features.add(
                CompatibilityFeature(
                    "HTML5 2D Canvas",
                    FeatureStatus.SUPPORTED,
                    "Hardware-accelerated 2D canvas animation loops supported."
                )
            )
        }

        // 7. WebGL
        if (allContent.contains("webgl", ignoreCase = true) || allContent.contains("three.js", ignoreCase = true)) {
            warningsCount++
            features.add(
                CompatibilityFeature(
                    "WebGL 3D Graphics",
                    FeatureStatus.WARNING,
                    "WebGL detected. Performance varies depending on the device GPU and Android WebView hardware acceleration."
                )
            )
        }

        // 8. LocalStorage & IndexedDB
        if (allContent.contains("localStorage", ignoreCase = true) || allContent.contains("sessionStorage", ignoreCase = true)) {
            features.add(
                CompatibilityFeature(
                    "LocalStorage & DOM Storage",
                    FeatureStatus.SUPPORTED,
                    "DOM storage database enabled for offline game saves and user preferences."
                )
            )
        }

        if (allContent.contains("indexedDB", ignoreCase = true)) {
            features.add(
                CompatibilityFeature(
                    "IndexedDB Database",
                    FeatureStatus.SUPPORTED,
                    "Structured client-side database supported."
                )
            )
        }

        // 9. Fetch & XMLHttpRequest
        if (allContent.contains("fetch(", ignoreCase = true) || allContent.contains("XMLHttpRequest", ignoreCase = true)) {
            features.add(
                CompatibilityFeature(
                    "Fetch & AJAX Requests",
                    FeatureStatus.SUPPORTED,
                    "Supported. Remote requests require Internet Access permission."
                )
            )
        }

        // 10. External URLs
        val externalPattern = Pattern.compile("https?://[^\\s\"'<>]+")
        val matcher = externalPattern.matcher(allContent)
        val externalUrls = mutableListOf<String>()
        while (matcher.find()) {
            externalUrls.add(matcher.group())
        }

        if (externalUrls.isNotEmpty()) {
            warningsCount++
            features.add(
                CompatibilityFeature(
                    "External Online Resources",
                    FeatureStatus.WARNING,
                    "Found ${externalUrls.size} external URL reference(s) (e.g. ${externalUrls.first().take(40)}...). Set Internet Access to ON for these resources to load."
                )
            )
        }

        // 11. Unsupported schemes
        if (allContent.contains("chrome://", ignoreCase = true) || allContent.contains("about:blank", ignoreCase = true)) {
            warningsCount++
            features.add(
                CompatibilityFeature(
                    "Browser Internal Schemes",
                    FeatureStatus.WARNING,
                    "Browser-specific schemes (such as chrome://) are not supported inside Android WebView."
                )
            )
        }

        return CompatibilityAnalysisResult(features, warningsCount)
    }

    fun checkProjectErrors(files: List<ProjectFile>, entryFileName: String = "index.html"): ProjectErrorCheckResult {
        val entry = files.find { it.name.equals(entryFileName, ignoreCase = true) }
        val hasEntry = entry != null
        val fileNames = files.map { it.name }.toSet()

        val cssFiles = files.filter { it.extension.equals("css", ignoreCase = true) }
        val jsFiles = files.filter { it.extension.equals("js", ignoreCase = true) || it.extension.equals("mjs", ignoreCase = true) }

        val missing = if (entry != null) findBrokenReferences(entry.content, fileNames) else emptyList()

        val allContent = files.joinToString("\n") { it.content }
        val externalPattern = Pattern.compile("https?://[^\\s\"'<>]+")
        val matcher = externalPattern.matcher(allContent)
        val externalUrls = mutableListOf<String>()
        while (matcher.find()) {
            externalUrls.add(matcher.group())
        }

        val fatalMessage = when {
            files.isEmpty() -> "Project contains no files to package."
            !hasEntry -> "Entry file '$entryFileName' not found in project. A valid HTML entry file is required for Android WebView."
            else -> null
        }

        return ProjectErrorCheckResult(
            hasEntryFile = hasEntry,
            entryFileName = entryFileName,
            cssValid = cssFiles.isNotEmpty() || true,
            jsValid = jsFiles.isNotEmpty() || true,
            localAssetsFound = files.size > 1,
            externalResourcesFound = externalUrls.isNotEmpty(),
            externalUrls = externalUrls.distinct(),
            missingFiles = missing,
            canPackage = fatalMessage == null,
            fatalMessage = fatalMessage
        )
    }

    private fun findBrokenReferences(htmlContent: String, existingFileNames: Set<String>): List<String> {
        val broken = mutableListOf<String>()
        // Match src="..." or href="..."
        val refPattern = Pattern.compile("(?:src|href)\\s*=\\s*[\"']([^\"'#?]+)[\"']", Pattern.CASE_INSENSITIVE)
        val matcher = refPattern.matcher(htmlContent)
        while (matcher.find()) {
            val ref = matcher.group(1).trim()
            if (ref.startsWith("http://") || ref.startsWith("https://") || ref.startsWith("data:") || ref.startsWith("mailto:") || ref.startsWith("tel:") || ref.startsWith("javascript:")) {
                continue
            }
            val fileName = ref.substringAfterLast("/")
            if (fileName.isNotBlank() && !existingFileNames.contains(fileName) && !existingFileNames.contains(ref)) {
                broken.add(ref)
            }
        }
        return broken.distinct()
    }

    private fun formatSize(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val kb = bytes / 1024.0
        if (kb < 1024) return String.format("%.1f KB", kb)
        val mb = kb / 1024.0
        return String.format("%.2f MB", mb)
    }
}
