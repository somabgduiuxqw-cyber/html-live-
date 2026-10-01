package com.example.data.source

enum class SourceMode(val label: String, val description: String) {
    RAW_HTTP("Raw HTTP Source", "Original payload received directly from web server via HTTP GET"),
    RENDERED_DOM("Rendered DOM", "Dynamic DOM evaluated after JavaScript execution in browser engine")
}

enum class SourceContentType(val displayName: String, val extension: String) {
    HTML("HTML", "html"),
    XML("XML / XHTML", "xml"),
    JSON("JSON", "json"),
    CSS("CSS", "css"),
    JAVASCRIPT("JavaScript", "js"),
    TEXT("Plain Text", "txt"),
    BINARY("Binary / Media", "bin")
}

data class SourceRetrievalResult(
    val url: String,
    val finalUrl: String = url,
    val httpStatusCode: Int = 200,
    val statusMessage: String = "OK",
    val contentType: String = "text/html",
    val detectedLanguage: String = "html",
    val charset: String = "UTF-8",
    val sizeBytes: Long = 0L,
    val responseTimeMs: Long = 0L,
    val rawHttpSource: String = "",
    val renderedDomSource: String? = null,
    val headers: List<Pair<String, String>> = emptyList(),
    val cookies: List<String> = emptyList(),
    val isHttps: Boolean = url.startsWith("https://", ignoreCase = true),
    val tlsVersion: String? = null,
    val cipherSuite: String? = null,
    val isBinary: Boolean = false,
    val errorMessage: String? = null
) {
    val characterCount: Int
        get() = (if (rawHttpSource.isNotEmpty()) rawHttpSource else renderedDomSource ?: "").length

    val lineCount: Int
        get() {
            val text = if (rawHttpSource.isNotEmpty()) rawHttpSource else renderedDomSource ?: ""
            if (text.isEmpty()) return 0
            return text.count { it == '\n' } + 1
        }

    fun getLineCount(mode: SourceMode): Int {
        val text = getSourceForMode(mode)
        if (text.isEmpty()) return 0
        return text.count { it == '\n' } + 1
    }

    val formattedSize: String
        get() {
            return when {
                sizeBytes < 1024 -> "$sizeBytes B"
                sizeBytes < 1024 * 1024 -> String.format("%.1f KB", sizeBytes / 1024.0)
                else -> String.format("%.2f MB", sizeBytes / (1024.0 * 1024.0))
            }
        }

    fun getSourceForMode(mode: SourceMode): String {
        return when (mode) {
            SourceMode.RAW_HTTP -> rawHttpSource
            SourceMode.RENDERED_DOM -> renderedDomSource ?: rawHttpSource
        }
    }
}

data class SourcePreset(
    val title: String,
    val url: String,
    val category: String,
    val description: String
)

object SourcePresets {
    val PRESETS = listOf(
        SourcePreset("Example Domain", "https://example.com", "HTML", "Standard W3C example webpage"),
        SourcePreset("W3C HTML Spec", "https://html.spec.whatwg.org/", "HTML", "Living HTML standard specification"),
        SourcePreset("JSON Sample API", "https://jsonplaceholder.typicode.com/posts/1", "JSON", "REST API JSON object payload"),
        SourcePreset("Hacker News", "https://news.ycombinator.com", "HTML", "Clean semantic HTML table layout"),
        SourcePreset("Wikipedia Main", "https://en.wikipedia.org/wiki/Main_Page", "HTML", "Rich dynamic responsive encyclopedia page"),
        SourcePreset("GitHub Status API", "https://www.githubstatus.com/api/v2/status.json", "JSON", "Live API health status JSON"),
        SourcePreset("CSS Reset Spec", "https://cdnjs.cloudflare.com/ajax/libs/normalize/8.0.1/normalize.min.css", "CSS", "Pure CSS stylesheet source")
    )
}
