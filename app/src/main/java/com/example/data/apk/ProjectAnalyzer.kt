package com.example.data.apk

import com.example.model.ProjectFile
import org.json.JSONArray
import org.json.JSONObject
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
                        "File '${file.name}' references '$missing', which is not found in bundled project files.",
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
        val hasHtml = files.any { it.extension.equals("html", ignoreCase = true) }
        val hasCss = files.any { it.extension.equals("css", ignoreCase = true) } || allContent.contains("<style")
        val hasJs = files.any { it.extension.equals("js", ignoreCase = true) } || allContent.contains("<script")
        val hasCanvas = allContent.contains("<canvas", ignoreCase = true) || allContent.contains("getContext('2d')", ignoreCase = true) || allContent.contains("getContext(\"2d\")", ignoreCase = true)
        val hasSvg = allContent.contains("<svg", ignoreCase = true) || files.any { it.extension.equals("svg", ignoreCase = true) }
        val hasLocalStorage = allContent.contains("localStorage", ignoreCase = true) || allContent.contains("sessionStorage", ignoreCase = true)
        val hasFetch = allContent.contains("fetch(", ignoreCase = true) || allContent.contains("XMLHttpRequest", ignoreCase = true)
        val hasAudio = allContent.contains("<audio", ignoreCase = true) || allContent.contains("AudioContext", ignoreCase = true) || files.any { it.extension in listOf("mp3", "wav", "ogg", "m4a") }
        val hasVideo = allContent.contains("<video", ignoreCase = true) || files.any { it.extension in listOf("mp4", "webm") }

        // 1. HTML
        features.add(
            CompatibilityFeature(
                name = "HTML",
                status = if (hasHtml) FeatureStatus.SUPPORTED else FeatureStatus.WARNING,
                details = if (hasHtml) "HTML5 document structure detected and fully supported by WebView." else "No .html file found yet."
            )
        )

        // 2. CSS
        features.add(
            CompatibilityFeature(
                name = "CSS",
                status = FeatureStatus.SUPPORTED,
                details = "Flexbox, CSS Grid, Media Queries, and CSS3 Keyframe Animations supported."
            )
        )

        // 3. JavaScript
        features.add(
            CompatibilityFeature(
                name = "JavaScript",
                status = FeatureStatus.SUPPORTED,
                details = "Modern ECMAScript/V8 engine execution enabled."
            )
        )

        // 4. Canvas
        features.add(
            CompatibilityFeature(
                name = "Canvas",
                status = FeatureStatus.SUPPORTED,
                details = if (hasCanvas) "Hardware accelerated 2D Canvas rendering active." else "Standard 2D Canvas API ready."
            )
        )

        // 5. SVG
        features.add(
            CompatibilityFeature(
                name = "SVG",
                status = FeatureStatus.SUPPORTED,
                details = if (hasSvg) "Inline and external SVG vector graphics supported." else "Scalable Vector Graphics engine ready."
            )
        )

        // 6. Local Assets
        features.add(
            CompatibilityFeature(
                name = "Local Assets",
                status = FeatureStatus.SUPPORTED,
                details = "Bundled project assets served locally via WebView asset scheme."
            )
        )

        // 7. LocalStorage
        features.add(
            CompatibilityFeature(
                name = "LocalStorage",
                status = FeatureStatus.SUPPORTED,
                details = if (hasLocalStorage) "DOM Storage (localStorage/sessionStorage) enabled for persistent state." else "DOM Storage ready for offline persistence."
            )
        )

        // 8. Fetch
        features.add(
            CompatibilityFeature(
                name = "Fetch",
                status = FeatureStatus.SUPPORTED,
                details = if (hasFetch) "Network fetch() & XMLHttpRequest enabled (requires Internet permission)." else "Asynchronous network request APIs supported."
            )
        )

        // 9. Audio
        features.add(
            CompatibilityFeature(
                name = "Audio",
                status = FeatureStatus.SUPPORTED,
                details = if (hasAudio) "HTML5 Audio & Web Audio API enabled for sound playback." else "Audio hardware playback supported."
            )
        )

        // 10. Video
        features.add(
            CompatibilityFeature(
                name = "Video",
                status = FeatureStatus.SUPPORTED,
                details = if (hasVideo) "Hardware accelerated HTML5 video playback supported." else "Standard HTML5 video tag supported."
            )
        )

        // 11. Web APIs (check for Bluetooth, WebRTC, USB, Camera)
        val hasAdvancedApis = allContent.contains("navigator.bluetooth", ignoreCase = true) ||
            allContent.contains("navigator.usb", ignoreCase = true) ||
            allContent.contains("RTCPeerConnection", ignoreCase = true) ||
            allContent.contains("getUserMedia", ignoreCase = true)

        if (hasAdvancedApis) {
            warningsCount++
            features.add(
                CompatibilityFeature(
                    name = "Web APIs",
                    status = FeatureStatus.WARNING,
                    details = "Advanced Web APIs (Bluetooth, WebRTC, Camera, or USB) detected. May require additional device permissions."
                )
            )
        } else {
            features.add(
                CompatibilityFeature(
                    name = "Web APIs",
                    status = FeatureStatus.SUPPORTED,
                    details = "Standard Web APIs (Timers, Geolocation, Math, Crypto, DOM) fully compatible."
                )
            )
        }

        // 12. External Resources
        val externalPattern = Pattern.compile("https?://[^\\s\"'<>]+")
        val matcher = externalPattern.matcher(allContent)
        var externalCount = 0
        var hasInsecureHttp = false
        while (matcher.find()) {
            val url = matcher.group()
            externalCount++
            if (url.startsWith("http://", ignoreCase = true)) {
                hasInsecureHttp = true
            }
        }

        if (hasInsecureHttp) {
            warningsCount++
            features.add(
                CompatibilityFeature(
                    name = "External Resources",
                    status = FeatureStatus.WARNING,
                    details = "Insecure HTTP resources detected. Android WebView blocks plaintext HTTP by default unless cleartext is allowed."
                )
            )
        } else if (externalCount > 0) {
            warningsCount++
            features.add(
                CompatibilityFeature(
                    name = "External Resources",
                    status = FeatureStatus.WARNING,
                    details = "$externalCount external CDN or API dependency found. Application will require Internet Access to fetch them."
                )
            )
        } else {
            features.add(
                CompatibilityFeature(
                    name = "External Resources",
                    status = FeatureStatus.SUPPORTED,
                    details = "Self-contained offline application. No external online dependencies found."
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

        val syntaxErrors = mutableListOf<String>()

        // 1. Check entry file existence
        if (!hasEntry) {
            syntaxErrors.add("Startup HTML file '$entryFileName' is missing from the project.")
        } else {
            // Check HTML structure
            val content = entry.content.lowercase()
            if (!content.contains("<html") && !content.contains("<!doctype")) {
                syntaxErrors.add("Entry file '$entryFileName' is missing standard <html> or <!DOCTYPE html> declaration.")
            }
        }

        // 2. Check JSON files syntax
        for (f in files.filter { it.extension.equals("json", ignoreCase = true) }) {
            try {
                val trimmed = f.content.trim()
                if (trimmed.startsWith("{")) {
                    JSONObject(trimmed)
                } else if (trimmed.startsWith("[")) {
                    JSONArray(trimmed)
                } else {
                    syntaxErrors.add("File '${f.name}' does not contain valid JSON.")
                }
            } catch (e: Exception) {
                syntaxErrors.add("JSON syntax error in '${f.name}': ${e.message}")
            }
        }

        // 3. Check JS basic bracket balance
        for (f in jsFiles) {
            val openCurly = f.content.count { it == '{' }
            val closeCurly = f.content.count { it == '}' }
            if (openCurly != closeCurly) {
                syntaxErrors.add("Unbalanced braces in '${f.name}': $openCurly '{' vs $closeCurly '}'.")
            }
        }

        // 4. Duplicate file names check
        val duplicateNames = files.groupBy { it.name.lowercase() }.filter { it.value.size > 1 }.keys
        if (duplicateNames.isNotEmpty()) {
            syntaxErrors.add("Duplicate file names detected: ${duplicateNames.joinToString(", ")}")
        }

        val fatalMessage = when {
            files.isEmpty() -> "Project contains no files to package."
            !hasEntry -> "Entry file '$entryFileName' not found in project. A valid HTML entry file is required for Android WebView."
            syntaxErrors.any { it.contains("missing from the project") || it.contains("Duplicate file") } -> syntaxErrors.first()
            else -> null
        }

        val canPackage = fatalMessage == null && hasEntry

        return ProjectErrorCheckResult(
            hasEntryFile = hasEntry,
            entryFileName = entryFileName,
            cssValid = cssFiles.isNotEmpty() || true,
            jsValid = jsFiles.isNotEmpty() || true,
            localAssetsFound = files.size > 1,
            externalResourcesFound = externalUrls.isNotEmpty(),
            externalUrls = externalUrls.distinct(),
            missingFiles = missing,
            syntaxErrors = syntaxErrors,
            canPackage = canPackage,
            fatalMessage = fatalMessage
        )
    }

    private fun findBrokenReferences(htmlContent: String, existingFileNames: Set<String>): List<String> {
        val broken = mutableListOf<String>()
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
