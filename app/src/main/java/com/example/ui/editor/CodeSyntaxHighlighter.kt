package com.example.ui.editor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import java.util.regex.Pattern

object CodeSyntaxHighlighter {

    // Theme Colors
    private val TagColor = Color(0xFFF43F5E) // Rose red
    private val KeywordColor = Color(0xFFC084FC) // Purple
    private val AttributeColor = Color(0xFF38BDF8) // Cyan
    private val StringColor = Color(0xFF34D399) // Mint emerald
    private val NumberColor = Color(0xFFFB923C) // Orange
    private val CommentColor = Color(0xFF64748B) // Slate grey
    private val PropertyColor = Color(0xFF60A5FA) // Light blue
    private val FunctionColor = Color(0xFFFACC15) // Gold
    private val PunctuationColor = Color(0xFF94A3B8)

    fun highlight(code: String, extension: String): AnnotatedString {
        val builder = AnnotatedString.Builder(code)
        if (code.isEmpty()) return builder.toAnnotatedString()

        when (extension.lowercase()) {
            "html", "htm", "svg" -> highlightHtml(code, builder)
            "css" -> highlightCss(code, builder)
            "js", "javascript" -> highlightJs(code, builder)
            "json" -> highlightJson(code, builder)
            else -> highlightGeneric(code, builder)
        }

        return builder.toAnnotatedString()
    }

    private fun highlightHtml(code: String, builder: AnnotatedString.Builder) {
        // Comments: <!-- ... -->
        applyRegex(code, builder, "<!--[\\s\\S]*?-->", CommentColor, fontStyle = FontStyle.Italic)

        // Tags: </?[a-zA-Z0-9-]+
        applyRegex(code, builder, "</?[a-zA-Z0-9-]+", TagColor, fontWeight = FontWeight.Bold)

        // Closing bracket: > or />
        applyRegex(code, builder, "/?>", TagColor)

        // Attributes: [a-zA-Z0-9-]+(?=\\=)
        applyRegex(code, builder, "\\b[a-zA-Z0-9-]+(?=\\=)", AttributeColor)

        // Strings: "[^"]*" or '[^']*'
        applyRegex(code, builder, "\"[^\"]*\"|'[^']*'", StringColor)

        // DOCTYPE
        applyRegex(code, builder, "<!DOCTYPE[^>]*>", KeywordColor, fontWeight = FontWeight.Bold)
    }

    private fun highlightCss(code: String, builder: AnnotatedString.Builder) {
        // Comments: /* ... */
        applyRegex(code, builder, "/\\*[\\s\\S]*?\\*/", CommentColor, fontStyle = FontStyle.Italic)

        // Properties: [a-zA-Z-]+(?=\\s*:)
        applyRegex(code, builder, "[a-zA-Z-]+(?=\\s*:)", PropertyColor, fontWeight = FontWeight.Medium)

        // Hex colors: #[0-9a-fA-F]{3,8}
        applyRegex(code, builder, "#[0-9a-fA-F]{3,8}\\b", FunctionColor)

        // Units: \\d+(?:\\.\\d+)?(px|rem|em|%|vh|vw|s|ms|deg)
        applyRegex(code, builder, "\\b\\d+(?:\\.\\d+)?(px|rem|em|%|vh|vw|s|ms|deg)\\b", NumberColor)

        // Numbers: \\b\\d+(?:\\.\\d+)?\\b
        applyRegex(code, builder, "\\b\\d+(?:\\.\\d+)?\\b", NumberColor)

        // Selectors / Classes: \\.[a-zA-Z0-9_-]+
        applyRegex(code, builder, "\\.[a-zA-Z0-9_-]+", TagColor)

        // IDs: #[a-zA-Z0-9_-]+
        applyRegex(code, builder, "#[a-zA-Z0-9_-]+", AttributeColor)

        // Strings
        applyRegex(code, builder, "\"[^\"]*\"|'[^']*'", StringColor)
    }

    private fun highlightJs(code: String, builder: AnnotatedString.Builder) {
        // Comments: // ... or /* ... */
        applyRegex(code, builder, "//.*|/\\*[\\s\\S]*?\\*/", CommentColor, fontStyle = FontStyle.Italic)

        // Keywords
        val keywords = "\\b(const|let|var|function|return|if|else|for|while|do|switch|case|break|continue|default|class|new|this|typeof|instanceof|import|export|from|as|async|await|try|catch|finally|throw|in|of|void|yield)\\b"
        applyRegex(code, builder, keywords, KeywordColor, fontWeight = FontWeight.Bold)

        // Built-ins: console, window, document, Math, JSON, Array, Object, String, Number, Boolean, Promise, setTimeout, setInterval, requestAnimationFrame
        val builtins = "\\b(console|window|document|Math|JSON|Array|Object|String|Number|Boolean|Promise|setTimeout|setInterval|requestAnimationFrame|localStorage|sessionStorage|ctx|canvas)\\b"
        applyRegex(code, builder, builtins, AttributeColor)

        // Functions: [a-zA-Z0-9_$]+(?=\\s*\\()
        applyRegex(code, builder, "\\b[a-zA-Z0-9_$]+(?=\\s*\\()", FunctionColor)

        // Booleans & Null
        applyRegex(code, builder, "\\b(true|false|null|undefined|NaN|Infinity)\\b", NumberColor, fontWeight = FontWeight.Bold)

        // Numbers
        applyRegex(code, builder, "\\b\\d+(?:\\.\\d+)?\\b", NumberColor)

        // Strings: single, double, template literals
        applyRegex(code, builder, "\"[^\"]*\"|'[^']*'|`[^`]*`", StringColor)
    }

    private fun highlightJson(code: String, builder: AnnotatedString.Builder) {
        // Keys: "[^"]*"(?=\\s*:)
        applyRegex(code, builder, "\"[^\"]*\"(?=\\s*:)", AttributeColor, fontWeight = FontWeight.Medium)
        // String values
        applyRegex(code, builder, ":\\s*(\"[^\"]*\")", StringColor)
        // Numbers
        applyRegex(code, builder, "\\b-?\\d+(?:\\.\\d+)?\\b", NumberColor)
        // Booleans
        applyRegex(code, builder, "\\b(true|false|null)\\b", KeywordColor)
    }

    private fun highlightGeneric(code: String, builder: AnnotatedString.Builder) {
        applyRegex(code, builder, "\"[^\"]*\"|'[^']*'", StringColor)
        applyRegex(code, builder, "\\b\\d+\\b", NumberColor)
    }

    private fun applyRegex(
        text: String,
        builder: AnnotatedString.Builder,
        patternStr: String,
        color: Color,
        fontWeight: FontWeight? = null,
        fontStyle: FontStyle? = null
    ) {
        try {
            val pattern = Pattern.compile(patternStr)
            val matcher = pattern.matcher(text)
            while (matcher.find()) {
                val start = matcher.start()
                val end = matcher.end()
                builder.addStyle(
                    SpanStyle(
                        color = color,
                        fontWeight = fontWeight,
                        fontStyle = fontStyle
                    ),
                    start,
                    end
                )
            }
        } catch (_: Exception) {
        }
    }
}
